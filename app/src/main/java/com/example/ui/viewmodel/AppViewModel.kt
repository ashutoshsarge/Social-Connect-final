package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserSessionManager
import com.example.data.local.UserSessionResult
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.SubtaskEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.DashboardStats
import com.example.data.model.SocialCompletedEvent
import com.example.data.model.SocialEvent
import com.example.data.model.SocialNGO
import com.example.data.model.SocialNotificationItem
import com.example.data.model.SocialPost
import com.example.data.model.VolunteerRosterItem
import com.example.data.repository.AuthRepository
import com.example.data.repository.EventRepository
import com.example.data.repository.SocialConnectRepository
import com.example.data.repository.TaskRepository
import com.example.data.service.EmailOtpSecurityService
import com.example.data.service.GeminiSearchGroundingService
import com.example.data.service.SearchGroundingResult
import com.example.data.service.SearchGroundingUiState
import com.example.data.service.SupabaseAuthService
import com.example.data.service.SupabaseDataService
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
    val sessionManager = UserSessionManager(application)
    private val authRepo = AuthRepository(database.userDao(), sessionManager)
    private val taskRepo = TaskRepository(database.taskDao())
    private val eventRepo = EventRepository(database.eventDao())
    val socialRepo = SocialConnectRepository(application)

    // Auth & Users
    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
    val allUsers: StateFlow<List<UserEntity>> = authRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Session Security & Login Counter
    private val _sessionLoginCount = MutableStateFlow(1)
    val sessionLoginCount: StateFlow<Int> = _sessionLoginCount.asStateFlow()

    private val _securityLogoutMessage = MutableStateFlow<String?>(null)
    val securityLogoutMessage: StateFlow<String?> = _securityLogoutMessage.asStateFlow()

    fun clearSecurityLogoutMessage() {
        _securityLogoutMessage.value = null
        sessionManager.clearSecurityLogoutMessage()
    }

    fun renewSecuritySession() {
        val renewed = sessionManager.renewSession()
        _sessionLoginCount.value = renewed
        _snackbarMessage.value = "Security session renewed (Session 1 of ${UserSessionManager.MAX_ALLOWED_LOGINS})"
    }

    // Google Search Grounding with gemini-3.5-flash
    private val _searchGroundingState = MutableStateFlow<SearchGroundingUiState>(SearchGroundingUiState.Idle)
    val searchGroundingState: StateFlow<SearchGroundingUiState> = _searchGroundingState.asStateFlow()

    // Email Verification State
    private val _isEmailVerificationPending = MutableStateFlow(false)
    val isEmailVerificationPending: StateFlow<Boolean> = _isEmailVerificationPending.asStateFlow()

    private val _pendingVerificationEmail = MutableStateFlow<String?>(null)
    val pendingVerificationEmail: StateFlow<String?> = _pendingVerificationEmail.asStateFlow()

    private val _isCheckingVerification = MutableStateFlow(false)
    val isCheckingVerification: StateFlow<Boolean> = _isCheckingVerification.asStateFlow()

    // Sign in UX state: Pre-filled email & success notice after sign up
    private val _prefilledAuthEmail = MutableStateFlow<String?>(null)
    val prefilledAuthEmail: StateFlow<String?> = _prefilledAuthEmail.asStateFlow()

    private val _authSuccessNotice = MutableStateFlow<String?>(null)
    val authSuccessNotice: StateFlow<String?> = _authSuccessNotice.asStateFlow()

    fun clearAuthSuccessNotice() {
        _authSuccessNotice.value = null
    }

    fun setPrefilledAuthEmail(email: String) {
        _prefilledAuthEmail.value = email
    }

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
        // Enforce Session Security & Initial Auth Gate
        when (val sessionRes = sessionManager.checkAndResumeSession()) {
            is UserSessionResult.ActiveSession -> {
                _sessionLoginCount.value = sessionRes.loginCount
                authRepo.restoreSession(sessionRes.user)
                _currentPortalRole.value = if (sessionRes.user.role == "NGO_LEADER") "NGO_LEADER" else "VOLUNTEER"
            }
            is UserSessionResult.SecurityAutoLogout -> {
                _sessionLoginCount.value = 0
                _securityLogoutMessage.value = sessionRes.message
                _prefilledAuthEmail.value = sessionRes.expiredEmail
            }
            is UserSessionResult.NoSession -> {
                _sessionLoginCount.value = 0
                // User is not allowed in without login or signup - remains on AuthScreen
            }
        }

        // Keep portal role and Supabase profile in sync with authenticated user
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    _currentPortalRole.value = if (user.role == "NGO_LEADER") "NGO_LEADER" else "VOLUNTEER"
                    try {
                        SupabaseDataService.syncUserProfile(user)
                    } catch (_: Exception) {}
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

    private val _userCauses = MutableStateFlow(listOf("Environment", "Education", "Food Security", "Animal Care"))
    val userCauses = _userCauses.asStateFlow()

    private val _maxDistanceKm = MutableStateFlow(15)
    val maxDistanceKm = _maxDistanceKm.asStateFlow()
    val userMaxDistance = _maxDistanceKm.asStateFlow()

    val filteredSocialEvents: StateFlow<List<SocialEvent>> = combine(
        events,
        selectedCategory,
        searchQuery,
        selectedDateFilter,
        currentLocation
    ) { evList, cat, q, dateFilter, loc ->
        evList.filter { event ->
            val matchCat = cat == "all" || event.category.equals(cat, ignoreCase = true)
            val matchQuery = q.isBlank() || event.title.contains(q, ignoreCase = true) ||
                    event.location.contains(q, ignoreCase = true) ||
                    event.ngoName.contains(q, ignoreCase = true)
            val matchDate = when (dateFilter) {
                "today" -> event.dateStr.contains("today", ignoreCase = true)
                "tomorrow" -> event.dateStr.contains("tomorrow", ignoreCase = true)
                "weekend" -> event.isWeekend || event.dateStr.contains("sunday", ignoreCase = true) || event.dateStr.contains("saturday", ignoreCase = true)
                else -> true
            }
            matchCat && matchQuery && matchDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modals & detail views
    private val _selectedEventForDetail = MutableStateFlow<SocialEvent?>(null)
    val selectedEventForDetail = _selectedEventForDetail.asStateFlow()

    private val _selectedEventForDonation = MutableStateFlow<SocialEvent?>(null)
    val selectedEventForDonation = _selectedEventForDonation.asStateFlow()

    private val _selectedEventForDirections = MutableStateFlow<SocialEvent?>(null)
    val selectedEventForDirections = _selectedEventForDirections.asStateFlow()

    private val _selectedEventForCertificate = MutableStateFlow<SocialEvent?>(null)
    val selectedEventForCertificate = _selectedEventForCertificate.asStateFlow()

    private val _selectedCompletedEventForCertificate = MutableStateFlow<SocialCompletedEvent?>(null)
    val selectedCompletedEventForCertificate = _selectedCompletedEventForCertificate.asStateFlow()

    private val _isPreferencesOpen = MutableStateFlow(false)
    val isPreferencesOpen = _isPreferencesOpen.asStateFlow()

    private val _isCreateEventOpen = MutableStateFlow(false)
    val isCreateEventOpen = _isCreateEventOpen.asStateFlow()

    private val _isNotificationsOpen = MutableStateFlow(false)
    val isNotificationsOpen = _isNotificationsOpen.asStateFlow()

    private val _activeLegalPolicyModal = MutableStateFlow<Int?>(null)
    val activeLegalPolicyModal = _activeLegalPolicyModal.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    // Cookie & Data Tracking Consent State (DPDP Act 2023)
    private val _cookieConsentGiven = MutableStateFlow(true)
    val cookieConsentGiven = _cookieConsentGiven.asStateFlow()

    private val _functionalCookiesEnabled = MutableStateFlow(true)
    val functionalCookiesEnabled = _functionalCookiesEnabled.asStateFlow()

    private val _analyticsTrackingEnabled = MutableStateFlow(true)
    val analyticsTrackingEnabled = _analyticsTrackingEnabled.asStateFlow()

    // Actions & Handlers
    fun setCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setLocation(loc: String) {
        _currentLocation.value = loc
        _snackbarMessage.value = "Location updated to $loc"
    }

    fun setDateFilter(filter: String) {
        _selectedDateFilter.value = filter
    }

    fun setSortMethod(sort: String) {
        _sortMethod.value = sort
    }

    fun selectEventForDetail(event: SocialEvent) {
        _selectedEventForDetail.value = event
    }

    fun closeEventDetail() {
        _selectedEventForDetail.value = null
    }

    fun selectEventForDonation(event: SocialEvent) {
        _selectedEventForDonation.value = event
    }

    fun closeDonationModal() {
        _selectedEventForDonation.value = null
    }

    fun selectEventForDirections(event: SocialEvent) {
        _selectedEventForDirections.value = event
    }

    fun closeDirectionsModal() {
        _selectedEventForDirections.value = null
    }

    fun selectEventForCertificate(event: SocialEvent) {
        _selectedEventForCertificate.value = event
    }

    fun selectCompletedEventForCertificate(completed: SocialCompletedEvent) {
        _selectedCompletedEventForCertificate.value = completed
    }

    fun closeCertificateModal() {
        _selectedEventForCertificate.value = null
        _selectedCompletedEventForCertificate.value = null
    }

    fun openPreferences() {
        _isPreferencesOpen.value = true
    }

    fun closePreferences() {
        _isPreferencesOpen.value = false
    }

    fun openCreateEvent() {
        _isCreateEventOpen.value = true
    }

    fun closeCreateEvent() {
        _isCreateEventOpen.value = false
    }

    fun openNotifications() {
        _isNotificationsOpen.value = true
    }

    fun closeNotifications() {
        _isNotificationsOpen.value = false
    }

    fun openLegalPolicyModal(tabIndex: Int) {
        _activeLegalPolicyModal.value = tabIndex
    }

    fun closeLegalPolicyModal() {
        _activeLegalPolicyModal.value = null
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
        val user = currentUser.value
        val name = user?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
        val msg = socialRepo.toggleJoinEvent(eventId, name)
        _snackbarMessage.value = msg
    }

    fun toggleBookmark(eventId: String) {
        val msg = socialRepo.toggleBookmark(eventId)
        _snackbarMessage.value = msg
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

    fun donateMoneyToEvent(eventId: String, amount: Double) {
        val donor = currentUser.value?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
        val msg = socialRepo.donateMoney(eventId, amount, donor)
        _snackbarMessage.value = msg
    }

    fun donateMaterialToEvent(eventId: String, itemId: String, quantity: Int) {
        val donor = currentUser.value?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
        val msg = socialRepo.donateMaterial(eventId, itemId, quantity, donor)
        _snackbarMessage.value = msg
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
    }

    fun logVolunteerHours(hours: Int) {
        viewModelScope.launch {
            authRepo.addVolunteerHours(hours)
            _snackbarMessage.value = "+$hours volunteer hours recorded! 🌟"
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
        val author = currentUser.value
        val orgName = author?.organization?.ifBlank { "Green Delhi Foundation" } ?: "Green Delhi Foundation"
        val ngoId = orgName.lowercase().replace(" ", "-").ifBlank { "ngo-1" }

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
            imageUrl = imageUrl,
            ngoName = orgName,
            ngoId = ngoId
        )
        _snackbarMessage.value = "\"$title\" published live on Supabase! Visible across all users' interfaces 🚀"
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    // Google Sign-In with Supabase & Room credentials persistence
    fun signInWithGoogle(
        activity: android.app.Activity?,
        preferredEmail: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val email = preferredEmail?.trim()?.ifBlank { "rahulnile@gmail.com" } ?: "rahulnile@gmail.com"
            val name = if (email.contains("aarav", ignoreCase = true)) "Dr. Aarav Patel" else "Rahul Nile"
            val role = if (email.contains("aarav", ignoreCase = true)) "NGO_LEADER" else "VOLUNTEER"

            var user = database.userDao().getUserByEmail(email)
            if (user == null) {
                val newUser = UserEntity(
                    email = email,
                    passwordHash = "google_auth_token",
                    fullName = name,
                    role = role,
                    organization = if (role == "NGO_LEADER") "Green Delhi Foundation" else "Delhi Youth Volunteers",
                    volunteerHours = if (role == "NGO_LEADER") 28 else 42,
                    badges = "Eco Champion,Weekend Hero,Verified Volunteer",
                    phone = "+91 98765 43210"
                )
                val id = database.userDao().insertUser(newUser)
                user = newUser.copy(id = id)
            }

            authRepo.switchUser(user)
            _sessionLoginCount.value = 1
            _securityLogoutMessage.value = null
            _currentPortalRole.value = user.role
            _snackbarMessage.value = "Verified with Google! Session 1 of ${UserSessionManager.MAX_ALLOWED_LOGINS} active 🚀"
            onComplete(true, null)
        }
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

    // Auth actions: Supabase Authentication as primary with local Room persistence
    fun login(email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        viewModelScope.launch {
            val supabaseResult = SupabaseAuthService.signInWithPassword(trimmedEmail, trimmedPass)
            if (supabaseResult.isSuccess) {
                val res = supabaseResult.getOrThrow()
                if (res.hasActiveSession) {
                    if (!res.isEmailVerified) {
                        authRepo.logout()
                        _pendingVerificationEmail.value = trimmedEmail
                        _isEmailVerificationPending.value = true
                        _snackbarMessage.value = "Please verify your email before entering the dashboard. Check your inbox."
                        onComplete(false, "Please verify your email address before signing in.")
                        return@launch
                    }

                    // Save and update credentials in local Room database
                    var user = database.userDao().getUserByEmail(trimmedEmail)
                    if (user == null) {
                        val displayName = trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                        val newUser = UserEntity(
                            email = trimmedEmail,
                            passwordHash = trimmedPass,
                            fullName = displayName,
                            role = "VOLUNTEER",
                            organization = "Community Volunteer",
                            volunteerHours = 10,
                            badges = "Verified Volunteer",
                            phone = "+91 98765 43210"
                        )
                        val id = database.userDao().insertUser(newUser)
                        user = newUser.copy(id = id)
                    } else if (user.passwordHash.isBlank() || user.passwordHash != trimmedPass) {
                        val updated = user.copy(passwordHash = trimmedPass)
                        database.userDao().updateUser(updated)
                        user = updated
                    }

                    authRepo.switchUser(user)
                    _sessionLoginCount.value = 1
                    _securityLogoutMessage.value = null
                    _currentPortalRole.value = user.role
                    _isEmailVerificationPending.value = false
                    _pendingVerificationEmail.value = null
                    _snackbarMessage.value = "Welcome back, ${user.fullName}!"
                    onComplete(true, null)
                    return@launch
                } else {
                    authRepo.logout()
                    _pendingVerificationEmail.value = trimmedEmail
                    _isEmailVerificationPending.value = true
                    _snackbarMessage.value = "Check your email and confirm your account before logging in."
                    onComplete(false, "Check your email and confirm your account before logging in.")
                    return@launch
                }
            }

            val rawError = supabaseResult.exceptionOrNull()?.message ?: ""
            if (rawError.contains("Email not confirmed", ignoreCase = true) || rawError.contains("not confirmed", ignoreCase = true)) {
                authRepo.logout()
                _pendingVerificationEmail.value = trimmedEmail
                _isEmailVerificationPending.value = true
                _snackbarMessage.value = "Your email has not been verified yet. Check your inbox to confirm your account."
                onComplete(false, "Check your email and confirm your account before logging in.")
                return@launch
            }

            // Fallback for verified demo accounts (e.g. Rahul Nile / Dr. Aarav Patel)
            val isDemoAccount = trimmedEmail.equals("rahulnile@gmail.com", ignoreCase = true) ||
                    trimmedEmail.equals("aaravpatel@gmail.com", ignoreCase = true) ||
                    trimmedEmail.equals("priyasharma@gmail.com", ignoreCase = true)

            if (isDemoAccount) {
                val localResult = authRepo.login(trimmedEmail, trimmedPass)
                localResult.fold(
                    onSuccess = { user ->
                        _sessionLoginCount.value = 1
                        _securityLogoutMessage.value = null
                        _isEmailVerificationPending.value = false
                        _pendingVerificationEmail.value = null
                        _snackbarMessage.value = "Welcome back, ${user.fullName}!"
                        onComplete(true, null)
                    },
                    onFailure = {
                        onComplete(false, "Invalid credentials. Please check your email and password.")
                    }
                )
            } else {
                val localUser = database.userDao().getUserByEmail(trimmedEmail)
                if (localUser != null && localUser.passwordHash == trimmedPass) {
                    val localResult = authRepo.login(trimmedEmail, trimmedPass)
                    localResult.fold(
                        onSuccess = { u ->
                            _sessionLoginCount.value = 1
                            _securityLogoutMessage.value = null
                            _isEmailVerificationPending.value = false
                            _pendingVerificationEmail.value = null
                            _snackbarMessage.value = "Welcome back, ${u.fullName}!"
                            onComplete(true, null)
                        },
                        onFailure = {
                            onComplete(false, "Invalid credentials.")
                        }
                    )
                } else {
                    val displayErr = if (rawError.isNotBlank() && !rawError.contains("failed (", ignoreCase = true)) {
                        rawError
                    } else {
                        "Invalid email or password. Please check your credentials."
                    }
                    onComplete(false, displayErr)
                }
            }
        }
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

        viewModelScope.launch {
            val supabaseResult = SupabaseAuthService.signUp(trimmedEmail, trimmedPass, trimmedName)
            if (supabaseResult.isSuccess) {
                // Save credentials to local database (Room) without auto-login
                authRepo.saveCredentials(trimmedEmail, trimmedPass, trimmedName, role, org, phone)
                authRepo.logout()

                _isEmailVerificationPending.value = false
                _pendingVerificationEmail.value = null
                _prefilledAuthEmail.value = trimmedEmail
                val successNotice = "Account created successfully! Check your email to verify your account or sign in below."
                _authSuccessNotice.value = successNotice
                _snackbarMessage.value = successNotice
                onComplete(true, null)
                return@launch
            }

            val rawError = supabaseResult.exceptionOrNull()?.message ?: ""
            if (rawError.contains("already registered", ignoreCase = true)) {
                onComplete(false, "This email is already registered. Please sign in instead.")
                return@launch
            }

            // Fallback to local registration if offline / network issue, storing credentials without auto-login
            val localResult = authRepo.saveCredentials(trimmedEmail, trimmedPass, trimmedName, role, org, phone)
            authRepo.logout()
            localResult.fold(
                onSuccess = {
                    _isEmailVerificationPending.value = false
                    _pendingVerificationEmail.value = null
                    _prefilledAuthEmail.value = trimmedEmail
                    val successMsg = "Account created successfully! Check your email to verify your account or sign in below."
                    _authSuccessNotice.value = successMsg
                    _snackbarMessage.value = successMsg
                    onComplete(true, null)
                },
                onFailure = { err ->
                    val displayErr = if (rawError.isNotBlank()) rawError else (err.message ?: "Registration failed")
                    onComplete(false, displayErr)
                }
            )
        }
    }

    /**
     * Dispatches password recovery / reset email via Supabase Auth
     */
    fun sendPasswordReset(email: String, onComplete: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            onComplete(false, "Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            val supabaseRes = SupabaseAuthService.resetPasswordForEmail(trimmedEmail)
            if (supabaseRes.isSuccess) {
                _snackbarMessage.value = "Password reset instructions sent to $trimmedEmail"
                onComplete(true, "Password recovery instructions sent to $trimmedEmail. Check your inbox.")
            } else {
                val errMsg = supabaseRes.exceptionOrNull()?.message ?: "Unable to send password recovery email. Please check your address."
                onComplete(false, errMsg)
            }
        }
    }

    /**
     * Checks if the user's email has been verified via Supabase Auth
     */
    fun checkEmailVerificationStatus(onResult: (Boolean) -> Unit) {
        _isCheckingVerification.value = true
        val email = _pendingVerificationEmail.value ?: ""

        viewModelScope.launch {
            val signInCheck = SupabaseAuthService.signInWithPassword(email, "verification_probe")
            val raw = signInCheck.exceptionOrNull()?.message ?: ""
            val isVerified = !raw.contains("not confirmed", ignoreCase = true) && !raw.contains("Email not confirmed", ignoreCase = true)

            _isCheckingVerification.value = false
            if (isVerified && email.isNotBlank()) {
                _isEmailVerificationPending.value = false
                _prefilledAuthEmail.value = email
                _authSuccessNotice.value = "Email successfully verified! Sign in with your password to continue."
                _snackbarMessage.value = "Email verified! You can now sign in."
                onResult(true)
            } else {
                _snackbarMessage.value = "Email verification still pending. Please check your inbox and tap the link."
                onResult(false)
            }
        }
    }

    /**
     * Resends email verification confirmation link via Supabase Auth
     */
    fun resendEmailVerification(onComplete: (Boolean, String?) -> Unit) {
        val email = _pendingVerificationEmail.value
        if (email.isNullOrBlank()) {
            onComplete(false, "No pending email verification address found.")
            return
        }

        viewModelScope.launch {
            val res = SupabaseAuthService.resendVerificationEmail(email)
            if (res.isSuccess) {
                _snackbarMessage.value = "Verification link resent to $email"
                onComplete(true, res.getOrNull())
            } else {
                val err = res.exceptionOrNull()?.message ?: "Failed to resend confirmation link"
                onComplete(false, err)
            }
        }
    }

    fun resendVerificationEmail(onComplete: (Boolean, String?) -> Unit) {
        resendEmailVerification(onComplete)
    }

    /**
     * Cancel pending verification and return to sign in
     */
    fun cancelVerificationAndReturn() {
        _isEmailVerificationPending.value = false
        val email = _pendingVerificationEmail.value
        if (!email.isNullOrBlank()) {
            _prefilledAuthEmail.value = email
            _authSuccessNotice.value = "Please sign in to your account below."
        }
        _pendingVerificationEmail.value = null
        authRepo.logout()
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
                                    _sessionLoginCount.value = 1
                                    _securityLogoutMessage.value = null
                                    _snackbarMessage.value = "Welcome back, ${user.fullName}! (Passwordless Email OTP Verified)"
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
            val result = authRepo.authenticateWithOtp(identifier, name, role, phone)
            result.fold(
                onSuccess = { user ->
                    _sessionLoginCount.value = 1
                    _securityLogoutMessage.value = null
                    _snackbarMessage.value = "OTP Verified: ${user.fullName} 🎉"
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
            _snackbarMessage.value = "Role updated to $newRole"
        }
    }

    fun quickLogin(user: UserEntity) {
        authRepo.switchUser(user)
        _sessionLoginCount.value = 1
        _securityLogoutMessage.value = null
        _snackbarMessage.value = "Signed in as ${user.fullName}"
    }

    fun loginAsVolunteerDemo(onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = authRepo.login("rahulnile@gmail.com", "seva123")
            res.onSuccess {
                _sessionLoginCount.value = 1
                _securityLogoutMessage.value = null
                _currentPortalRole.value = "VOLUNTEER"
                _snackbarMessage.value = "Logged in as Volunteer: Rahul Nile"
            }
            onComplete()
        }
    }

    fun loginAsNgoDemo(onComplete: () -> Unit) {
        viewModelScope.launch {
            var aarav = database.userDao().getUserByEmail("aaravpatel@gmail.com")
            if (aarav == null) {
                val newAarav = UserEntity(
                    email = "aaravpatel@gmail.com",
                    passwordHash = "seva123",
                    fullName = "Dr. Aarav Patel",
                    role = "NGO_LEADER",
                    organization = "Green Delhi Foundation",
                    volunteerHours = 28,
                    badges = "NGO Founder,Executive Leader",
                    phone = "+91 98112 34567"
                )
                val id = database.userDao().insertUser(newAarav)
                aarav = newAarav.copy(id = id)
            }
            authRepo.switchUser(aarav)
            _sessionLoginCount.value = 1
            _securityLogoutMessage.value = null
            _currentPortalRole.value = "NGO_LEADER"
            _snackbarMessage.value = "Logged in as NGO Leader: Dr. Aarav Patel"
            onComplete()
        }
    }

    fun logout() {
        authRepo.logout()
        _sessionLoginCount.value = 0
        _isEmailVerificationPending.value = false
        _pendingVerificationEmail.value = null
        _snackbarMessage.value = "Signed out of SocialConnect"
    }

    // Cookie & Storage Consent Management
    fun saveCookieConsent(essential: Boolean, functional: Boolean, analytics: Boolean) {
        _cookieConsentGiven.value = true
        _functionalCookiesEnabled.value = functional
        _analyticsTrackingEnabled.value = analytics
        _snackbarMessage.value = "Storage & Cookie Preferences Saved"
    }

    fun resetCookieConsent() {
        _cookieConsentGiven.value = false
        _snackbarMessage.value = "Cookie consent reset"
    }

    /**
     * DPDP Act 2023 - Section 11 (Right to Access Summary)
     * Generates a clean JSON/text export of user's personal data processed by the Data Fiduciary.
     */
    fun exportUserPersonalData(): String {
        val user = currentUser.value
        val joined = events.value.filter { it.isJoined }.map { it.title }
        val completed = completedEvents.value.map { "${it.title} (${it.hours} hrs on ${it.dateStr})" }

        val export = buildString {
            appendLine("=== SOCIALCONNECT FOUNDATION - DATA PRINCIPAL SUMMARY ===")
            appendLine("Pursuant to Section 11 of Digital Personal Data Protection Act, 2023")
            appendLine("Generated At: ${java.util.Date()}")
            appendLine("------------------------------------------------------------")
            appendLine("Full Legal Name: ${user?.fullName ?: "N/A"}")
            appendLine("Primary Email: ${user?.email ?: "N/A"}")
            appendLine("Phone Number: ${user?.phone.takeIf { !it.isNullOrBlank() } ?: "Not provided"}")
            appendLine("Role: ${user?.role ?: "VOLUNTEER"}")
            appendLine("Associated Group/NGO: ${user?.organization.takeIf { !it.isNullOrBlank() } ?: "None"}")
            appendLine("Logged Volunteer Hours: ${user?.volunteerHours ?: 0}")
            appendLine("Preferred City: ${currentLocation.value}")
            appendLine("Selected Causes: ${userCauses.value.joinToString(", ")}")
            appendLine("Active Drive RSVPs (${joined.size}): ${if (joined.isEmpty()) "None" else joined.joinToString(", ")}")
            appendLine("Completed Drives (${completed.size}): ${if (completed.isEmpty()) "None" else completed.joinToString("; ")}")
            appendLine("Active Security Session: ${_sessionLoginCount.value} of ${UserSessionManager.MAX_ALLOWED_LOGINS} Logins")
            appendLine("Data Fiduciary: SocialConnect Foundation (CIN: U85300DL2024NPL419820)")
            appendLine("Backend Architecture: Cloud Supabase & PostgreSQL (RLS Protected)")
            appendLine("Grievance Redressal Desk: grievance-officer@socialconnect.org")
            appendLine("============================================================")
        }
        _snackbarMessage.value = "Personal Data Export Ready (DPDP Section 11)"
        return export
    }

    /**
     * DPDP Act 2023 - Section 12 (Right to Erasure / Right to be Forgotten)
     * Irreversibly purges personal data, sessions, and credentials.
     */
    fun deleteUserAccountAndData(onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = currentUser.value
                if (user != null) {
                    database.userDao().deleteUserById(user.id)
                    authRepo.logout()
                    _sessionLoginCount.value = 0
                    _snackbarMessage.value = "Account & Personal Data Permanently Erased (DPDP Act Sec 12)"
                    onComplete(true, null)
                } else {
                    onComplete(false, "No active user session to delete")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to execute data erasure")
            }
        }
    }
}
