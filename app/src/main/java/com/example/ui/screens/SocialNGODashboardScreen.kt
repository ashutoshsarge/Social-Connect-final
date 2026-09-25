package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialEvent
import com.example.data.model.VolunteerRosterItem
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialBlue
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialWarm

@Composable
fun SocialNGODashboardScreen(
    roster: List<VolunteerRosterItem>,
    events: List<SocialEvent>,
    onToggleAttendance: (String) -> Unit,
    onOpenCreateEvent: () -> Unit
) {
    var selectedDashboardTab by remember { mutableStateOf("overview") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting & Header (Website Screenshot 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome, Green Delhi Foundation 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Live volunteer participation, ongoing events, and impact analytics.",
                        fontSize = 12.sp,
                        color = SocialTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenCreateEvent,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SocialGreen),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 KPI Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KPICard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Upcoming Events",
                    value = "12",
                    trend = "+2 this month",
                    tint = SocialGreen,
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    icon = Icons.Default.Groups,
                    title = "Registered Vols",
                    value = "486",
                    trend = "+48 this week",
                    tint = SocialBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KPICard(
                    icon = Icons.Default.Schedule,
                    title = "Volunteer Hours",
                    value = "1,240",
                    trend = "+120 hrs",
                    tint = SocialWarm,
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    icon = Icons.Default.Public,
                    title = "People Reached",
                    value = "8,500",
                    trend = "+15% vs last mo",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Volunteer Participation Graph (Website Screenshot 5)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Volunteer Growth & Participation",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialTextMain
                            )
                            Text(
                                text = "2026 Monthly active volunteers and service hours",
                                fontSize = 11.sp,
                                color = SocialTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SocialGreenUltraLight
                        ) {
                            Text(
                                text = "+38% YoY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Canvas Line & Area Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val points = listOf(
                                Offset(0f, h * 0.85f),
                                Offset(w * 0.15f, h * 0.72f),
                                Offset(w * 0.30f, h * 0.65f),
                                Offset(w * 0.45f, h * 0.50f),
                                Offset(w * 0.60f, h * 0.40f),
                                Offset(w * 0.75f, h * 0.28f),
                                Offset(w * 0.90f, h * 0.18f),
                                Offset(w, h * 0.12f)
                            )

                            // Fill path
                            val fillPath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cx = (prev.x + curr.x) / 2
                                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                                }
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF059669).copy(alpha = 0.35f),
                                        Color(0xFF059669).copy(alpha = 0.02f)
                                    )
                                )
                            )

                            // Stroke path
                            val strokePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cx = (prev.x + curr.x) / 2
                                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                                }
                            }

                            drawPath(
                                path = strokePath,
                                color = Color(0xFF059669),
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Point circles
                            points.forEach { pt ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color(0xFF059669),
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Month Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul-Sep").forEach { m ->
                            Text(text = m, fontSize = 10.sp, color = SocialTextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Volunteers Roster & Attendance section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volunteer Roster & Attendance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                Text(
                    text = "${roster.size} Volunteers",
                    fontSize = 12.sp,
                    color = SocialTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Table / Card items of volunteers
        items(roster, key = { it.id }) { vol ->
            VolunteerRosterCard(
                volunteer = vol,
                onToggleAttendance = { onToggleAttendance(vol.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun KPICard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    trend: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SocialTextMuted,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SocialTextMain
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = trend,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SocialGreen
            )
        }
    }
}

@Composable
private fun VolunteerRosterCard(
    volunteer: VolunteerRosterItem,
    onToggleAttendance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (volunteer.avatar == "RN" || !volunteer.avatar.startsWith("http")) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF064E3B), Color(0xFF059669))
                            )
                        )
                        .border(1.5.dp, SocialGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                AsyncImage(
                    model = volunteer.avatar,
                    contentDescription = volunteer.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = volunteer.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                Text(
                    text = "${volunteer.event} • ${volunteer.skills}",
                    fontSize = 11.sp,
                    color = SocialTextMuted,
                    maxLines = 1
                )
            }

            // Attendance Toggle Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (volunteer.status) {
                    "Attended" -> Color(0xFFD1FAE5)
                    "Registered" -> Color(0xFFFEF3C7)
                    else -> Color(0xFFF1F5F9)
                },
                modifier = Modifier.clickable { onToggleAttendance() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (volunteer.status == "Attended") {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = volunteer.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (volunteer.status) {
                            "Attended" -> Color(0xFF065F46)
                            "Registered" -> Color(0xFF92400E)
                            else -> SocialTextMuted
                        }
                    )
                }
            }
        }
    }
}
