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

    // Auth & Users
    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
    val allUsers: StateFlow<List<UserEntity>> = authRepo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // Filtered Social Events Flow matching website logic
    val filteredSocialEvents: StateFlow<List<SocialEvent>> = combine(
        events,
        currentLocation,
        selectedCategory,
        searchQuery,
        sortMethod
    ) { evts, loc, cat, query, sort ->
        evts.filter { evt ->
            // Location filter
            if (loc != "Delhi") {
                val locFilter = loc.lowercase()
                val evtLoc = evt.location.lowercase()
                val matchLoc = when {
                    locFilter.contains("dwarka") && evtLoc.contains("dwarka") -> true
                    locFilter.contains("south") && (evtLoc.contains("south") || evtLoc.contains("hauz khas") || evtLoc.contains("mehrauli") || evtLoc.contains("kalkaji")) -> true
                    locFilter.contains("janakpuri") && evtLoc.contains("janakpuri") -> true
                    locFilter.contains("noida") && evtLoc.contains("noida") -> true
                    locFilter.contains("gurugram") && evtLoc.contains("gurugram") -> true
                    else -> evtLoc.contains(locFilter)
                }
                if (!matchLoc) return@filter false
            }

            // Category filter
            when (cat) {
                "near-me" -> if (evt.distanceKm > 6) return@filter false
                "this-weekend" -> if (!evt.isWeekend) return@filter false
                "all" -> { /* allow all */ }
                else -> if (evt.category != cat) return@filter false
            }

            // Search query filter
            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                val match = evt.title.lowercase().contains(q) ||
                    evt.description.lowercase().contains(q) ||
                    evt.ngoName.lowercase().contains(q) ||
                    evt.category.lowercase().contains(q) ||
                    evt.location.lowercase().contains(q)
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
            val user = allUsers.value.firstOrNull { it.role == "VOLUNTEER" && it.email == "diyasarge@gmail.com" }
                ?: allUsers.value.firstOrNull { it.role == "VOLUNTEER" }
            if (user != null) {
                authRepo.switchUser(user)
                _currentPortalRole.value = "VOLUNTEER"
                _snackbarMessage.value = "Signed in as Diya Sarge (Volunteer)"
                onComplete()
            } else {
                login("diyasarge@gmail.com", "seva123") { success, _ ->
                    if (success) {
                        _currentPortalRole.value = "VOLUNTEER"
                        onComplete()
                    } else {
                        // Instant fallback guarantee
                        val fallback = UserEntity(
                            id = 1,
                            email = "diyasarge@gmail.com",
                            passwordHash = "seva123",
                            fullName = "Diya Sarge",
                            role = "VOLUNTEER",
                            organization = "Delhi Youth Volunteers",
                            volunteerHours = 42,
                            badges = "Eco Champion,Weekend Hero,7-Day Streak,Verified Volunteer",
                            phone = "+91 98765 43210"
                        )
                        authRepo.switchUser(fallback)
                        _currentPortalRole.value = "VOLUNTEER"
                        onComplete()
                    }
                }
            }
        }
    }

    fun loginAsNgoDemo(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val user = allUsers.value.firstOrNull { it.role == "NGO_LEADER" }
            if (user != null) {
                authRepo.switchUser(user)
                _currentPortalRole.value = "NGO_LEADER"
                _snackbarMessage.value = "Signed in as Aarav Patel (NGO Leader)"
                onComplete()
            } else {
                login("aarav@goonj.org", "admin123") { success, _ ->
                    if (success) {
                        _currentPortalRole.value = "NGO_LEADER"
                        onComplete()
                    } else {
                        // Instant fallback guarantee
                        val fallback = UserEntity(
                            id = 2,
                            email = "aarav@goonj.org",
                            passwordHash = "admin123",
                            fullName = "Aarav Patel",
                            role = "NGO_LEADER",
                            organization = "Goonj Seva Foundation",
                            volunteerHours = 120,
                            badges = "Community Leader,Master Organizer,500+ Hours",
                            phone = "+91 98223 45678"
                        )
                        authRepo.switchUser(fallback)
                        _currentPortalRole.value = "NGO_LEADER"
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

    fun resetFilters() {
        _selectedCategory.value = "all"
        _searchQuery.value = ""
        _currentLocation.value = "Delhi"
        _sortMethod.value = "recommended"
        _snackbarMessage.value = "Filters reset to default"
    }

    fun toggleJoinSocialEvent(eventId: String) {
        val msg = socialRepo.toggleJoinEvent(eventId)
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
            val author = currentUser.value?.fullName?.ifBlank { "Diya Sarge" } ?: "Diya Sarge"
            socialRepo.addCommentToPost(postId, text.trim(), author)
            _snackbarMessage.value = "Comment posted"
        }
    }

    fun publishPost(text: String) {
        if (text.isNotBlank()) {
            val author = currentUser.value?.fullName?.ifBlank { "Diya Sarge" } ?: "Diya Sarge"
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
        _snackbarMessage.value = "\"$title\" published successfully! 🚀"
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    // Auth actions
    fun login(email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepo.login(email, pass)
            result.fold(
                onSuccess = { user ->
                    _snackbarMessage.value = "Welcome back, ${user.fullName}!"
                    onComplete(true, null)
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "Authentication failed")
                }
            )
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
        viewModelScope.launch {
            val result = authRepo.register(email, pass, name, role, org, phone)
            result.fold(
                onSuccess = { user ->
                    _snackbarMessage.value = "Welcome to SocialConnect, ${user.fullName}!"
                    onComplete(true, null)
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "Registration failed")
                }
            )
        }
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
                    _snackbarMessage.value = "OTP Verified! Welcome, ${user.fullName} 🎉"
                    onComplete(true, null)
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "Authentication failed")
                }
            )
        }
    }

    fun quickLogin(user: UserEntity) {
        authRepo.switchUser(user)
        _snackbarMessage.value = "Signed in as ${user.fullName}"
    }

    fun logout() {
        authRepo.logout()
        _snackbarMessage.value = "Signed out of SocialConnect"
    }
}
