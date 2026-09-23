package com.example.data.model

import com.example.R

fun getCategoryDrawable(category: String): Int {
    return when (category.lowercase().trim()) {
        "environment", "tree plantation", "plantation" -> R.drawable.event_tree_plantation
        "education", "teaching", "literacy" -> R.drawable.event_teaching
        "food distribution", "food", "hunger" -> R.drawable.event_food_drive
        "animal welfare", "animals", "shelter" -> R.drawable.event_animal_rescue
        "community development", "cleanliness", "river cleanup", "cleanup", "disaster relief" -> R.drawable.event_cleanliness
        "healthcare", "health", "medical" -> R.drawable.event_health_camp
        "elderly care", "elderly" -> R.drawable.event_elderly_care
        "women empowerment", "women" -> R.drawable.event_women_empower
        else -> R.drawable.event_tree_plantation
    }
}

data class SocialEvent(
    val id: String,
    val title: String,
    val ngoId: String,
    val ngoName: String,
    val ngoLogo: String,
    val verified: Boolean = true,
    val category: String, // Environment, Education, Food Distribution, Healthcare, Animal Welfare, Women Empowerment, Disaster Relief, Community Development
    val dateStr: String,
    val timeStr: String,
    val location: String,
    val distanceKm: Int,
    val joinedCount: Int,
    val maxVolunteers: Int,
    val coverImg: String,
    val description: String,
    val whatYoullDo: List<String>,
    val skillsNeeded: List<String>,
    val whatToBring: List<String>,
    val isJoined: Boolean = false,
    val isBookmarked: Boolean = false,
    val isWeekend: Boolean = true,
    val localDrawableRes: Int = getCategoryDrawable(category)
)

data class SocialNGO(
    val id: String,
    val name: String,
    val verified: Boolean = true,
    val logo: String,
    val cover: String,
    val desc: String,
    val causes: List<String>,
    val volunteersCount: Int,
    val eventsCount: Int,
    val hoursCount: Int,
    val followed: Boolean = false,
    val location: String
)

data class SocialPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatar: String,
    val time: String,
    val text: String,
    val images: List<String>,
    val likes: Int,
    val liked: Boolean = false,
    val comments: List<SocialComment> = emptyList()
)

data class SocialComment(
    val author: String,
    val avatar: String,
    val text: String,
    val time: String = "Just now"
)

data class SocialCompletedEvent(
    val id: String,
    val title: String,
    val ngoName: String,
    val dateStr: String,
    val hours: Int,
    val category: String,
    val thumbImg: String,
    val localDrawableRes: Int = getCategoryDrawable(category)
)

data class VolunteerRosterItem(
    val id: String,
    val name: String,
    val avatar: String,
    val skills: String,
    val regDate: String,
    val event: String,
    val status: String, // "Registered", "Attended", "Cancelled"
    val hours: Int
)

data class SocialNotificationItem(
    val id: String,
    val icon: String,
    val title: String,
    val desc: String,
    val time: String,
    val unread: Boolean = true,
    val action: String = "explore"
)
