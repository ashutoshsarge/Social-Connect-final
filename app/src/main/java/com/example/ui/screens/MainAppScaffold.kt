package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialCompletedEvent
import com.example.data.model.SocialEvent
import com.example.ui.components.SocialConnectLogoCompact
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialBlue
import com.example.ui.theme.SocialCreamBorder
import com.example.ui.theme.SocialCreamCard
import com.example.ui.theme.SocialCreamSubtle
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialLogoNavy
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialWarm
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val portalRole by viewModel.portalRole.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Filtered / State flows
    val filteredEvents by viewModel.filteredSocialEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingSocialEvents.collectAsState()
    val completedEvents by viewModel.completedSocialEvents.collectAsState()
    val savedEvents by viewModel.savedSocialEvents.collectAsState()
    val ngos by viewModel.socialNGOs.collectAsState()
    val posts by viewModel.socialPosts.collectAsState()
    val roster by viewModel.volunteerRoster.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMethod by viewModel.sortMethod.collectAsState()
    val userCauses by viewModel.userCauses.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val userDistance by viewModel.userMaxDistance.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    val searchGroundingState by viewModel.searchGroundingState.collectAsState()

    // Active bottom navigation tab
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Home/Dashboard, 1: Explore, 2: My Events, 3: Community, 4: NGOs, 5: Profile
    var myEventsSubTab by remember { mutableStateOf("upcoming") }

    // Modal dialogs state
    var selectedEventForDetail by remember { mutableStateOf<SocialEvent?>(null) }
    var selectedCompletedForCert by remember { mutableStateOf<SocialCompletedEvent?>(null) }
    var selectedEventForDirections by remember { mutableStateOf<SocialEvent?>(null) }
    var showPreferencesModal by remember { mutableStateOf(false) }
    var showNotificationsModal by remember { mutableStateOf(false) }
    var showCreateEventModal by remember { mutableStateOf(false) }

    LaunchedEffect(portalRole) {
        selectedTab = 0
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + Brand (Website Navbar)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedTab = 0 }
                    ) {
                        SocialConnectLogoCompact(emblemSize = 32.dp)
                    }

                    // Right Actions: Strict Role Badge + Notifications + Profile Avatar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Strict Role Indicator Badge (No unauthorized toggle)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (portalRole == "VOLUNTEER") Color(0xFFDCFCE7) else Color(0xFFCCFBF1),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (portalRole == "VOLUNTEER") Color(0xFF86EFAC) else Color(0xFF99F6E4)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (portalRole == "VOLUNTEER") Icons.Default.VolunteerActivism else Icons.Default.Business,
                                    contentDescription = if (portalRole == "VOLUNTEER") "Volunteer" else "NGO Admin",
                                    tint = if (portalRole == "VOLUNTEER") Color(0xFF15803D) else Color(0xFF0F766E),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (portalRole == "VOLUNTEER") "Volunteer" else "NGO Admin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (portalRole == "VOLUNTEER") Color(0xFF15803D) else Color(0xFF0F766E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Notification Icon with Badge
                        IconButton(
                            onClick = { showNotificationsModal = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFEF4444),
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "$unreadCount",
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = SocialTextMain,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // User Avatar with initials fallback
                        val userDisplayName = currentUser?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
                        val initials = userDisplayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF064E3B), Color(0xFF059669))
                                    )
                                )
                                .border(1.5.dp, SocialGreen, CircleShape)
                                .clickable { selectedTab = 4 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (initials.isNotEmpty()) initials else "RN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE8E2D5),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (portalRole == "VOLUNTEER") {
                    // Volunteer Tab 0: Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Volunteer Tab 1: Explore
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.Explore else Icons.Outlined.Explore,
                                    contentDescription = "Explore",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Explore",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_explore")
                    )

                    // Volunteer Tab 2: My Events
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.Event else Icons.Outlined.Event,
                                    contentDescription = "My Events",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "My Events",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_my_events")
                    )

                    // Volunteer Tab 3: Community
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 3) Icons.Filled.Forum else Icons.Outlined.Forum,
                                    contentDescription = "Community",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Community",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_community")
                    )

                    // Volunteer Tab 4: Profile
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Profile",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                } else {
                    // NGO Leader Tab 0: Dashboard
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Dashboard",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Dashboard",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_ngo_dash")
                    )

                    // NGO Leader Tab 1: Drives Management
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment,
                                    contentDescription = "Drives",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Drives",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_ngo_drives")
                    )

                    // NGO Leader Tab 2: Roster & Attendance
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.AutoMirrored.Filled.FactCheck else Icons.AutoMirrored.Outlined.FactCheck,
                                    contentDescription = "Roster",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Roster",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_ngo_roster")
                    )

                    // NGO Leader Tab 3: Community
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 3) Icons.Filled.Forum else Icons.Outlined.Forum,
                                    contentDescription = "Community",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Community",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_community")
                    )

                    // NGO Leader Tab 4: Organization Profile
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == 4) Icons.Filled.Business else Icons.Outlined.Business,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Profile",
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SocialGreen,
                            selectedTextColor = SocialGreen,
                            indicatorColor = SocialGreenUltraLight,
                            unselectedIconColor = SocialTextMuted,
                            unselectedTextColor = SocialTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    if (portalRole == "VOLUNTEER") {
                        SocialHomeScreen(
                            events = filteredEvents,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            sortMethod = sortMethod,
                            currentLocation = userLocation,
                            userName = currentUser?.fullName ?: "Rahul Nile",
                            onCategorySelect = { viewModel.setCategory(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onSortChange = { viewModel.setSortMethod(it) },
                            onOpenPreferences = { showPreferencesModal = true },
                            onOpenMySchedule = {
                                selectedTab = 2
                                myEventsSubTab = "upcoming"
                            },
                            onSelectEvent = { selectedEventForDetail = it },
                            onToggleJoin = { viewModel.toggleJoinSocialEvent(it.id) },
                            onToggleBookmark = { viewModel.toggleBookmarkSocialEvent(it.id) },
                            onResetFilters = { viewModel.resetFilters() }
                        )
                    } else {
                        SocialNGODashboardScreen(
                            roster = roster,
                            events = filteredEvents,
                            onToggleAttendance = { viewModel.toggleVolunteerAttendance(it) },
                            onOpenCreateEvent = { showCreateEventModal = true }
                        )
                    }
                }

                1 -> {
                    if (portalRole == "VOLUNTEER") {
                        SocialExploreScreen(
                            events = filteredEvents,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            selectedLocation = userLocation,
                            selectedDateFilter = selectedDateFilter,
                            searchGroundingState = searchGroundingState,
                            onPerformSearchGrounding = { viewModel.performGoogleSearchGrounding(it) },
                            onClearSearchGrounding = { viewModel.clearSearchGrounding() },
                            onCategorySelect = { viewModel.setCategory(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onLocationSelect = { viewModel.setLocation(it) },
                            onDateSelect = { viewModel.setDateFilter(it) },
                            onResetFilters = { viewModel.resetFilters() },
                            onSelectEvent = { selectedEventForDetail = it },
                            onToggleJoin = { viewModel.toggleJoinSocialEvent(it.id) },
                            onToggleBookmark = { viewModel.toggleBookmarkSocialEvent(it.id) }
                        )
                    } else {
                        // NGO Drives Management Screen
                        SocialExploreScreen(
                            events = filteredEvents,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            selectedLocation = userLocation,
                            selectedDateFilter = selectedDateFilter,
                            searchGroundingState = searchGroundingState,
                            onPerformSearchGrounding = { viewModel.performGoogleSearchGrounding(it) },
                            onClearSearchGrounding = { viewModel.clearSearchGrounding() },
                            onCategorySelect = { viewModel.setCategory(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onLocationSelect = { viewModel.setLocation(it) },
                            onDateSelect = { viewModel.setDateFilter(it) },
                            onResetFilters = { viewModel.resetFilters() },
                            onSelectEvent = { selectedEventForDetail = it },
                            onToggleJoin = { /* NGOs don't join their own drives */ },
                            onToggleBookmark = { viewModel.toggleBookmarkSocialEvent(it.id) }
                        )
                    }
                }

                2 -> {
                    if (portalRole == "VOLUNTEER") {
                        SocialMyEventsScreen(
                            upcomingEvents = upcomingEvents,
                            completedEvents = completedEvents,
                            savedEvents = savedEvents,
                            selectedTab = myEventsSubTab,
                            onTabSelect = { myEventsSubTab = it },
                            onSelectEvent = { selectedEventForDetail = it },
                            onShowDirections = { selectedEventForDirections = it },
                            onCancelJoin = { viewModel.toggleJoinSocialEvent(it.id) },
                            onViewCertificate = { selectedCompletedForCert = it },
                            onBrowseDrives = { selectedTab = 1 }
                        )
                    } else {
                        // NGO Roster & Attendance Check-In Screen
                        SocialNGODashboardScreen(
                            roster = roster,
                            events = filteredEvents,
                            onToggleAttendance = { viewModel.toggleVolunteerAttendance(it) },
                            onOpenCreateEvent = { showCreateEventModal = true }
                        )
                    }
                }

                3 -> SocialCommunityScreen(
                    posts = posts,
                    ngos = ngos,
                    userName = currentUser?.fullName ?: (if (portalRole == "VOLUNTEER") "Rahul Nile" else "Aarav Patel"),
                    onToggleLike = { viewModel.togglePostLike(it) },
                    onAddComment = { id, text -> viewModel.addPostComment(id, text) },
                    onPublishPost = { viewModel.publishCommunityPost(it) },
                    onToggleFollowNGO = { viewModel.toggleFollowNGO(it) }
                )

                4 -> SocialProfileScreen(
                    userCauses = userCauses,
                    currentUser = currentUser,
                    onOpenPreferences = { showPreferencesModal = true },
                    onFindDrive = { selectedTab = 1 },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }

    // Modal Overlays
    selectedEventForDetail?.let { event ->
        EventDetailModal(
            event = event,
            onDismiss = { selectedEventForDetail = null },
            onToggleJoin = { viewModel.toggleJoinSocialEvent(event.id) },
            onToggleBookmark = { viewModel.toggleBookmarkSocialEvent(event.id) },
            onShowDirections = {
                selectedEventForDirections = event
            }
        )
    }

    selectedCompletedForCert?.let { completed ->
        CertificateModal(
            event = completed,
            volunteerName = currentUser?.fullName ?: "Rahul Nile",
            onDismiss = { selectedCompletedForCert = null }
        )
    }

    selectedEventForDirections?.let { event ->
        DirectionsModal(
            event = event,
            onDismiss = { selectedEventForDirections = null }
        )
    }

    if (showPreferencesModal) {
        PreferencesModal(
            initialCauses = userCauses,
            initialLocation = userLocation,
            initialDistance = userDistance,
            onDismiss = { showPreferencesModal = false },
            onSave = { causes, loc, dist ->
                viewModel.savePreferences(causes, loc, dist)
            }
        )
    }

    if (showNotificationsModal) {
        NotificationDropdownModal(
            notifications = notifications,
            onDismiss = { showNotificationsModal = false },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onNotificationClick = {
                // If it relates to events, go to my events
                selectedTab = 2
            }
        )
    }

    if (showCreateEventModal) {
        CreateEventModal(
            onDismiss = { showCreateEventModal = false },
            onCreateEvent = { title, cat, dateStr, timeStr, loc, maxVol, desc, skills, bring, img ->
                viewModel.createSocialEvent(
                    title = title,
                    category = cat,
                    dateStr = dateStr,
                    timeStr = timeStr,
                    location = loc,
                    maxVolunteers = maxVol,
                    description = desc,
                    skills = skills,
                    bring = bring,
                    imageUrl = img
                )
            }
        )
    }
}
