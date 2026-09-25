package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialCompletedEvent
import com.example.data.model.SocialEvent
import com.example.ui.components.SocialEventImage
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialBlue
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialWarm

@Composable
fun SocialMyEventsScreen(
    upcomingEvents: List<SocialEvent>,
    completedEvents: List<SocialCompletedEvent>,
    savedEvents: List<SocialEvent>,
    selectedTab: String,
    onTabSelect: (String) -> Unit,
    onSelectEvent: (SocialEvent) -> Unit,
    onShowDirections: (SocialEvent) -> Unit,
    onCancelJoin: (SocialEvent) -> Unit,
    onViewCertificate: (SocialCompletedEvent) -> Unit,
    onBrowseDrives: () -> Unit
) {
    val tabs = listOf("upcoming", "completed", "saved")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Banner Title (Website Screenshot 4)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "My Volunteer Events",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SocialTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage your upcoming commitments, access verified hours, and view saved events.",
                    fontSize = 13.sp,
                    color = SocialTextMuted,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Navigation Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TabPillButton(
                        title = "Upcoming (${upcomingEvents.size})",
                        isSelected = selectedTab == "upcoming",
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelect("upcoming") }
                    )
                    TabPillButton(
                        title = "Completed (${completedEvents.size})",
                        isSelected = selectedTab == "completed",
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelect("completed") }
                    )
                    TabPillButton(
                        title = "Saved (${savedEvents.size})",
                        isSelected = selectedTab == "saved",
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelect("saved") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Render based on selected Tab
        when (selectedTab) {
            "upcoming" -> {
                if (upcomingEvents.isEmpty()) {
                    item {
                        EmptyStateCard(
                            emoji = "📅",
                            title = "No upcoming volunteer events",
                            desc = "You haven't joined any drives yet. Explore and be part of positive change!",
                            actionText = "Find Volunteer Drives",
                            onAction = onBrowseDrives
                        )
                    }
                } else {
                    items(upcomingEvents, key = { it.id }) { event ->
                        UpcomingEventCard(
                            event = event,
                            onViewDetails = { onSelectEvent(event) },
                            onShowDirections = { onShowDirections(event) },
                            onCancel = { onCancelJoin(event) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            "completed" -> {
                if (completedEvents.isEmpty()) {
                    item {
                        EmptyStateCard(
                            emoji = "📜",
                            title = "No completed events yet",
                            desc = "Participate in upcoming drives to earn verified hours and certificates!",
                            actionText = "Discover Opportunities",
                            onAction = onBrowseDrives
                        )
                    }
                } else {
                    items(completedEvents, key = { it.id }) { completed ->
                        CompletedEventCard(
                            event = completed,
                            onViewCertificate = { onViewCertificate(completed) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            "saved" -> {
                if (savedEvents.isEmpty()) {
                    item {
                        EmptyStateCard(
                            emoji = "⭐",
                            title = "No saved events",
                            desc = "Bookmark interesting drives from the Home or Explore tab to save them for later.",
                            actionText = "Explore Drives",
                            onAction = onBrowseDrives
                        )
                    }
                } else {
                    items(savedEvents, key = { it.id }) { event ->
                        SocialEventCard(
                            event = event,
                            onSelect = { onSelectEvent(event) },
                            onToggleJoin = { onCancelJoin(event) },
                            onToggleBookmark = { onCancelJoin(event) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TabPillButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) SocialGreen else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else SocialTextMuted
            )
        }
    }
}

@Composable
fun UpcomingEventCard(
    event: SocialEvent,
    onViewDetails: () -> Unit,
    onShowDirections: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                SocialEventImage(
                    event = event,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Status pill over image
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(SocialGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registered",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                // Category pill over image
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = event.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SocialTextMain
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Organized by ${event.ngoName}",
                fontSize = 12.sp,
                color = SocialTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    color = SocialTextMain
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = event.location,
                    fontSize = 12.sp,
                    color = SocialTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Text("View Details", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onShowDirections,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = SocialGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
}

@Composable
fun CompletedEventCard(
    event: SocialCompletedEvent,
    onViewCertificate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left thumbnail
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = event.localDrawableRes),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (event.thumbImg.startsWith("http")) {
                    AsyncImage(
                        model = event.thumbImg,
                        contentDescription = event.title,
                        placeholder = androidx.compose.ui.res.painterResource(id = event.localDrawableRes),
                        error = androidx.compose.ui.res.painterResource(id = event.localDrawableRes),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Completed Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = "✓ Completed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )

                Text(
                    text = "${event.dateStr} • ${event.ngoName}",
                    fontSize = 12.sp,
                    color = SocialTextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Logged Hours
                Text(
                    text = "⏱️ Volunteer Hours: ${event.hours} hours logged",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Download Certificate Button
                Button(
                    onClick = onViewCertificate,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SocialGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download Certificate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    emoji: String,
    title: String,
    desc: String,
    actionText: String,
    onAction: () -> Unit
) {
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
            Text(text = emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SocialTextMain
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = SocialTextMuted,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SocialGreen)
            ) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
