package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventModal(
    onDismiss: () -> Unit,
    onCreateEvent: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val categories = listOf("Environment", "Education", "Food Distribution", "Animal Welfare", "Healthcare", "Women Empowerment", "Disaster Relief")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var catExpanded by remember { mutableStateOf(false) }

    var dateStr by remember { mutableStateOf("Sunday, Oct 18, 2026") }
    var timeStr by remember { mutableStateOf("8:00 AM – 12:00 PM") }
    var location by remember { mutableStateOf("Dwarka Sector 12 Park, Delhi") }
    var maxVolunteersStr by remember { mutableStateOf("40") }
    var description by remember { mutableStateOf("") }
    var skillsStr by remember { mutableStateOf("Coordination, Enthusiasm") }
    var bringStr by remember { mutableStateOf("Water bottle, Comfortable clothes") }
    var imageUrl by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                            text = "+ Create Volunteer Drive",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                        Text(
                            text = "Publish a new initiative for changemakers across Delhi",
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

                val modalInputColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    cursorColor = SocialGreen,
                    focusedBorderColor = SocialGreen,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = SocialGreenDark,
                    unfocusedLabelColor = SocialTextMuted,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFBF9F4)
                )

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    placeholder = { Text("e.g. Winter Clothes Donation Camp") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modalInputColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category / Cause") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = modalInputColors
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1.2f),
                        colors = modalInputColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        colors = modalInputColors,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location & Max Volunteers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1.4f),
                        colors = modalInputColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxVolunteersStr,
                        onValueChange = { maxVolunteersStr = it },
                        label = { Text("Capacity") },
                        modifier = Modifier.weight(0.8f),
                        colors = modalInputColors,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Mission *") },
                    placeholder = { Text("Explain the purpose, schedule, and volunteer impact...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modalInputColors,
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Skills & Bring
                OutlinedTextField(
                    value = skillsStr,
                    onValueChange = { skillsStr = it },
                    label = { Text("Skills Required (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modalInputColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bringStr,
                    onValueChange = { bringStr = it },
                    label = { Text("What to Bring (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modalInputColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Banner Image URL (optional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = modalInputColors,
                    singleLine = true
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
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
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text("Cancel", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMsg = "Please enter an event title"
                                return@Button
                            }
                            if (description.isBlank()) {
                                errorMsg = "Please enter event description"
                                return@Button
                            }
                            val maxVol = maxVolunteersStr.toIntOrNull() ?: 30
                            val skills = skillsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val bring = bringStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

                            onCreateEvent(
                                title,
                                selectedCategory,
                                dateStr,
                                timeStr,
                                location,
                                maxVol,
                                description,
                                skills,
                                bring,
                                imageUrl.ifBlank { null }
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SocialGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Publish Drive", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
