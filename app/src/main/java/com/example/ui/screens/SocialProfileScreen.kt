package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialGreen
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialWarm
import com.example.ui.theme.SocialWarmLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialProfileScreen(
    userCauses: List<String>,
    currentUser: UserEntity? = null,
    onOpenPreferences: () -> Unit,
    onFindDrive: () -> Unit,
    onLogout: () -> Unit
) {
    val isNgoLeader = currentUser?.role == "NGO_LEADER"

    val avatarUrl = if (isNgoLeader) {
        "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=240&q=80"
    } else {
        null
    }

    val displayName = if (isNgoLeader) {
        currentUser?.fullName?.ifBlank { "Dr. Aarav Patel" } ?: "Dr. Aarav Patel"
    } else {
        currentUser?.fullName?.ifBlank { "Rahul Nile" } ?: "Rahul Nile"
    }

    val roleTitle = if (isNgoLeader) "Executive Director & NGO Leader" else "Verified Volunteer"
    val orgTitle = if (isNgoLeader) "Green Delhi Foundation • Reg. #DL-NGO-8821" else (currentUser?.organization ?: "Delhi Youth Volunteers")
    val emailText = if (isNgoLeader) (currentUser?.email ?: "aarav.patel@greendelhi.org") else (currentUser?.email ?: "rahulnile@gmail.com")
    val phoneText = if (isNgoLeader) (currentUser?.phone ?: "+91 98112 34567") else (currentUser?.phone ?: "+91 98765 43210")
    val locationText = if (isNgoLeader) "Connaught Place, Central Delhi" else "Delhi, India"
    val memberStatusText = if (isNgoLeader) "NGO Founder & Leader" else "Active Member"

    val bioText = if (isNgoLeader) {
        "Executive Director at Green Delhi Foundation. Spearheading urban afforestation, Yamuna cleanup drives, and community empowerment initiatives. Mobilized 5,000+ changemakers across Delhi NCR since 2019."
    } else {
        "Passionate about education, environment and community development. Believer in grassroots action and active civic participation."
    }

    val statusPillText = if (isNgoLeader) {
        "Organizing 3 upcoming weekend drives across Delhi NCR"
    } else {
        "Available this weekend for local drives"
    }

    val displayedCauses = if (isNgoLeader) {
        listOf("Environment", "Clean Energy", "Riverfront Restoration", "Civic Action", "Youth Leadership")
    } else {
        if (userCauses.isNotEmpty()) userCauses else listOf("Environment", "Education", "Food Security", "Animal Care")
    }

    val skillsTitle = if (isNgoLeader) "Core Focus Areas" else "Skills & Capabilities"
    val skills = if (isNgoLeader) {
        listOf("Urban Afforestation", "Volunteer Mobilization", "Civic Policy", "Waste Management", "Youth Mentorship")
    } else {
        listOf("Teaching", "Photography", "Social Media", "Event Coordination", "First Aid")
    }

    val badges = if (isNgoLeader) {
        listOf(
            BadgeData("🏆", "Master Organizer", "Organized 30+ verified social drives in NCR", Color(0xFFFEF3C7)),
            BadgeData("🌿", "Green Delhi Pioneer", "Led largest urban afforestation campaigns", Color(0xFFD1FAE5)),
            BadgeData("🛡️", "FCRA & 80G Certified", "100% compliant, audited nonprofit partner", Color(0xFFDBEAFE)),
            BadgeData("⭐", "Top Rated Partner", "98% volunteer satisfaction rating", Color(0xFFFFEDD5))
        )
    } else {
        listOf(
            BadgeData("🏅", "Community Builder", "Attended 10+ community drives in Delhi", Color(0xFFFEF3C7)),
            BadgeData("🌱", "Green Champion", "Planted over 50 native trees in Dwarka", Color(0xFFD1FAE5)),
            BadgeData("📚", "Education Volunteer", "Taught 20+ hours with Udaan Foundation", Color(0xFFDBEAFE)),
            BadgeData("⭐", "Weekend Volunteer", "Active 5 consecutive weekends in service", Color(0xFFFFEDD5))
        )
    }

    val activitiesTitle = if (isNgoLeader) "Drives Organized" else "Recent Volunteer Activity"
    val activities = if (isNgoLeader) {
        listOf(
            ActivityData("🌱", "Dwarka Native Reforestation Drive", "Dwarka Sector 10 Eco Park", "Sunday, Oct 18", "68 Registered"),
            ActivityData("🌊", "Clean Yamuna Riverfront Restoration", "Yamuna Ghat, Kashmere Gate", "Sunday, Sep 20", "120 Attended"),
            ActivityData("🍱", "Winter Ration & Warm Meals Drive", "Janakpuri District Center", "Sunday, Aug 30", "85 Mobilized")
        )
    } else {
        listOf(
            ActivityData("🌱", "Tree Plantation Drive 2026", "Dwarka Sector 10 Eco Park", "Sunday, Sep 13", "4 hrs logged"),
            ActivityData("📚", "Teach & Inspire Weekend", "Najafgarh Community Center", "Saturday, Sep 12", "3 hrs logged"),
            ActivityData("🍱", "Community Meal Distribution", "Janakpuri District Center", "Sunday, Aug 30", "5 hrs logged")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar / RN Logo with edit/preference button
                        Box {
                            if (isNgoLeader && !avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Profile Photo of $displayName",
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, SocialGreen, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Crisp, distinctive RN monogram logo
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF10B981))
                                            )
                                        )
                                        .border(2.5.dp, SocialGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "RN",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SocialGreen)
                                    .border(2.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd)
                                    .clickable { onOpenPreferences() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = displayName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SocialTextMain
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Changemaker",
                                        tint = SocialGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Surface(
                                    onClick = onOpenPreferences,
                                    shape = RoundedCornerShape(10.dp),
                                    color = SocialGreenUltraLight,
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = SocialGreenDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Edit",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SocialGreenDark
                                        )
                                    }
                                }
                            }

                            Text(
                                text = roleTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialGreen
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = orgTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SocialGreenDark
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = emailText,
                                    fontSize = 11.sp,
                                    color = SocialTextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Email",
                                            tint = SocialGreen,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Verified",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SocialGreenDark
                                        )
                                    }
                                }
                            }

                            Text(
                                text = phoneText,
                                fontSize = 11.sp,
                                color = SocialTextMuted
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = locationText, fontSize = 11.sp, color = SocialTextMuted)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "•", fontSize = 11.sp, color = SocialTextMuted)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = memberStatusText, fontSize = 11.sp, color = SocialTextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = bioText,
                        fontSize = 13.sp,
                        color = SocialTextMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SocialGreenUltraLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SocialGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusPillText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = onOpenPreferences,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = SocialGreenUltraLight,
                            border = BorderStroke(1.5.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile & Preferences",
                                    tint = SocialGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Edit Profile",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocialGreenDark
                                )
                            }
                        }

                        Button(
                            onClick = onFindDrive,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocialGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isNgoLeader) Icons.Default.Explore else Icons.Default.Search,
                                contentDescription = if (isNgoLeader) "Create Drive" else "Find Drives",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isNgoLeader) "+ Create Drive" else "Find Drives",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Ribbon
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (isNgoLeader) {
                        ProfileStatItem(num = "34", label = "Drives Held")
                        ProfileStatItem(num = "1,420", label = "Volunteers", isHighlight = true)
                        ProfileStatItem(num = "4,850h", label = "Impact Hours")
                        ProfileStatItem(num = "4.9 ★", label = "Rating")
                    } else {
                        ProfileStatItem(num = "12", label = "Drives Joined")
                        ProfileStatItem(num = "${currentUser?.volunteerHours ?: 42}", label = "Volunteer Hours", isHighlight = true)
                        ProfileStatItem(num = "6", label = "NGOs Supported")
                        ProfileStatItem(num = "4", label = "Badges Earned")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Causes / Focus Areas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNgoLeader) "Focus Causes" else "Causes I Care About",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        displayedCauses.forEach { cause ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SocialGreenUltraLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SocialGreenLight)
                            ) {
                                Text(
                                    text = cause,
                                    color = SocialGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = skillsTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skills.forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = skill,
                                    color = SocialTextMain,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Achievement Badges
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNgoLeader) "Leadership & Organization Credentials" else "Achievement Badges",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    badges.chunked(2).forEach { rowBadges ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowBadges.forEach { badge ->
                                BadgeCardView(
                                    badge = badge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowBadges.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Activity Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activitiesTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    activities.forEachIndexed { idx, act ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SocialGreenUltraLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = act.icon, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = act.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocialTextMain
                                )
                                Text(
                                    text = "${act.location} • ${act.date}",
                                    fontSize = 11.sp,
                                    color = SocialTextMuted
                                )
                            }
                            Text(
                                text = act.hours,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialGreen
                            )
                        }
                        if (idx < activities.size - 1) {
                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFFEF2F2),
                    contentColor = Color(0xFFDC2626)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Sign Out of Account",
                    color = Color(0xFFDC2626),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private data class BadgeData(
    val emoji: String,
    val title: String,
    val desc: String,
    val tintBg: Color
)

private data class ActivityData(
    val icon: String,
    val title: String,
    val location: String,
    val date: String,
    val hours: String
)

@Composable
private fun ProfileStatItem(num: String, label: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = num,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isHighlight) SocialGreen else SocialTextMain
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = SocialTextMuted
        )
    }
}

@Composable
private fun BadgeCardView(badge: BadgeData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badge.tintBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = badge.emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SocialTextMain,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge.desc,
                fontSize = 10.sp,
                color = SocialTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
