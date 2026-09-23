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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SocialNGO
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialTextSubtle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialNGOsScreen(
    ngos: List<SocialNGO>,
    onToggleFollow: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNGOs = ngos.filter { ngo ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.lowercase()
            ngo.name.lowercase().contains(q) ||
                ngo.desc.lowercase().contains(q) ||
                ngo.location.lowercase().contains(q) ||
                ngo.causes.any { it.lowercase().contains(q) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Banner Title (Website Screenshot 9)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Discover NGOs & Partners",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SocialTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Partner with verified, transparent non-profits actively organizing drives and community projects across Delhi NCR.",
                    fontSize = 13.sp,
                    color = SocialTextMuted,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = {
                    Text(
                        text = "Search NGOs by name, cause, location...",
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
                        IconButton(onClick = { searchQuery = "" }) {
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "${filteredNGOs.size} Verified Non-Profits",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SocialTextMain
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(filteredNGOs, key = { it.id }) { ngo ->
            NGOCard(
                ngo = ngo,
                onToggleFollow = { onToggleFollow(ngo.id) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NGOCard(
    ngo: SocialNGO,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cover Image with Circular Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = ngo.cover,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )

                // Circular Logo overlay at bottom left
                AsyncImage(
                    model = ngo.logo,
                    contentDescription = ngo.name,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(56.dp)
                        .align(Alignment.BottomStart)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Card Body
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ngo.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialTextMain
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified NGO",
                                tint = SocialGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ngo.location,
                                fontSize = 12.sp,
                                color = SocialTextMuted
                            )
                        }
                    }

                    // Follow Button
                    Button(
                        onClick = onToggleFollow,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ngo.followed) Color(0xFFE2E8F0) else SocialGreen
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = if (ngo.followed) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = if (ngo.followed) SocialTextMain else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (ngo.followed) "Following" else "Follow",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ngo.followed) SocialTextMain else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = ngo.desc,
                    fontSize = 13.sp,
                    color = SocialTextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Causes tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ngo.causes.forEach { cause ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SocialGreenUltraLight
                        ) {
                            Text(
                                text = cause,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${ngo.volunteersCount}+",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                        Text(text = "Volunteers", fontSize = 11.sp, color = SocialTextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${ngo.eventsCount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                        Text(text = "Events Done", fontSize = 11.sp, color = SocialTextMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${ngo.hoursCount}h",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialGreen
                        )
                        Text(text = "Impact Hours", fontSize = 11.sp, color = SocialTextMuted)
                    }
                }
            }
        }
    }
}
