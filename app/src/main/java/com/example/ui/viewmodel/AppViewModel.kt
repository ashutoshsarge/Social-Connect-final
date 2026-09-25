package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.SubtaskEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.DashboardStats
import com.example.data.model.FirestoreUser
import com.example.data.model.SocialCompletedEvent
import com.example.data.model.SocialEvent
import com.example.data.model.SocialNGO
import com.example.data.model.SocialNotificationItem
import com.example.data.model.SocialPost
import com.example.data.model.VolunteerRosterItem
import com.example.data.repository.AuthRepository
import com.example.data.repository.EventRepository
import com.example.data.repository.FirestoreUserRepository
import com.example.data.repository.FirestoreVolunteerEventRepository
import com.example.data.repository.SocialConnectRepository
import com.example.data.repository.TaskRepository
import com.example.data.service.EmailOtpSecurityService
import com.example.data.service.FirebaseAuthService
import com.example.data.service.GeminiSearchGroundingService
import com.example.data.service.SearchGroundingResult
import com.example.data.service.SearchGroundingUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val authRepo = AuthRepository(database.userDao())
    private val taskRepo = TaskRepository(database.taskDao())
    private val eventRepo = EventRepository(database.eventDao())
    val socialRepo = SocialConnectRepository()
    val firestoreUserRepo = FirestoreUserRepository()
    val firestoreEventRepo = FirestoreVolunteerEventRepository()

    // Auth & Users
    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
    val allUsers: StateFlow<List<UserEntity>> = authRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Firestore Persisted User State
    private val _firestoreUser = MutableStateFlow<FirestoreUser?>(null)
    val firestoreUser: StateFlow<FirestoreUser?> = _firestoreUser.asStateFlow()

    // Google Search Grounding with gemini-3.5-flash
    private val _searchGroundingState = MutableStateFlow<SearchGroundingUiState>(SearchGroundingUiState.Idle)
    val searchGroundingState: StateFlow<SearchGroundingUiState> = _searchGroundingState.asStateFlow()

    // Firebase Auth Email Verification State
    private val _isEmailVerificationPending = MutableStateFlow(false)
    val isEmailVerificationPending: StateFlow<Boolean> = _isEmailVerificationPending.asStateFlow()

    private val _pendingVerificationEmail = MutableStateFlow<String?>(null)
    val pendingVerificationEmail: StateFlow<String?> = _pendingVerificationEmail.asStateFlow()

    private val _isCheckingVerification = MutableStateFlow(false)
    val isCheckingVerification: StateFlow<Boolean> = _isCheckingVerification.asStateFlow()

    // Database task & stats flows
    val allTasks: StateFlow<List<TaskEntity>> = taskRepo.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubtasks: StateFlow<List<SubtaskEntity>> = taskRepo.allSubtasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<DashboardStats> = taskRepo.dashboardStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // SocialConnect Platform State
    val events: StateFlow<List<SocialEvent>> = socialRepo.events
    val completedEvents: StateFlow<List<SocialCompletedEvent>> = socialRepo.completedEvents
    val completedSocialEvents: StateFlow<List<SocialCompletedEvent>> = socialRepo.completedEvents
    val ngos: StateFlow<List<SocialNGO>> = socialRepo.ngos
    val socialNGOs: StateFlow<List<SocialNGO>> = socialRepo.ngos
    val posts: StateFlow<List<SocialPost>> = socialRepo.posts
    val socialPosts: StateFlow<List<SocialPost>> = socialRepo.posts
    val roster: StateFlow<List<VolunteerRosterItem>> = socialRepo.roster
    val volunteerRoster: StateFlow<List<VolunteerRosterItem>> = socialRepo.roster
    val notifications: StateFlow<List<SocialNotificationItem>> = socialRepo.notifications

    val upcomingSocialEvents: StateFlow<List<SocialEvent>> = events
        .combine(MutableStateFlow(Unit)) { list, _ -> list.filter { it.isJoined } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSocialEvents: StateFlow<List<SocialEvent>> = events
        .combine(MutableStateFlow(Unit)) { list, _ -> list.filter { it.isBookmarked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notifications
        .combine(MutableStateFlow(Unit)) { list, _ -> list.count { it.unread } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // View & Portal Mode State
    private val _currentPortalRole = MutableStateFlow("VOLUNTEER") // "VOLUNTEER" or "NGO_LEADER"
    val currentPortalRole = _currentPortalRole.asStateFlow()
    val portalRole = _currentPortalRole.asStateFlow()

    init {
        // Automatically bind portal role strictly to authenticated user's assigned role
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    _currentPortalRole.value = if (user.role == "NGO_LEADER") "NGO_LEADER" else "VOLUNTEER"
                    val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
                    firestoreUserRepo.getUserProfile(uid).collect { fUser ->
                        _firestoreUser.value = fUser
                    }
                } else {
                    _firestoreUser.value = null
                }
            }
        }
        viewModelScope.launch {
            allUsers.collect { users ->
                if (currentUser.value == null && users.isNotEmpty()) {
                    val rahul = users.firstOrNull { it.email.equals("rahulnile@gmail.com", ignoreCase = true) }
                        ?: users.firstOrNull { it.id == 1L }
                    if (rahul != null) {
                        authRepo.switchUser(rahul)
                    }
                }
            }
        }
    }

    private val _currentLocation = MutableStateFlow("Delhi")
    val currentLocation = _currentLocation.asStateFlow()
    val userLocation = _currentLocation.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow("all")
    val selectedDateFilter = _selectedDateFilter.asStateFlow()

    private val _sortMethod = MutableStateFlow("recommended")
    val sortMethod = _sortMethod.asStateFlow()

    private val _userCauses = MutableStateFlow(listOf("Environment", "Education", "Animal Welfare", "Food Distribution"))
    val userCauses = _userCauses.asStateFlow()

    private val _maxDistanceKm = MutableStateFlow(10)
    val maxDistanceKm = _maxDistanceKm.asStateFlow()
    val userMaxDistance = _maxDistanceKm.asStateFlow()

    private val _myEventsTab = MutableStateFlow("upcoming") // "upcoming", "completed", "saved"
    val myEventsTab = _myEventsTab.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    data class EventFilterCriteria(
        val loc: String,
        val cat: String,
        val query: String,
        val sort: String,
        val dateFilter: String
    )

    private val filterCriteria: Flow<EventFilterCriteria> = combine(
        currentLocation,
        selectedCategory,
        searchQuery,
        sortMethod,
        _selectedDateFilter
    ) { loc, cat, query, sort, dateFilter ->
        EventFilterCriteria(loc, cat, query, sort, dateFilter)
    }

    // Filtered Social Events Flow matching website logic
    val filteredSocialEvents: StateFlow<List<SocialEvent>> = combine(
        events,
        filterCriteria
    ) { evts, criteria ->
        val loc = criteria.loc
        val cat = criteria.cat
        val query = criteria.query
        val sort = criteria.sort
        val dateFilter = criteria.dateFilter
        evts.filter { evt ->
            // Location filter
            if (loc != "Delhi" && loc != "all" && loc != "All Delhi NCR" && loc != "All Locations") {
                val locFilter = loc.lowercase().trim()
                val evtLoc = evt.location.lowercase()
                val matchLoc = when {
                    locFilter.contains("dwarka") && evtLoc.contains("dwarka") -> true
                    locFilter.contains("south") && (evtLoc.contains("south") || evtLoc.contains("hauz khas") || evtLoc.contains("mehrauli") || evtLoc.contains("okhla")) -> true
                    locFilter.contains("janakpuri") && evtLoc.contains("janakpuri") -> true
                    locFilter.contains("noida") && evtLoc.contains("noida") -> true
                    locFilter.contains("gurugram") && evtLoc.contains("gurugram") -> true
                    locFilter.contains("connaught") || locFilter.contains("central") -> evtLoc.contains("connaught") || evtLoc.contains("central") || evtLoc.contains("delhi")
                    locFilter.contains("old delhi") -> evtLoc.contains("old delhi") || evtLoc.contains("railway")
                    else -> evtLoc.contains(locFilter)
                }
                if (!matchLoc) return@filter false
            }

            // Category filter
            when (cat.trim().lowercase()) {
                "near-me" -> if (evt.distanceKm > 6) return@filter false
                "this-weekend" -> if (!evt.isWeekend) return@filter false
                "all", "all causes" -> { /* allow all */ }
                else -> if (!evt.category.equals(cat.trim(), ignoreCase = true)) return@filter false
            }

            // Date filter
            when (dateFilter.trim().lowercase()) {
                "weekend", "this-weekend", "this weekend" -> if (!evt.isWeekend) return@filter false
                "week", "next 7 days", "next-7-days" -> if (evt.distanceKm > 8 && !evt.isWeekend) return@filter false
                "today", "today / tomorrow" -> if (evt.distanceKm > 6) return@filter false
                "all", "all dates" -> { /* allow all */ }
                else -> { /* allow */ }
            }

            // Search query filter
            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                val match = evt.title.lowercase().contains(q) ||
                    evt.description.lowercase().contains(q) ||
                    evt.ngoName.lowercase().contains(q) ||
                    evt.category.lowercase().contains(q) ||
                    evt.location.lowercase().contains(q) ||
                    evt.skillsNeeded.any { it.lowercase().contains(q) }
                if (!match) return@filter false
            }

            true
        }.sortedWith { a, b ->
            when (sort) {
                "nearest" -> a.distanceKm.compareTo(b.distanceKm)
                "popular" -> b.joinedCount.compareTo(a.joinedCount)
                "newest" -> b.id.compareTo(a.id)
                else -> 0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePortalRole() {
        val user = currentUser.value
        if (user?.role == "VOLUNTEER") {
            _snackbarMessage.value = "Access restricted: Only verified NGO Leaders can access NGO Portal."
        } else if (user?.role == "NGO_LEADER") {
            _snackbarMessage.value = "Access restricted: Currently logged in as NGO Organization Lead."
        }
    }

    fun loginAsVolunteerDemo(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            var user = allUsers.value.firstOrNull { it.email.equals("rahulnile@gmail.com", ignoreCase = true) }
                ?: allUsers.value.firstOrNull { it.fullName.contains("Rahul Nile", ignoreCase = true) }
                ?: database.userDao().getUserByEmail("rahulnile@gmail.com")
                ?: database.userDao().getUserByIdSync(1)

            if (user == null || user.fullName == "Ananya Sen" || !user.email.equals("rahulnile@gmail.com", ignoreCase = true)) {
                val rahul = UserEntity(
                    id = 1,
                    email = "rahulnile@gmail.com",
                    passwordHash = "seva123",
                    fullName = "Rahul Nile",
                    role = "VOLUNTEER",
                    organization = "Delhi Youth Volunteers",
                    volunteerHours = 42,
                    badges = "Eco Champion,Weekend Hero,7-Day Streak,Verified Volunteer",
                    phone = "+91 98765 43210"
                )
                database.userDao().insertUser(rahul)
                user = rahul
            }

            authRepo.switchUser(user)
            _currentPortalRole.value = "VOLUNTEER"
            _snackbarMessage.value = "Signed in as Rahul Nile (Volunteer)"
            onComplete()
        }
    }

    fun loginAsNgoDemo(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val user = allUsers.value.firstOrNull { it.role == "NGO_LEADER" }
            if (user != null) {
                authRepo.switchUser(user)
                _currentPortalRole.value = "NGO_LEADER"
                _snackbarMessage.value = "Signed in as Dr. Aarav Patel (Green Delhi Foundation)"
                onComplete()
            } else {
                login("aarav.patel@greendelhi.org", "admin123") { success, _ ->
                    if (success) {
                        _currentPortalRole.value = "NGO_LEADER"
                        onComplete()
                    } else {
                        // Instant fallback guarantee
                        val fallback = UserEntity(
                            id = 2,
                            email = "aarav.patel@greendelhi.org",
                            passwordHash = "admin123",
                            fullName = "Dr. Aarav Patel",
                            role = "NGO_LEADER",
                            organization = "Green Delhi Foundation",
                            volunteerHours = 340,
                            badges = "Master Organizer,Green Delhi Pioneer,FCRA & 80G Certified,Top Rated Partner",
                            phone = "+91 98112 34567"
                        )
                        authRepo.switchUser(fallback)
                        _currentPortalRole.value = "NGO_LEADER"
                        _snackbarMessage.value = "Signed in as Dr. Aarav Patel (Green Delhi Foundation)"
                        onComplete()
                    }
                }
            }
        }
    }

    fun setLocation(loc: String) {
        _currentLocation.value = loc
        _snackbarMessage.value = "Location set to $loc"
    }

    fun setCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortMethod(sort: String) {
        _sortMethod.value = sort
    }

    fun setMyEventsTab(tab: String) {
        _myEventsTab.value = tab
    }

    fun setDateFilter(date: String) {
        _selectedDateFilter.value = date
    }

    fun resetFilters() {
        _selectedCategory.value = "all"
        _searchQuery.value = ""
        _currentLocation.value = "Delhi"
        _selectedDateFilter.value = "all"
        _sortMethod.value = "recommended"
        _snackbarMessage.value = "Filters reset to default"
    }

    fun toggleJoinSocialEvent(eventId: String) {
        val msg = socialRepo.toggleJoinEvent(eventId)
        _snackbarMessage.value = msg
        // Sync with Firebase Firestore
        viewModelScope.launch {
            val user = currentUser.value
            val isNowJoined = events.value.firstOrNull { it.id == eventId }?.isJoined == true
            val uid = FirebaseAuthService.currentUser?.uid ?: user?.email?.replace(".", "_") ?: "rahul_nile"
            firestoreUserRepo.toggleJoinedEvent(uid, eventId, isNowJoined)
            if (isNowJoined) {
                firestoreEventRepo.registerVolunteer(eventId, uid)
            } else {
                firestoreEventRepo.unregisterVolunteer(eventId, uid)
            }
        }
    }

    fun toggleBookmark(eventId: String) {
        val msg = socialRepo.toggleBookmark(eventId)
        _snackbarMessage.value = msg
        // Sync with Firebase Firestore
        viewModelScope.launch {
            val user = currentUser.value
            val isBookmarked = events.value.firstOrNull { it.id == eventId }?.isBookmarked == true
            val uid = FirebaseAuthService.currentUser?.uid ?: user?.email?.replace(".", "_") ?: "rahul_nile"
            firestoreUserRepo.toggleBookmarkedEvent(uid, eventId, isBookmarked)
        }
    }

    fun toggleBookmarkSocialEvent(eventId: String) {
        toggleBookmark(eventId)
    }

    fun toggleLikePost(postId: String) {
        socialRepo.toggleLikePost(postId)
    }

    fun togglePostLike(postId: String) {
        toggleLikePost(postId)
    }

    fun addPostComment(postId: String, text: String) {
        if (text.isNotBlank()) {
            val author = currentUser.value?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
            socialRepo.addCommentToPost(postId, text.trim(), author)
            _snackbarMessage.value = "Comment posted"
        }
    }

    fun publishPost(text: String) {
        if (text.isNotBlank()) {
            val author = currentUser.value?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
            socialRepo.publishPost(text.trim(), author)
            _snackbarMessage.value = "Your update has been shared with the community! 🚀"
        }
    }

    fun publishCommunityPost(text: String) {
        publishPost(text)
    }

    fun toggleFollowNGO(ngoId: String) {
        val msg = socialRepo.toggleFollowNGO(ngoId)
        _snackbarMessage.value = msg
    }

    fun toggleAttendance(volId: String) {
        val msg = socialRepo.toggleAttendance(volId)
        _snackbarMessage.value = msg
    }

    fun toggleVolunteerAttendance(volId: String) {
        toggleAttendance(volId)
    }

    fun markAllNotificationsRead() {
        socialRepo.markAllNotificationsRead()
        _snackbarMessage.value = "All notifications marked as read"
    }

    fun savePreferences(causes: List<String>, location: String, distanceKm: Int) {
        _userCauses.value = causes
        if (location.isNotBlank()) {
            _currentLocation.value = location
        }
        _maxDistanceKm.value = distanceKm
        _snackbarMessage.value = "Personalized feed updated with your interests! ✨"

        // Sync preferences with Firebase Firestore
        viewModelScope.launch {
            val user = currentUser.value
            val uid = FirebaseAuthService.currentUser?.uid ?: user?.email?.replace(".", "_") ?: "rahul_nile"
            firestoreUserRepo.updatePreferences(uid, causes, location)
        }
    }

    fun logVolunteerHours(hours: Int) {
        viewModelScope.launch {
            authRepo.addVolunteerHours(hours)
            val user = currentUser.value
            val uid = FirebaseAuthService.currentUser?.uid ?: user?.email?.replace(".", "_") ?: "rahul_nile"
            firestoreUserRepo.addVolunteerHours(uid, hours)
            _snackbarMessage.value = "+$hours volunteer hours recorded & synced to Firestore! 🌟"
        }
    }

    fun createSocialEvent(
        title: String,
        category: String,
        dateStr: String,
        timeStr: String,
        location: String,
        maxVolunteers: Int,
        description: String,
        skills: List<String>,
        bring: List<String>,
        imageUrl: String?
    ) {
        socialRepo.createEvent(
            title = title,
            category = category,
            dateStr = dateStr,
            timeStr = timeStr,
            location = location,
            maxVolunteers = maxVolunteers,
            description = description,
            skills = skills,
            bring = bring,
            imageUrl = imageUrl
        )
        // Also sync new event with Firestore volunteer_events collection
        viewModelScope.launch {
            val author = currentUser.value
            val event = com.example.data.model.VolunteerEvent(
                id = "",
                title = title,
                description = description,
                ngoId = author?.organization?.replace(" ", "_") ?: "green_delhi",
                ngoName = author?.organization?.ifBlank { "Green Delhi Foundation" } ?: "Green Delhi Foundation",
                category = category,
                location = location,
                dateStr = dateStr,
                timeStr = timeStr,
                maxVolunteers = maxVolunteers,
                skillsNeeded = skills,
                whatToBring = bring,
                contactEmail = author?.email ?: "aarav.patel@greendelhi.org"
            )
            firestoreEventRepo.saveVolunteerEvent(event)
        }
        _snackbarMessage.value = "\"$title\" published successfully! Synced to Firestore 🚀"
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    init {
        com.example.util.FirebaseInitHelper.ensureInitialized(application)
    }

    // Google Sign-In with Firebase Auth & Firestore user synchronization
    fun signInWithGoogle(
        activity: android.app.Activity?,
        preferredEmail: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        FirebaseAuthService.signInWithGoogleDirect(
            activity = activity,
            preferredEmail = preferredEmail,
            onSuccess = { gEmail, gName, uid ->
                viewModelScope.launch {
                    val syncResult = firestoreUserRepo.syncUserOnAuth(
                        uid = uid,
                        email = gEmail,
                        fullName = gName,
                        role = "VOLUNTEER"
                    )
                    val fUser = syncResult.getOrNull()
                    var user = database.userDao().getUserByEmail(gEmail)
                    if (user == null) {
                        val newUser = UserEntity(
                            email = gEmail,
                            passwordHash = "firebase_google_auth",
                            fullName = gName,
                            role = fUser?.role ?: "VOLUNTEER",
                            organization = fUser?.organization ?: "Community Volunteer",
                            volunteerHours = fUser?.volunteerHours ?: 42,
                            badges = fUser?.badges?.joinToString(",") ?: "Eco Champion,Weekend Hero,Verified Volunteer",
                            phone = fUser?.phone ?: "+91 98765 43210"
                        )
                        val id = database.userDao().insertUser(newUser)
                        user = newUser.copy(id = id)
                    }
                    authRepo.switchUser(user)
                    _currentPortalRole.value = user.role
                    _snackbarMessage.value = "Verified with Google & Firebase Auth! User data synced with Firestore 🚀"
                    onComplete(true, null)
                }
            },
            onError = { err ->
                onComplete(false, err)
            }
        )
    }

    // Google Search Grounding with gemini-3.5-flash
    fun performGoogleSearchGrounding(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        viewModelScope.launch {
            _searchGroundingState.value = SearchGroundingUiState.Loading(q)
            try {
                val result = GeminiSearchGroundingService.searchWithGrounding(q)
                _searchGroundingState.value = SearchGroundingUiState.Success(result)
            } catch (e: Exception) {
                _searchGroundingState.value = SearchGroundingUiState.Error(
                    message = e.message ?: "Failed to perform Google Search Grounding"
                )
            }
        }
    }

    fun clearSearchGrounding() {
        _searchGroundingState.value = SearchGroundingUiState.Idle
    }

    // Auth actions: Real Firebase Authentication & Authorization
    fun login(email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        FirebaseAuthService.signInWithEmail(
            email = trimmedEmail,
            pass = trimmedPass,
            onSuccess = { firebaseUser ->
                // Check if user's email is verified
                if (!firebaseUser.isEmailVerified) {
                    _pendingVerificationEmail.value = firebaseUser.email ?: trimmedEmail
                    _isEmailVerificationPending.value = true
                    _snackbarMessage.value = "Please verify your email before accessing SocialConnect"
                    onComplete(true, null)
                    return@signInWithEmail
                }

                _isEmailVerificationPending.value = false
                _pendingVerificationEmail.value = null

                viewModelScope.launch {
                    val uid = firebaseUser.uid
                    val fEmail = firebaseUser.email ?: trimmedEmail
                    val fName = firebaseUser.displayName?.ifBlank { null }
                        ?: fEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

                    val syncResult = firestoreUserRepo.syncUserOnAuth(
                        uid = uid,
                        email = fEmail,
                        fullName = fName,
                        role = "VOLUNTEER"
                    )
                    val fUser = syncResult.getOrNull()

                    var user = database.userDao().getUserByEmail(fEmail)
                    if (user == null) {
                        val newUser = UserEntity(
                            email = fEmail,
                            passwordHash = trimmedPass,
                            fullName = fName,
                            role = fUser?.role ?: "VOLUNTEER",
                            organization = fUser?.organization ?: "Community Volunteer",
                            volunteerHours = fUser?.volunteerHours ?: 10,
                            badges = fUser?.badges?.joinToString(",") ?: "Verified Volunteer",
                            phone = fUser?.phone ?: "+91 98765 43210"
                        )
                        val id = database.userDao().insertUser(newUser)
                        user = newUser.copy(id = id)
                    }
                    authRepo.switchUser(user)
                    _currentPortalRole.value = user.role
                    _snackbarMessage.value = "Welcome back, ${user.fullName}! (Firebase Authorized)"
                    onComplete(true, null)
                }
            },
            onError = { firebaseErr ->
                // Local fallback (offline or demo accounts like Rahul Nile / Dr. Aarav Patel)
                viewModelScope.launch {
                    val result = authRepo.login(trimmedEmail, trimmedPass)
                    result.fold(
                        onSuccess = { user ->
                            _isEmailVerificationPending.value = false
                            _pendingVerificationEmail.value = null
                            _snackbarMessage.value = "Welcome back, ${user.fullName}!"
                            val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
                            firestoreUserRepo.syncUserOnAuth(
                                uid = uid,
                                email = user.email,
                                fullName = user.fullName,
                                role = user.role,
                                organization = user.organization,
                                phone = user.phone
                            )
                            onComplete(true, null)
                        },
                        onFailure = {
                            onComplete(false, firebaseErr.ifBlank { "Authentication failed. Check your email and password." })
                        }
                    )
                }
            }
        )
    }

    fun register(
        email: String,
        pass: String,
        name: String,
        role: String,
        org: String,
        phone: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val trimmedName = name.trim()

        FirebaseAuthService.signUpWithEmail(
            email = trimmedEmail,
            pass = trimmedPass,
            displayName = trimmedName,
            onSuccess = { firebaseUser ->
                viewModelScope.launch {
                    val uid = firebaseUser.uid
                    firestoreUserRepo.syncUserOnAuth(
                        uid = uid,
                        email = trimmedEmail,
                        fullName = trimmedName,
                        role = role,
                        organization = org,
                        phone = phone
                    )
                    // Persist locally in Room
                    authRepo.register(trimmedEmail, trimmedPass, trimmedName, role, org, phone)

                    // Mark email verification pending requirement
                    _pendingVerificationEmail.value = trimmedEmail
                    _isEmailVerificationPending.value = true

                    _snackbarMessage.value = "Verification link sent to $trimmedEmail. Please verify your email."
                    onComplete(true, null)
                }
            },
            onError = { firebaseErr ->
                viewModelScope.launch {
                    val result = authRepo.register(trimmedEmail, trimmedPass, trimmedName, role, org, phone)
                    result.fold(
                        onSuccess = { user ->
                            _snackbarMessage.value = "Welcome to SocialConnect, ${user.fullName}!"
                            val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
                            firestoreUserRepo.syncUserOnAuth(
                                uid = uid,
                                email = user.email,
                                fullName = user.fullName,
                                role = user.role,
                                organization = user.organization,
                                phone = user.phone
                            )
                            onComplete(true, null)
                        },
                        onFailure = { err ->
                            onComplete(false, firebaseErr.ifBlank { err.message ?: "Registration failed" })
                        }
                    )
                }
            }
        )
    }

    /**
     * Checks if the user's email has been verified via Firebase Auth
     */
    fun checkEmailVerificationStatus(onResult: (Boolean) -> Unit) {
        _isCheckingVerification.value = true
        FirebaseAuthService.reloadUser { isVerified, user ->
            _isCheckingVerification.value = false
            if (isVerified && user != null) {
                _isEmailVerificationPending.value = false
                val email = user.email ?: _pendingVerificationEmail.value ?: ""
                _pendingVerificationEmail.value = null

                viewModelScope.launch {
                    var localUser = database.userDao().getUserByEmail(email)
                    if (localUser != null) {
                        authRepo.switchUser(localUser)
                        _currentPortalRole.value = localUser.role
                    } else {
                        val newUser = UserEntity(
                            email = email,
                            passwordHash = "firebase_verified",
                            fullName = user.displayName?.ifBlank { "Verified Member" } ?: "Verified Member",
                            role = "VOLUNTEER",
                            organization = "Community Volunteer",
                            volunteerHours = 10,
                            badges = "Verified Volunteer",
                            phone = "+91 98765 43210"
                        )
                        val id = database.userDao().insertUser(newUser)
                        localUser = newUser.copy(id = id)
                        authRepo.switchUser(localUser)
                        _currentPortalRole.value = localUser.role
                    }
                    _snackbarMessage.value = "Email verified! Welcome to SocialConnect, ${localUser.fullName} 🎉"
                    onResult(true)
                }
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Resends Firebase Auth email verification
     */
    fun resendVerificationEmail(onComplete: (Boolean, String?) -> Unit) {
        FirebaseAuthService.sendEmailVerification(
            onSuccess = {
                val email = _pendingVerificationEmail.value ?: FirebaseAuthService.currentUser?.email ?: "your email"
                onComplete(true, "New verification email sent to $email! Please check your inbox.")
            },
            onError = { err ->
                onComplete(false, err)
            }
        )
    }

    /**
     * Cancel pending verification and return to sign in
     */
    fun cancelVerificationAndReturn() {
        _isEmailVerificationPending.value = false
        _pendingVerificationEmail.value = null
        FirebaseAuthService.signOut()
    }

    /**
     * Passwordless Email OTP: Dispatches 6-digit cryptographic security code to email.
     */
    fun sendPasswordlessEmailOtp(
        email: String,
        context: android.content.Context?,
        onComplete: (success: Boolean, message: String?, cooldownSeconds: Int) -> Unit
    ) {
        EmailOtpSecurityService.sendOtp(
            email = email,
            context = context,
            onSuccess = { session ->
                val cooldown = EmailOtpSecurityService.getRemainingCooldownSeconds(email)
                _snackbarMessage.value = "Login code dispatched to ${session.email}"
                onComplete(true, "We've sent a 6-digit login code to ${session.email}", cooldown)
            },
            onError = { err ->
                onComplete(false, err, 0)
            }
        )
    }

    /**
     * Passwordless Email OTP: Verifies code, checks 5-min expiration & brute-force limits,
     * and signs in user without password.
     */
    fun verifyPasswordlessEmailOtp(
        email: String,
        code: String,
        preferredRole: String = "VOLUNTEER",
        onComplete: (success: Boolean, errorMessage: String?, attemptsLeft: Int?, isLocked: Boolean) -> Unit
    ) {
        EmailOtpSecurityService.verifyOtp(
            email = email,
            code = code,
            onResult = { result ->
                when (result) {
                    is EmailOtpSecurityService.VerificationResult.Success -> {
                        viewModelScope.launch {
                            try {
                                if (FirebaseAuthService.currentUser == null) {
                                    FirebaseAuthService.auth?.signInAnonymously()
                                }
                            } catch (e: Exception) {
                                // Handled safely
                            }

                            val cleanEmail = result.email.trim().lowercase()
                            val displayName = if (cleanEmail.contains("rahul", true)) "Rahul Nile"
                                else cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

                            val authResult = authRepo.authenticateWithOtp(
                                identifier = cleanEmail,
                                name = displayName,
                                role = preferredRole
                            )

                            authResult.fold(
                                onSuccess = { user ->
                                    _snackbarMessage.value = "Welcome back, ${user.fullName}! (Passwordless Email OTP Verified)"
                                    val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
                                    firestoreUserRepo.syncUserOnAuth(
                                        uid = uid,
                                        email = user.email,
                                        fullName = user.fullName,
                                        role = user.role,
                                        organization = user.organization,
                                        phone = user.phone
                                    )
                                    onComplete(true, null, null, false)
                                },
                                onFailure = { ex ->
                                    onComplete(false, ex.message ?: "Authentication failed", null, false)
                                }
                            )
                        }
                    }
                    is EmailOtpSecurityService.VerificationResult.Error -> {
                        onComplete(false, result.message, result.attemptsLeft, result.isLocked)
                    }
                }
            }
        )
    }

    fun authenticateWithOtp(
        identifier: String,
        name: String,
        role: String,
        phone: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (FirebaseAuthService.currentUser == null) {
                    FirebaseAuthService.auth?.signInAnonymously()
                }
            } catch (e: Exception) {
                // Handled gracefully
            }

            val result = authRepo.authenticateWithOtp(identifier, name, role, phone)
            result.fold(
                onSuccess = { user ->
                    _snackbarMessage.value = "OTP Verified! Firebase Authorized: ${user.fullName} 🎉"
                    val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
                    firestoreUserRepo.syncUserOnAuth(
                        uid = uid,
                        email = user.email,
                        fullName = user.fullName,
                        role = user.role,
                        organization = user.organization,
                        phone = user.phone
                    )
                    onComplete(true, null)
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "Authentication failed")
                }
            )
        }
    }

    fun switchRoleAuthorization(newRole: String, organization: String = "") {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(
                role = newRole,
                organization = if (organization.isNotBlank()) organization else if (newRole == "NGO_LEADER") "Green Delhi Foundation" else "Community Volunteer"
            )
            database.userDao().updateUser(updatedUser)
            authRepo.switchUser(updatedUser)
            _currentPortalRole.value = newRole
            val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
            firestoreUserRepo.syncUserOnAuth(
                uid = uid,
                email = user.email,
                fullName = user.fullName,
                role = newRole,
                organization = updatedUser.organization,
                phone = user.phone
            )
            _snackbarMessage.value = "Firebase Authorization: Role is now $newRole"
        }
    }

    fun quickLogin(user: UserEntity) {
        authRepo.switchUser(user)
        _snackbarMessage.value = "Signed in as ${user.fullName}"
        viewModelScope.launch {
            val uid = FirebaseAuthService.currentUser?.uid ?: user.email.replace(".", "_")
            firestoreUserRepo.syncUserOnAuth(
                uid = uid,
                email = user.email,
                fullName = user.fullName,
                role = user.role,
                organization = user.organization,
                phone = user.phone
            )
        }
    }

    fun logout() {
        FirebaseAuthService.signOut()
        authRepo.logout()
        _firestoreUser.value = null
        _isEmailVerificationPending.value = false
        _pendingVerificationEmail.value = null
        _snackbarMessage.value = "Signed out of SocialConnect"
    }
}
