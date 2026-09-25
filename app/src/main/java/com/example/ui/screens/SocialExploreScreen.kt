package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialEvent
import com.example.ui.components.SocialEventImage
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.data.service.SearchGroundingUiState
import com.example.ui.components.GoogleSearchGroundingCard
import com.example.ui.components.GoogleSearchGroundingQuickPills
import com.example.ui.theme.SocialTextSubtle
import com.example.ui.theme.SocialWarm

@Composable
fun SocialExploreScreen(
    events: List<SocialEvent>,
    selectedCategory: String,
    searchQuery: String,
    selectedLocation: String,
    selectedDateFilter: String,
    searchGroundingState: SearchGroundingUiState = SearchGroundingUiState.Idle,
    onPerformSearchGrounding: (String) -> Unit = {},
    onClearSearchGrounding: () -> Unit = {},
    onCategorySelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onLocationSelect: (String) -> Unit,
    onDateSelect: (String) -> Unit,
    onResetFilters: () -> Unit,
    onSelectEvent: (SocialEvent) -> Unit,
    onToggleJoin: (SocialEvent) -> Unit,
    onToggleBookmark: (SocialEvent) -> Unit
) {
    var causeMenuExpanded by remember { mutableStateOf(false) }
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var dateMenuExpanded by remember { mutableStateOf(false) }

    val causesList = listOf(
        "all" to "All Causes",
        "Environment" to "🌱 Environment",
        "Education" to "📚 Education",
        "Food Distribution" to "🍱 Food Security",
        "Animal Welfare" to "🐶 Animal Care",
        "Healthcare" to "🏥 Healthcare Camps",
        "Women Empowerment" to "👩 Women Rights",
        "Disaster Relief" to "🚨 Disaster Relief"
    )

    val locationsList = listOf(
        "all" to "All Delhi NCR",
        "Dwarka" to "Dwarka, New Delhi",
        "South Delhi" to "South Delhi",
        "Connaught Place" to "Central Delhi / CP",
        "Janakpuri" to "Janakpuri & West",
        "Noida" to "Noida & Gr. Noida",
        "Gurugram" to "Gurugram, HR"
    )

    val datesList = listOf(
        "all" to "All Dates",
        "this-weekend" to "⚡ This Weekend",
        "next-7-days" to "📅 Next 7 Days",
        "today" to "☀️ Today / Tomorrow"
    )

    val hasActiveFilters = searchQuery.isNotBlank() ||
            (selectedCategory != "all" && selectedCategory.isNotBlank()) ||
            (selectedLocation != "all" && selectedLocation.isNotBlank() && selectedLocation != "Delhi NCR") ||
            (selectedDateFilter != "all" && selectedDateFilter.isNotBlank())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        // Tight, zero-waste discover header
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Title & Reset Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Discover Volunteer Drives",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SocialTextMain
                    )
                    Text(
                        text = "Filter opportunities by cause area, location, and date",
                        fontSize = 12.sp,
                        color = SocialTextMuted
                    )
                }

                if (hasActiveFilters) {
                    Surface(
                        onClick = onResetFilters,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                placeholder = {
                    Text(
                        text = "Search causes, activities, NGOs, keywords...",
                        fontSize = 13.sp,
                        color = SocialTextSubtle
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SocialGreen,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SocialTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                val q = searchQuery.ifBlank { "Yamuna river cleanup and Delhi environmental volunteer drives 2026" }
                                onPerformSearchGrounding(q)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Google Search Grounding",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    cursorColor = SocialGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = SocialGreen,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Grounding Inquiry Chips
            GoogleSearchGroundingQuickPills(
                onSelectQuery = { q ->
                    onSearchChange(q)
                    onPerformSearchGrounding(q)
                }
            )

            // Live Google Search Grounding Results Card (gemini-3.5-flash)
            GoogleSearchGroundingCard(
                state = searchGroundingState,
                onSearchAgain = onPerformSearchGrounding,
                onDismiss = onClearSearchGrounding
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Way Filter Control Bar: Cause Area | Location | Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Cause Area Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val isCauseActive = selectedCategory != "all" && selectedCategory.isNotBlank()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCauseActive) SocialGreenUltraLight else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isCauseActive) SocialGreen else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clickable { causeMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val causeLabel = causesList.find { it.first == selectedCategory }?.second ?: "Cause"
                            Text(
                                text = causeLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isCauseActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCauseActive) SocialGreenDark else SocialTextMain,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Cause",
                                tint = if (isCauseActive) SocialGreen else SocialTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = causeMenuExpanded,
                        onDismissRequest = { causeMenuExpanded = false }
                    ) {
                        causesList.forEach { (catKey, catName) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = catName,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedCategory == catKey) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCategory == catKey) SocialGreen else SocialTextMain
                                    )
                                },
                                onClick = {
                                    onCategorySelect(catKey)
                                    causeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Location Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val isLocActive = selectedLocation != "all" && selectedLocation != "Delhi NCR" && selectedLocation.isNotBlank()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLocActive) SocialGreenUltraLight else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isLocActive) SocialGreen else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clickable { locationMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val locLabel = if (isLocActive) selectedLocation else "Location"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isLocActive) SocialGreen else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = locLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isLocActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLocActive) SocialGreenDark else SocialTextMain,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Location",
                                tint = if (isLocActive) SocialGreen else SocialTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = locationMenuExpanded,
                        onDismissRequest = { locationMenuExpanded = false }
                    ) {
                        locationsList.forEach { (locKey, locName) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = locName,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedLocation == locKey) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedLocation == locKey) SocialGreen else SocialTextMain
                                    )
                                },
                                onClick = {
                                    onLocationSelect(locKey)
                                    locationMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. Date Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val isDateActive = selectedDateFilter != "all" && selectedDateFilter.isNotBlank()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDateActive) SocialGreenUltraLight else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isDateActive) SocialGreen else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clickable { dateMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val dateLabel = when (selectedDateFilter) {
                                "this-weekend" -> "Weekend"
                                "next-7-days" -> "7 Days"
                                "today" -> "Today"
                                else -> "Date"
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isDateActive) SocialGreen else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = dateLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isDateActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDateActive) SocialGreenDark else SocialTextMain,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Date",
                                tint = if (isDateActive) SocialGreen else SocialTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dateMenuExpanded,
                        onDismissRequest = { dateMenuExpanded = false }
                    ) {
                        datesList.forEach { (dKey, dName) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = dName,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedDateFilter == dKey) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedDateFilter == dKey) SocialGreen else SocialTextMain
                                    )
                                },
                                onClick = {
                                    onDateSelect(dKey)
                                    dateMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickPills = listOf(
                    "all" to "All Drives",
                    "Environment" to "🌱 Environment",
                    "Education" to "📚 Education",
                    "Food Distribution" to "🍱 Food Security",
                    "Animal Welfare" to "🐶 Animal Care",
                    "Healthcare" to "🏥 Healthcare",
                    "Women Empowerment" to "👩 Women Rights",
                    "Disaster Relief" to "🚨 Relief"
                )

                quickPills.forEach { (catKey, label) ->
                    val isSel = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) SocialGreen else Color.White,
                        border = BorderStroke(1.dp, if (isSel) SocialGreen else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { onCategorySelect(catKey) }
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.White else SocialTextMain,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results count row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${events.size} volunteer opportunities found",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                if (hasActiveFilters) {
                    Text(
                        text = "Filtered view",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SocialGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Empty State or Event Cards
        if (events.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔎", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No opportunities match these filters",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your location, cause, or date filters to see more results.",
                            fontSize = 12.sp,
                            color = SocialTextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onResetFilters,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SocialGreen)
                        ) {
                            Text("Reset All Filters", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                SocialExploreEventCard(
                    event = event,
                    onSelect = { onSelectEvent(event) },
                    onToggleJoin = { onToggleJoin(event) },
                    onToggleBookmark = { onToggleBookmark(event) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SocialExploreEventCard(
    event: SocialEvent,
    onSelect: () -> Unit,
    onToggleJoin: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            // Hero Photo with Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                SocialEventImage(
                    event = event,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )

                // Cause Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SocialGreen,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = event.category,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Bookmark Icon
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (event.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (event.isBookmarked) SocialWarm else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // If user is joined, show indicator
                if (event.isJoined) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF047857),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Registered",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(14.dp)) {
                // NGO Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = event.ngoLogo,
                        contentDescription = event.ngoName,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.ngoName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SocialTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified NGO",
                        tint = SocialGreen,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = SocialGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${event.dateStr} • ${event.timeStr}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SocialTextMain
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Location & Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${event.location} (${event.distanceKm} km away)",
                        fontSize = 12.sp,
                        color = SocialTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Registered Changemakers Preview Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatars
                            val changemakers = event.registeredChangemakers.ifEmpty {
                                listOf(
                                    com.example.data.model.Changemaker("1", "Ananya Verma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=80&q=80", "Volunteer", "Star", "Today"),
                                    com.example.data.model.Changemaker("2", "Rohan Gupta", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80", "Volunteer", "Star", "Yesterday")
                                )
                            }
                            Row {
                                changemakers.take(3).forEachIndexed { idx, cm ->
                                    if (cm.avatar == "RN" || !cm.avatar.startsWith("http") || cm.name.contains("Rahul", ignoreCase = true)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF064E3B), Color(0xFF059669))
                                                    )
                                                )
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "RN",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = cm.avatar,
                                            contentDescription = cm.name,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    if (idx < 2) Spacer(modifier = Modifier.width((-6).dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (event.isJoined) "You + ${event.joinedCount - 1} changemakers" else "${event.joinedCount} changemakers registered",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (event.isJoined) SocialGreen else SocialTextMain
                            )
                        }

                        Text(
                            text = "View All →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                val progress = (event.joinedCount.toFloat() / event.maxVolunteers.toFloat()).coerceIn(0f, 1f)
                val spotsLeft = maxOf(0, event.maxVolunteers - event.joinedCount)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.joinedCount}/${event.maxVolunteers} Volunteers",
                        fontSize = 11.sp,
                        color = SocialTextMuted
                    )
                    Text(
                        text = "$spotsLeft spots left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (spotsLeft <= 5) Color(0xFFEF4444) else SocialGreen
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SocialGreen,
                    trackColor = Color(0xFFF1F5F9)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Buttons: View Details & Register
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelect,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                    }

                    Button(
                        onClick = onToggleJoin,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.isJoined) Color(0xFF047857) else SocialGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (event.isJoined) "Registered ✓" else "Register Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
