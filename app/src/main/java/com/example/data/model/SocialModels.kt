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

data class MaterialDonationItem(
    val id: String = "",
    val name: String = "",
    val category: String = "General", // "Toys", "Study Material", "Books", "Food", "Medical", "Supplies"
    val neededQuantity: Int = 50,
    val collectedQuantity: Int = 20,
    val unit: String = "items", // "sets", "kits", "packs", "boxes", "pieces"
    val urgency: String = "Normal", // "Urgent", "High Priority", "Normal"
    val iconEmoji: String = "📦", // "🧸", "📚", "🎨", "🍲", "🌱", "🐾", "🩺"
    val description: String = ""
)

data class DonationCampaign(
    val targetAmount: Double = 25000.0,
    val raisedAmount: Double = 16500.0,
    val donorsCount: Int = 24,
    val currencySymbol: String = "₹",
    val materialItems: List<MaterialDonationItem> = emptyList(),
    val allowMoneyDonation: Boolean = true,
    val taxExemption80G: Boolean = true,
    val userMoneyDonated: Double = 0.0,
    val userMaterialDonated: List<String> = emptyList()
)

data class Changemaker(
    val id: String = "",
    val name: String = "",
    val avatar: String = "",
    val role: String = "Volunteer",
    val badge: String = "Changemaker",
    val registeredDate: String = "Sep 2026"
)

data class SocialEvent(
    val id: String = "",
    val title: String = "",
    val ngoId: String = "",
    val ngoName: String = "",
    val ngoLogo: String = "",
    val verified: Boolean = true,
    val category: String = "Environment",
    val dateStr: String = "",
    val timeStr: String = "",
    val location: String = "",
    val distanceKm: Int = 0,
    val joinedCount: Int = 0,
    val maxVolunteers: Int = 50,
    val coverImg: String = "",
    val description: String = "",
    val whatYoullDo: List<String> = emptyList(),
    val skillsNeeded: List<String> = emptyList(),
    val whatToBring: List<String> = emptyList(),
    val isJoined: Boolean = false,
    val isBookmarked: Boolean = false,
    val isWeekend: Boolean = true,
    val registeredChangemakers: List<Changemaker> = emptyList(),
    val localDrawableRes: Int = getCategoryDrawable(category),
    val donationCampaign: DonationCampaign = DonationCampaign(),
    val createdAt: Long = System.currentTimeMillis()
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
