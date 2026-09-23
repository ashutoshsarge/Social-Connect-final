package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesModal(
    initialCauses: List<String>,
    initialLocation: String,
    initialDistance: Int,
    onDismiss: () -> Unit,
    onSave: (List<String>, String, Int) -> Unit
) {
    val causesOptions = listOf(
        "🌱 Environment",
        "📚 Education",
        "🍱 Food Distribution",
        "🐶 Animal Welfare",
        "🏥 Healthcare",
        "👩 Women Empowerment",
        "🚨 Disaster Relief",
        "🧑🤝🧑 Community Support"
    )

    val selectedCauses = remember {
        mutableStateListOf<String>().apply {
            addAll(initialCauses)
        }
    }

    var locationInput by remember { mutableStateOf(initialLocation) }
    var distanceKm by remember { mutableFloatStateOf(initialDistance.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎯 Customize Preferences",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                        Text(
                            text = "Personalize your recommendations and volunteer alerts",
                            fontSize = 12.sp,
                            color = SocialTextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SocialTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Causes
                Text(
                    text = "Causes You Care About",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    causesOptions.forEach { causeWithIcon ->
                        // Clean cause name without emoji
                        val rawName = causeWithIcon.substringAfter(" ").trim()
                        val isSelected = selectedCauses.any { it.contains(rawName, ignoreCase = true) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SocialGreenUltraLight else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) SocialGreen else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.clickable {
                                if (isSelected) {
                                    selectedCauses.removeAll { it.contains(rawName, ignoreCase = true) }
                                } else {
                                    selectedCauses.add(rawName)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = causeWithIcon,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SocialGreen else SocialTextMain
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SocialGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Location Preference
                Text(
                    text = "Your Primary Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                Spacer(modifier = Modifier.height(6.dp))

                val presetLocations = listOf("Delhi", "Dwarka, Delhi", "South Delhi", "Janakpuri, Delhi", "Noida", "Gurugram")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLocations.forEach { loc ->
                        val isLocSelected = locationInput.equals(loc, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isLocSelected) SocialGreenUltraLight else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isLocSelected) SocialGreen else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.clickable { locationInput = loc }
                        ) {
                            Text(
                                text = "📍 $loc",
                                fontSize = 12.sp,
                                fontWeight = if (isLocSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLocSelected) SocialGreen else SocialTextMain,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Max Distance Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Maximum Travel Distance",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Text(
                        text = "${distanceKm.toInt()} km",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SocialGreen
                    )
                }

                Slider(
                    value = distanceKm,
                    onValueChange = { distanceKm = it },
                    valueRange = 2f..30f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = SocialGreen,
                        activeTrackColor = SocialGreen,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text("Cancel", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onSave(selectedCauses.toList(), locationInput, distanceKm.toInt())
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White)
                    ) {
                        Text("Save & Apply", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
