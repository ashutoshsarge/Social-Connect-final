package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.SocialTextSubtle
import com.example.ui.theme.SocialWarm

@Composable
fun SocialHomeScreen(
    events: List<SocialEvent>,
    selectedCategory: String,
    searchQuery: String,
    sortMethod: String,
    currentLocation: String,
    userName: String = "Volunteer",
    onCategorySelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenMySchedule: () -> Unit,
    onSelectEvent: (SocialEvent) -> Unit,
    onToggleJoin: (SocialEvent) -> Unit,
    onToggleBookmark: (SocialEvent) -> Unit,
    onResetFilters: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val categoryPills = listOf(
        "all" to "All",
        "near-me" to "📍 Near Me (<6 km)",
        "this-weekend" to "⚡ This Weekend",
        "Environment" to "🌱 Environment",
        "Education" to "📚 Education",
        "Healthcare" to "🏥 Healthcare",
        "Animal Welfare" to "🐶 Animal Welfare",
        "Food Distribution" to "🍱 Food Distribution",
        "Women Empowerment" to "👩 Women Empowerment",
        "Disaster Relief" to "🚨 Disaster Relief"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting Section (Website Screenshot 1 & 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val firstName = userName.trim().split(" ").firstOrNull()?.ifBlank { "Volunteer" } ?: "Volunteer"
                    Text(
                        text = "Good morning, $firstName 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Welcome to SocialConnect! Ready to make a real difference today? Here are curated drives for you.",
                        fontSize = 13.sp,
                        color = SocialTextMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .clickable { onOpenPreferences() }
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🎯", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Customize Preferences",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .clickable { onOpenMySchedule() }
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📅", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "My Schedule (${events.count { it.isJoined }})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar & Sort Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    placeholder = {
                        Text(
                            text = "Search events, causes, NGOs...",
                            fontSize = 13.sp,
                            color = SocialTextSubtle
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SocialTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SocialTextMuted
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
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Dropdown Box
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .height(52.dp)
                            .clickable { sortMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Sort",
                                tint = SocialGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (sortMethod) {
                                    "nearest" -> "Nearest"
                                    "popular" -> "Popular"
                                    "newest" -> "Newest"
                                    else -> "Sort"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialTextMain
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Recommended") },
                            onClick = {
                                onSortChange("recommended")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nearest (< 5 km)") },
                            onClick = {
                                onSortChange("nearest")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Most Popular") },
                            onClick = {
                                onSortChange("popular")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Newest") },
                            onClick = {
                                onSortChange("newest")
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Category Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryPills.forEach { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) SocialGreen else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SocialGreen else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.clickable { onCategorySelect(catKey) }
                    ) {
                        Text(
                            text = catLabel,
                            color = if (isSelected) Color.White else SocialTextMain,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Recommended for You",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Text(
                        text = "Based on your interests in Environment, Education, and $currentLocation",
                        fontSize = 12.sp,
                        color = SocialTextMuted
                    )
                }
                Text(
                    text = "${events.size} events",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (events.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No events found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                        Text(
                            text = "Try adjusting your search terms or category filters.",
                            fontSize = 12.sp,
                            color = SocialTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onResetFilters) {
                            Text("Reset All Filters", color = SocialGreen)
                        }
                    }
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                SocialEventCard(
                    event = event,
                    onSelect = { onSelectEvent(event) },
                    onToggleJoin = { onToggleJoin(event) },
                    onToggleBookmark = { onToggleBookmark(event) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SocialEventCard(
    event: SocialEvent,
    onSelect: () -> Unit,
    onToggleJoin: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cover Image with Badges
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

                // Top Category Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SocialGreen,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = event.category,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Bookmark Heart/Star Button
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = if (event.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (event.isBookmarked) SocialWarm else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Card Body
            Column(modifier = Modifier.padding(16.dp)) {
                // NGO Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = event.ngoLogo,
                        contentDescription = event.ngoName,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain,
                    lineHeight = 22.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Description
                Text(
                    text = event.description,
                    fontSize = 13.sp,
                    color = SocialTextMuted,
                    lineHeight = 18.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = SocialGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.dateStr} • ${event.timeStr}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SocialTextMain
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Location & Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.location} • ${event.distanceKm} km away",
                        fontSize = 12.sp,
                        color = SocialTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Capacity Tracker
                val progress = (event.joinedCount.toFloat() / event.maxVolunteers.toFloat()).coerceIn(0f, 1f)
                val spotsLeft = maxOf(0, event.maxVolunteers - event.joinedCount)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = SocialGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.joinedCount} joined",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMuted
                        )
                    }
                    Text(
                        text = "$spotsLeft spots left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (spotsLeft <= 5) Color(0xFFEF4444) else SocialGreen
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SocialGreen,
                    trackColor = Color(0xFFF1F5F9)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Registered Changemakers Preview Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                            val changemakers = event.registeredChangemakers.ifEmpty {
                                listOf(
                                    com.example.data.model.Changemaker("1", "Ananya Verma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=80&q=80", "Volunteer", "Star", "Today"),
                                    com.example.data.model.Changemaker("2", "Rohan Gupta", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80", "Volunteer", "Star", "Yesterday")
                                )
                            }
                            Row {
                                changemakers.take(3).forEachIndexed { idx, cm ->
                                    if (cm.avatar == "RN" || !cm.avatar.startsWith("http")) {
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

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelect,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "View Event",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                    }

                    Button(
                        onClick = onToggleJoin,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.isJoined) Color(0xFF10B981) else SocialGreen
                        )
                    ) {
                        Icon(
                            imageVector = if (event.isJoined) Icons.Default.Check else Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (event.isJoined) "✓ Registered" else "Join Event",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
