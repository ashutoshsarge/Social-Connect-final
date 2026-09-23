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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocialEvent
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialTextSubtle

@Composable
fun SocialExploreScreen(
    events: List<SocialEvent>,
    selectedCategory: String,
    searchQuery: String,
    onCategorySelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectEvent: (SocialEvent) -> Unit,
    onToggleJoin: (SocialEvent) -> Unit,
    onToggleBookmark: (SocialEvent) -> Unit
) {
    val categoryTiles = listOf(
        CategoryTileData("🌱", "Environment", "6 Drives", "Environment", Color(0xFFD1FAE5)),
        CategoryTileData("📚", "Education", "4 Programs", "Education", Color(0xFFDBEAFE)),
        CategoryTileData("🍱", "Food Security", "3 Drives", "Food Distribution", Color(0xFFFEF3C7)),
        CategoryTileData("🐶", "Animal Care", "2 Rescues", "Animal Welfare", Color(0xFFFFEDD5)),
        CategoryTileData("🏥", "Healthcare", "2 Camps", "Healthcare", Color(0xFFFCE7F3)),
        CategoryTileData("👩", "Women Rights", "2 Workshops", "Women Empowerment", Color(0xFFEDE9FE))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SocialBgBody)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Banner Title (Website Screenshot 10)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Explore Volunteer Opportunities",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SocialTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse through grassroots drives, educational workshops, and environmental initiatives happening in Delhi NCR.",
                    fontSize = 13.sp,
                    color = SocialTextMuted,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = {
                    Text(
                        text = "Search causes, locations, activities...",
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

            Spacer(modifier = Modifier.height(18.dp))

            // Browse by Cause Grid (2 columns)
            Text(
                text = "Browse by Cause",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SocialTextMain
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Rows of 2 tiles
            for (i in categoryTiles.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tile1 = categoryTiles[i]
                    val isTile1Selected = selectedCategory == tile1.categoryKey
                    CategoryTileView(
                        tile = tile1,
                        isSelected = isTile1Selected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isTile1Selected) onCategorySelect("all") else onCategorySelect(tile1.categoryKey)
                        }
                    )

                    if (i + 1 < categoryTiles.size) {
                        val tile2 = categoryTiles[i + 1]
                        val isTile2Selected = selectedCategory == tile2.categoryKey
                        CategoryTileView(
                            tile = tile2,
                            isSelected = isTile2Selected,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isTile2Selected) onCategorySelect("all") else onCategorySelect(tile2.categoryKey)
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Showing results header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "all") "All Upcoming Drives" else "Drives in $selectedCategory",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                Text(
                    text = "${events.size} opportunities",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(events, key = { it.id }) { event ->
            SocialEventCard(
                event = event,
                onSelect = { onSelectEvent(event) },
                onToggleJoin = { onToggleJoin(event) },
                onToggleBookmark = { onToggleBookmark(event) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private data class CategoryTileData(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val categoryKey: String,
    val tintBg: Color
)

@Composable
private fun CategoryTileView(
    tile: CategoryTileData,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SocialGreenUltraLight else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SocialGreen else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tile.tintBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = tile.emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = tile.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) SocialGreen else SocialTextMain
                )
                Text(
                    text = tile.subtitle,
                    fontSize = 11.sp,
                    color = SocialTextMuted
                )
            }
        }
    }
}
