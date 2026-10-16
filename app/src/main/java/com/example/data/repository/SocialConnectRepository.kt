package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Changemaker
import com.example.data.model.DonationCampaign
import com.example.data.model.MaterialDonationItem
import com.example.data.model.SocialComment
import com.example.data.model.SocialCompletedEvent
import com.example.data.model.SocialEvent
import com.example.data.model.SocialNGO
import com.example.data.model.SocialNotificationItem
import com.example.data.model.SocialPost
import com.example.data.model.VolunteerRosterItem
import com.example.data.model.getCategoryDrawable
import com.example.data.service.SupabaseDataService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SocialConnectRepository(private val context: Context? = null) {

    private val _events = MutableStateFlow(createInitialEvents())
    val events: StateFlow<List<SocialEvent>> = _events.asStateFlow()

    private val _completedEvents = MutableStateFlow(createInitialCompletedEvents())
    val completedEvents: StateFlow<List<SocialCompletedEvent>> = _completedEvents.asStateFlow()

    private val _ngos = MutableStateFlow(createInitialNGOs())
    val ngos: StateFlow<List<SocialNGO>> = _ngos.asStateFlow()

    private val _posts = MutableStateFlow(createInitialPosts())
    val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _roster = MutableStateFlow(createInitialRoster())
    val roster: StateFlow<List<VolunteerRosterItem>> = _roster.asStateFlow()

    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<SocialNotificationItem>> = _notifications.asStateFlow()

    init {
        startRealtimeSync()
    }

    /**
     * Connects to Supabase PostgREST endpoint to fetch live volunteer events.
     * Keeps local state responsive and resilient with offline fallback.
     */
    fun startRealtimeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = SupabaseDataService.fetchEvents()
                if (result.isSuccess) {
                    val remoteList = result.getOrNull() ?: emptyList()
                    if (remoteList.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val currentEvents = _events.value
                            _events.value = remoteList.map { remoteEv ->
                                val localMatch = currentEvents.firstOrNull { it.id == remoteEv.id }
                                if (localMatch != null) {
                                    remoteEv.copy(
                                        isJoined = localMatch.isJoined || remoteEv.isJoined,
                                        isBookmarked = localMatch.isBookmarked || remoteEv.isBookmarked
                                    )
                                } else {
                                    remoteEv
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Notice fetching Supabase events: ${e.message}")
            }
        }
    }


    fun toggleJoinEvent(eventId: String, volunteerName: String = "Rahul Nile"): String {
        var resultMsg = ""
        var updatedChangemakersList: List<Changemaker> = emptyList()
        var updatedCount = 0

        _events.value = _events.value.map { event ->
            if (event.id == eventId) {
                val newJoined = !event.isJoined
                val newCount = if (newJoined) event.joinedCount + 1 else maxOf(0, event.joinedCount - 1)
                resultMsg = if (newJoined) "Registered for \"${event.title}\"! See in My Events." else "Cancelled registration for \"${event.title}\""
                val updatedChangemakers = if (newJoined) {
                    val me = Changemaker(
                        id = "user-me",
                        name = volunteerName,
                        avatar = if (volunteerName.contains("Rahul", ignoreCase = true)) "RN" else "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=80&q=80",
                        role = "Verified Volunteer",
                        badge = "Registered",
                        registeredDate = "Just now"
                    )
                    listOf(me) + event.registeredChangemakers.filter { it.name != volunteerName }
                } else {
                    event.registeredChangemakers.filter { it.name != volunteerName }
                }
                updatedChangemakersList = updatedChangemakers
                updatedCount = newCount
                event.copy(isJoined = newJoined, joinedCount = newCount, registeredChangemakers = updatedChangemakers)
            } else event
        }

        // Supabase join count synchronization
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseDataService.updateEventJoinedCount(eventId, updatedCount)
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing join count to Supabase: ${e.message}")
            }
        }

        return resultMsg
    }

    fun toggleBookmark(eventId: String): String {
        var msg = ""
        _events.value = _events.value.map { event ->
            if (event.id == eventId) {
                val newBookmarked = !event.isBookmarked
                msg = if (newBookmarked) "Saved \"${event.title}\" to bookmarks" else "Removed \"${event.title}\" from saved events"
                event.copy(isBookmarked = newBookmarked)
            } else event
        }
        return msg
    }

    fun toggleLikePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.liked
                val newLikes = post.likes + (if (newLiked) 1 else -1)
                post.copy(liked = newLiked, likes = newLikes)
            } else post
        }
    }

    fun addCommentToPost(postId: String, text: String, authorName: String = "Rahul Nile") {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = SocialComment(
                    author = authorName,
                    avatar = if (authorName.contains("Patel", ignoreCase = true)) {
                        "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=120&q=80"
                    } else {
                        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=120&q=80"
                    },
                    text = text,
                    time = "Just now"
                )
                post.copy(comments = post.comments + newComment)
            } else post
        }
    }

    fun publishPost(text: String, authorName: String = "Rahul Nile") {
        val newPost = SocialPost(
            id = "post-${System.currentTimeMillis()}",
            authorName = authorName,
            authorRole = "Volunteer",
            authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80",
            time = "Just now",
            text = text,
            images = listOf("https://images.unsplash.com/photo-1559027615-cd4628902d4a?auto=format&fit=crop&w=800&q=80"),
            likes = 1,
            liked = true
        )
        _posts.value = listOf(newPost) + _posts.value
    }

    fun toggleFollowNGO(ngoId: String): String {
        var msg = ""
        _ngos.value = _ngos.value.map { ngo ->
            if (ngo.id == ngoId) {
                val newFollow = !ngo.followed
                msg = if (newFollow) "Now following ${ngo.name}" else "Unfollowed ${ngo.name}"
                ngo.copy(followed = newFollow)
            } else ngo
        }
        return msg
    }

    fun toggleAttendance(volId: String): String {
        var msg = ""
        _roster.value = _roster.value.map { vol ->
            if (vol.id == volId) {
                val (newStatus, newHours) = when (vol.status) {
                    "Registered" -> "Attended" to 4
                    "Attended" -> "Registered" to 0
                    else -> "Registered" to 0
                }
                msg = "Marked ${vol.name} as $newStatus ($newHours hrs)"
                vol.copy(status = newStatus, hours = newHours)
            } else vol
        }
        return msg
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(unread = false) }
    }

    fun donateMoney(eventId: String, amount: Double, donorName: String = "Rahul Nile"): String {
        var resultMsg = ""
        var updatedCamp: DonationCampaign? = null
        _events.value = _events.value.map { event ->
            if (event.id == eventId) {
                val camp = event.donationCampaign
                val newRaised = camp.raisedAmount + amount
                val newDonors = camp.donorsCount + 1
                val updatedUserMoney = camp.userMoneyDonated + amount
                val updatedCampaign = camp.copy(
                    raisedAmount = newRaised,
                    donorsCount = newDonors,
                    userMoneyDonated = updatedUserMoney
                )
                updatedCamp = updatedCampaign
                resultMsg = "Thank you $donorName! Donated ${camp.currencySymbol}${amount.toInt()} to \"${event.title}\". 80G Tax receipt generated."
                event.copy(donationCampaign = updatedCampaign)
            } else event
        }

        return resultMsg
    }

    fun donateMaterial(eventId: String, itemId: String, quantity: Int, donorName: String = "Rahul Nile"): String {
        var resultMsg = ""
        _events.value = _events.value.map { event ->
            if (event.id == eventId) {
                val camp = event.donationCampaign
                var itemName = "items"
                val updatedMaterials = camp.materialItems.map { item ->
                    if (item.id == itemId) {
                        itemName = item.name
                        item.copy(collectedQuantity = item.collectedQuantity + quantity)
                    } else item
                }
                val pledgeNote = "$quantity x $itemName"
                val updatedCampaign = camp.copy(
                    materialItems = updatedMaterials,
                    userMaterialDonated = camp.userMaterialDonated + pledgeNote
                )
                resultMsg = "Thank you $donorName! Pledged $quantity of $itemName for \"${event.title}\"."
                event.copy(donationCampaign = updatedCampaign)
            } else event
        }

        return resultMsg
    }

    fun createEvent(
        title: String,
        category: String,
        dateStr: String,
        timeStr: String,
        location: String,
        maxVolunteers: Int,
        description: String,
        skills: List<String>,
        bring: List<String>,
        imageUrl: String?,
        ngoName: String = "Green Delhi Foundation",
        ngoLogo: String = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
        ngoId: String = "ngo-1"
    ) {
        val eventId = "evt-${System.currentTimeMillis()}"
        val newEvent = SocialEvent(
            id = eventId,
            title = title,
            ngoId = ngoId,
            ngoName = ngoName,
            ngoLogo = ngoLogo,
            verified = true,
            category = category,
            dateStr = dateStr,
            timeStr = timeStr,
            location = location,
            distanceKm = 4,
            joinedCount = 1,
            maxVolunteers = maxVolunteers,
            coverImg = imageUrl?.ifBlank { null }
                ?: "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=800&q=80",
            description = description,
            whatYoullDo = listOf(
                "Arrive 15 minutes before the start time for briefing",
                "Collaborate with project captains and team members",
                "Help tidy up and pack equipment post-event"
            ),
            skillsNeeded = if (skills.isEmpty()) listOf("No prior experience required") else skills,
            whatToBring = if (bring.isEmpty()) listOf("Water bottle", "Comfortable shoes") else bring,
            isJoined = false,
            isBookmarked = false,
            isWeekend = true,
            localDrawableRes = getCategoryDrawable(category),
            donationCampaign = getCampaignForEvent(eventId, category, title),
            createdAt = System.currentTimeMillis()
        )

        // 1. Immediately reflect locally so creating user sees it immediately
        _events.value = listOf(newEvent) + _events.value.filter { it.id != eventId }

        // 2. Persist to active Supabase cloud database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseDataService.createEvent(newEvent)
                Log.d(TAG, "Event $eventId written to Supabase cloud database!")
            } catch (e: Exception) {
                Log.w(TAG, "Supabase write exception for $eventId: ${e.message}")
            }
        }
    }

    companion object {
        const val COLLECTION_SOCIAL_EVENTS = "social_events"
        private const val TAG = "SocialConnectRepo"

        private fun createInitialEvents(): List<SocialEvent> = listOf(
            SocialEvent(
                id = "evt-1",
                title = "Tree Plantation Drive 2026",
                ngoId = "ngo-1",
                ngoName = "Green Delhi Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Environment",
                dateStr = "Sunday, Sep 13, 2026",
                timeStr = "7:00 AM – 11:00 AM",
                location = "Dwarka Sector 10 Eco Park, Delhi",
                distanceKm = 5,
                joinedCount = 32,
                maxVolunteers = 50,
                coverImg = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=800&q=80",
                description = "Join us for a community tree plantation drive to create a greener and healthier Delhi. Volunteers will help with planting native saplings, watering, soil conditioning, logistics, and neighborhood eco-awareness.",
                whatYoullDo = listOf(
                    "Plant native Delhi saplings (Neem, Peepal, Jamun)",
                    "Help prepare planting areas and compost beds",
                    "Educate local residents on sapling after-care",
                    "Document and photograph the community drive"
                ),
                skillsNeeded = listOf(
                    "No experience required",
                    "Photography & Social Media",
                    "Event coordination",
                    "First aid assistance"
                ),
                whatToBring = listOf(
                    "Comfortable clothing & shoes",
                    "Refillable water bottle",
                    "Sun cap / hat",
                    "Gardening gloves (optional)"
                ),
                isJoined = true,
                isBookmarked = false,
                isWeekend = true,
                registeredChangemakers = listOf(
                    Changemaker("user-me", "Rahul Nile", "RN", "Verified Volunteer", "Eco Champion", "Yesterday")
                ) + sampleChangemakers("Tree Planter", "Soil Specialist")
            ),
            SocialEvent(
                id = "evt-2",
                title = "Teach & Inspire: Weekend Math & English",
                ngoId = "ngo-2",
                ngoName = "Udaan Education Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Education",
                dateStr = "Saturday, Sep 12, 2026",
                timeStr = "10:00 AM – 1:00 PM",
                location = "Najafgarh Community Center, Delhi",
                distanceKm = 4,
                joinedCount = 12,
                maxVolunteers = 20,
                coverImg = "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=800&q=80",
                description = "Spend your Saturday morning helping underprivileged elementary students with basic reading, spoken English, and mental math through fun interactive games.",
                whatYoullDo = listOf(
                    "Lead 1-on-1 reading sessions with kids",
                    "Organize educational games and puzzles",
                    "Assist teachers with stationery distribution"
                ),
                skillsNeeded = listOf(
                    "Basic English & Hindi fluency",
                    "Patience and passion for mentoring kids",
                    "Storytelling & creative drawing"
                ),
                whatToBring = listOf(
                    "Personal ID proof",
                    "Notebook & pen",
                    "Positive energy & enthusiasm"
                ),
                isJoined = false,
                isBookmarked = true,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-3",
                title = "Community Food Distribution Drive",
                ngoId = "ngo-3",
                ngoName = "Care For All Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Food Distribution",
                dateStr = "Sunday, Sep 13, 2026",
                timeStr = "11:30 AM – 3:30 PM",
                location = "Janakpuri District Center, Delhi",
                distanceKm = 8,
                joinedCount = 20,
                maxVolunteers = 30,
                coverImg = "https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=800&q=80",
                description = "Help assemble, pack, and distribute nutritious meals and clean drinking water to over 400 homeless individuals and daily wage workers across Janakpuri.",
                whatYoullDo = listOf(
                    "Package hot meal trays in hygienic containers",
                    "Organize queues with kindness and respect",
                    "Ensure proper waste collection and segregation after distribution"
                ),
                skillsNeeded = listOf(
                    "Packing and crowd coordination",
                    "Teamwork & empathy",
                    "No prior experience required"
                ),
                whatToBring = listOf(
                    "Comfortable shoes",
                    "Hand sanitizer",
                    "Face mask (provided if needed)"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-4",
                title = "Stray Animal Health & Rescue Camp",
                ngoId = "ngo-4",
                ngoName = "PawsCare India",
                ngoLogo = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Animal Welfare",
                dateStr = "Saturday, Sep 19, 2026",
                timeStr = "9:00 AM – 1:30 PM",
                location = "Hauz Khas Village Park, Delhi",
                distanceKm = 6,
                joinedCount = 18,
                maxVolunteers = 25,
                coverImg = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80",
                description = "Support our veterinary team in conducting rabies vaccinations, deworming, and health checks for community indie dogs and rescue cats in South Delhi.",
                whatYoullDo = listOf(
                    "Assist vets during canine vaccinations",
                    "Calm and leash rescue dogs gently",
                    "Distribute reflective collars to street dogs"
                ),
                skillsNeeded = listOf(
                    "Animal lovers, comfortable handling dogs",
                    "Veterinary students welcome",
                    "Compassion and patience"
                ),
                whatToBring = listOf(
                    "Sturdy shoes & denim trousers",
                    "Water bottle"
                ),
                isJoined = false,
                isBookmarked = true,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-5",
                title = "Free Community Health & Eye Screening Camp",
                ngoId = "ngo-5",
                ngoName = "Hope & Health Mission",
                ngoLogo = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Healthcare",
                dateStr = "Sunday, Sep 20, 2026",
                timeStr = "8:30 AM – 2:00 PM",
                location = "Connaught Place Central Dispensary, Delhi",
                distanceKm = 7,
                joinedCount = 28,
                maxVolunteers = 40,
                coverImg = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=800&q=80",
                description = "Partner with certified volunteer doctors and optometrists to provide free blood pressure, sugar, and vision checks to over 500 senior citizens.",
                whatYoullDo = listOf(
                    "Manage registration desks and token allocation",
                    "Guide elderly attendees to consultation rooms",
                    "Assist with basic vital recordings"
                ),
                skillsNeeded = listOf(
                    "Good communication skills (Hindi / English)",
                    "Nursing or medical students appreciated",
                    "Helpful demeanor"
                ),
                whatToBring = listOf(
                    "Photo identification",
                    "Notebook"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-6",
                title = "Women Digital & Financial Literacy Workshop",
                ngoId = "ngo-6",
                ngoName = "Stree Shakti Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Women Empowerment",
                dateStr = "Saturday, Sep 26, 2026",
                timeStr = "11:00 AM – 3:00 PM",
                location = "South Delhi Community Hub, Delhi",
                distanceKm = 9,
                joinedCount = 14,
                maxVolunteers = 20,
                coverImg = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=800&q=80",
                description = "Help women micro-entrepreneurs and homemakers learn UPI payments, basic smartphone security, online banking, and government welfare portals.",
                whatYoullDo = listOf(
                    "1-on-1 coaching on using smartphones for digital payments",
                    "Explain safe digital habits and fraud prevention",
                    "Help participants practice creating UPI PINs safely"
                ),
                skillsNeeded = listOf(
                    "Familiarity with digital payment apps",
                    "Patience & clear communication"
                ),
                whatToBring = listOf(
                    "Smartphone or tablet with internet",
                    "Enthusiastic coaching attitude"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-7",
                title = "Clean Yamuna Riverfront Action Day",
                ngoId = "ngo-1",
                ngoName = "Green Delhi Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Environment",
                dateStr = "Sunday, Oct 4, 2026",
                timeStr = "6:30 AM – 10:00 AM",
                location = "Kashmere Gate Ghat, Yamuna Bank, Delhi",
                distanceKm = 12,
                joinedCount = 45,
                maxVolunteers = 60,
                coverImg = "https://images.unsplash.com/photo-1618477461853-cf6ed80faba5?auto=format&fit=crop&w=800&q=80",
                description = "Join our signature riverfront clean-up initiative removing single-use plastic waste, cloth debris, and non-biodegradable offerings from the Yamuna floodplain.",
                whatYoullDo = listOf(
                    "Segregate collected waste into plastic, glass, and organic",
                    "Record waste weight metrics for pollution monitoring",
                    "Participate in collective oath for clean river preservation"
                ),
                skillsNeeded = listOf(
                    "Physical stamina",
                    "Commitment to environmental conservation"
                ),
                whatToBring = listOf(
                    "Rubber boots or sturdy closed shoes",
                    "Gloves and mask (provided on site if needed)"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-8",
                title = "Emergency First Aid & Disaster Response Training",
                ngoId = "ngo-5",
                ngoName = "Hope & Health Mission",
                ngoLogo = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Disaster Relief",
                dateStr = "Saturday, Oct 10, 2026",
                timeStr = "10:00 AM – 2:00 PM",
                location = "Rohini Community Hall, Delhi",
                distanceKm = 14,
                joinedCount = 22,
                maxVolunteers = 35,
                coverImg = "https://images.unsplash.com/photo-1516574187841-cb9cc2ca948b?auto=format&fit=crop&w=800&q=80",
                description = "Hands-on disaster preparedness workshop covering basic CPR, tourniquet application, stretcher handling, and rapid community evacuation drills.",
                whatYoullDo = listOf(
                    "Practice CPR techniques on medical mannequins",
                    "Learn fracture splinting and burn management",
                    "Participate in simulated emergency drill"
                ),
                skillsNeeded = listOf(
                    "No medical background required",
                    "Eagerness to learn lifesaving techniques"
                ),
                whatToBring = listOf(
                    "Comfortable athletic clothing",
                    "Notebook"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true
            ),
            SocialEvent(
                id = "evt-9",
                title = "Elder Care & Musical Morning at Anand Dham",
                ngoId = "ngo-3",
                ngoName = "Care For All Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Healthcare",
                dateStr = "Saturday, Sep 19, 2026",
                timeStr = "9:30 AM – 12:30 PM",
                location = "Sector 23 Senior Care Home, Dwarka, Delhi",
                distanceKm = 3,
                joinedCount = 15,
                maxVolunteers = 25,
                coverImg = "https://images.unsplash.com/photo-1516307365426-bea591f05011?auto=format&fit=crop&w=800&q=80",
                description = "Spend heartwarming weekend hours bringing joy to 60 elderly residents through live acoustic melodies, board games, poetry recitals, and serving fresh breakfast.",
                whatYoullDo = listOf(
                    "Play music, sing classic songs, or recite poetry",
                    "Play carrom, chess, and card games with seniors",
                    "Help distribute healthy fruit bowls and herbal tea",
                    "Offer attentive listening and companionship"
                ),
                skillsNeeded = listOf(
                    "Empathy and patience",
                    "Musical or storytelling talent is a plus",
                    "Respectful communication"
                ),
                whatToBring = listOf(
                    "Acoustic instruments (if playing)",
                    "Smiling warmth and positive energy"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true,
                registeredChangemakers = sampleChangemakers("Caregiver Volunteer", "Seniors Ally")
            ),
            SocialEvent(
                id = "evt-10",
                title = "Solar Lantern Assembly & Rural Clean Energy",
                ngoId = "ngo-1",
                ngoName = "Green Delhi Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Environment",
                dateStr = "Saturday, Oct 17, 2026",
                timeStr = "10:00 AM – 2:00 PM",
                location = "Okhla Industrial Area Phase 2, South Delhi",
                distanceKm = 7,
                joinedCount = 18,
                maxVolunteers = 30,
                coverImg = "https://images.unsplash.com/photo-1508873696983-2df570464756?auto=format&fit=crop&w=800&q=80",
                description = "Assemble portable solar-powered LED study lanterns for rural students in unelectrified peri-urban settlements. Learn basic circuit soldering with guidance from green energy engineers.",
                whatYoullDo = listOf(
                    "Assemble mini photovoltaic solar panels and rechargeable cells",
                    "Test LED brightness, casing durability, and battery efficiency",
                    "Pack instructional guides in Hindi and English"
                ),
                skillsNeeded = listOf(
                    "Hands-on assembly curiosity",
                    "Basic electronic safety awareness",
                    "Teamwork & careful handling"
                ),
                whatToBring = listOf(
                    "Valid photo ID",
                    "Safety glasses (optional, provided on-site)"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true,
                registeredChangemakers = sampleChangemakers("Clean Tech Ally", "Solar Builder")
            ),
            SocialEvent(
                id = "evt-11",
                title = "Stray Animal Rescue & Winter Feeding Drive",
                ngoId = "ngo-4",
                ngoName = "Paws & Tails Welfare",
                ngoLogo = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Animal Welfare",
                dateStr = "Sunday, Oct 18, 2026",
                timeStr = "8:00 AM – 11:30 AM",
                location = "Hauz Khas Village & Deer Park, South Delhi",
                distanceKm = 5,
                joinedCount = 22,
                maxVolunteers = 35,
                coverImg = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80",
                description = "Join animal caretakers to prepare and distribute high-protein meals for stray community dogs and cats. We will also inspect animals for seasonal wounds, apply antiseptic spray, and install reflective safety collars.",
                whatYoullDo = listOf(
                    "Mix boiled chicken, rice, and vitamin kibble portions",
                    "Distribute meals at 8 designated neighborhood feeding stations",
                    "Affix luminous reflective safety collars for night road safety",
                    "Report sick or injured animals to the emergency mobile veterinary team"
                ),
                skillsNeeded = listOf(
                    "Gentle demeanor around friendly street animals",
                    "Basic handling comfort",
                    "No fear of friendly dogs"
                ),
                whatToBring = listOf(
                    "Clothes you don't mind getting dusty",
                    "Hand towel & sanitizer",
                    "Water bottle"
                ),
                isJoined = false,
                isBookmarked = true,
                isWeekend = true,
                registeredChangemakers = sampleChangemakers("Paws Protector", "Animal Healer")
            ),
            SocialEvent(
                id = "evt-12",
                title = "Community Blood Donation & Free Health Camp",
                ngoId = "ngo-3",
                ngoName = "Care For All Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Healthcare",
                dateStr = "Saturday, Oct 24, 2026",
                timeStr = "9:00 AM – 1:30 PM",
                location = "Red Cross Bhawan, Connaught Place, Central Delhi",
                distanceKm = 9,
                joinedCount = 28,
                maxVolunteers = 40,
                coverImg = "https://images.unsplash.com/photo-1615461066841-6116e61058f4?auto=format&fit=crop&w=800&q=80",
                description = "Assist certified doctors and nurses in hosting a comprehensive community health camp. We are offering free vitals checks, blood sugar testing, and organizing a voluntary donor drive for thalassemic children.",
                whatYoullDo = listOf(
                    "Register donors and community walk-ins digitally",
                    "Manage donor resting lounge with fruit juice and biscuits",
                    "Guide senior citizens to blood pressure screening stations",
                    "Distribute free hygiene packs"
                ),
                skillsNeeded = listOf(
                    "Warm communication and active listening",
                    "Patience and crowd guidance",
                    "Medical students or first aiders welcomed"
                ),
                whatToBring = listOf(
                    "Comfortable shoes",
                    "Notebook & mobile phone"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true,
                registeredChangemakers = sampleChangemakers("Life Saver", "Health Hero")
            ),
            SocialEvent(
                id = "evt-13",
                title = "Girls Coding & STEM Mentorship Workshop",
                ngoId = "ngo-2",
                ngoName = "Udaan Education Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Education",
                dateStr = "Sunday, Oct 25, 2026",
                timeStr = "11:00 AM – 3:00 PM",
                location = "Sector 62 Community Tech Lab, Noida",
                distanceKm = 12,
                joinedCount = 14,
                maxVolunteers = 20,
                coverImg = "https://images.unsplash.com/photo-1531482615713-2afd69097998?auto=format&fit=crop&w=800&q=80",
                description = "Empower young female high school students from underserved communities through practical coding skills, block programming, and inspiring career dialogues with women tech leaders.",
                whatYoullDo = listOf(
                    "Sit 1-on-1 with students to help them build their first interactive web page",
                    "Debug basic HTML and Python syntax questions",
                    "Share personal education and career stories to inspire confidence"
                ),
                skillsNeeded = listOf(
                    "Basic coding knowledge (HTML/Python/Scratch)",
                    "Friendly mentoring attitude",
                    "Empathy and patience"
                ),
                whatToBring = listOf(
                    "Personal laptop with charger (optional)",
                    "Enthusiastic mentorship spirit"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true,
                registeredChangemakers = sampleChangemakers("Tech Mentor", "STEM Advocate")
            ),
            SocialEvent(
                id = "evt-14",
                title = "Night Shelter Warm Blanket & Hot Meal Distribution",
                ngoId = "ngo-3",
                ngoName = "Care For All Foundation",
                ngoLogo = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80",
                verified = true,
                category = "Food Distribution",
                dateStr = "Saturday, Oct 31, 2026",
                timeStr = "6:30 PM – 9:30 PM",
                location = "Old Delhi Railway Station Shelter Zone, Delhi",
                distanceKm = 10,
                joinedCount = 34,
                maxVolunteers = 45,
                coverImg = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=800&q=80",
                description = "As temperatures drop at night, volunteer teams will deliver high-quality thermal blankets, warm woollen socks, and freshly cooked vegetable khichdi to pavement dwellers and families at shelter transit zones.",
                whatYoullDo = listOf(
                    "Sort thermal blankets by size and pack into distribution bundles",
                    "Serve fresh hot meals in compostable plates with warm tea",
                    "Interact with shelter supervisors to note medical needs"
                ),
                skillsNeeded = listOf(
                    "Night drive comfort",
                    "Compassionate and respectful demeanor",
                    "Team player"
                ),
                whatToBring = listOf(
                    "Warm personal jacket / sweater",
                    "Comfortable walking shoes"
                ),
                isJoined = false,
                isBookmarked = false,
                isWeekend = true,
                registeredChangemakers = sampleChangemakers("Warmth Giver", "Compassion Star")
            )
        ).map { it.copy(donationCampaign = getCampaignForEvent(it.id, it.category, it.title)) }

        fun getCampaignForEvent(eventId: String, category: String, title: String): DonationCampaign {
            return when (eventId) {
                "evt-2" -> DonationCampaign(
                    targetAmount = 30000.0,
                    raisedAmount = 21500.0,
                    donorsCount = 38,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt2-1", "Study Material & Notebooks", "Study Material", neededQuantity = 100, collectedQuantity = 68, unit = "sets", urgency = "Urgent", iconEmoji = "📚", description = "Ruled notebooks, pencil pouches, sharpeners, and erasers"),
                        MaterialDonationItem("mat-evt2-2", "Educational Toys & STEM Puzzles", "Toys", neededQuantity = 50, collectedQuantity = 32, unit = "packs", urgency = "High Priority", iconEmoji = "🧸", description = "Building blocks, shape sorters, learning puzzles, and flashcards"),
                        MaterialDonationItem("mat-evt2-3", "Children's Storybooks (Hindi & English)", "Books", neededQuantity = 120, collectedQuantity = 94, unit = "books", urgency = "Normal", iconEmoji = "📖", description = "Illustrated folktales, beginner moral stories, and comics"),
                        MaterialDonationItem("mat-evt2-4", "Coloring Kits & Drawing Books", "Study Material", neededQuantity = 80, collectedQuantity = 55, unit = "kits", urgency = "Normal", iconEmoji = "🎨", description = "Crayons, sketch pens, watercolor sets, and drawing pads")
                    )
                )
                "evt-1" -> DonationCampaign(
                    targetAmount = 45000.0,
                    raisedAmount = 32400.0,
                    donorsCount = 52,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt1-1", "Native Neem & Peepal Saplings", "Plantation Supplies", neededQuantity = 200, collectedQuantity = 145, unit = "saplings", urgency = "Urgent", iconEmoji = "🌱", description = "1.5ft to 2ft healthy nursery saplings with root balls"),
                        MaterialDonationItem("mat-evt1-2", "Protective Metal Tree Guards", "Equipment", neededQuantity = 50, collectedQuantity = 31, unit = "guards", urgency = "Urgent", iconEmoji = "🛡️", description = "Cylindrical mesh guards to safeguard young trees from cattle"),
                        MaterialDonationItem("mat-evt1-3", "Organic Vermicompost Bags (10kg)", "Plantation Supplies", neededQuantity = 60, collectedQuantity = 42, unit = "bags", urgency = "High Priority", iconEmoji = "🪴", description = "Enriched nutrient soil and manure for root nourishment"),
                        MaterialDonationItem("mat-evt1-4", "Gardening Hand Trowels & Gloves", "Tools", neededQuantity = 40, collectedQuantity = 35, unit = "sets", urgency = "Normal", iconEmoji = "🧤", description = "Rubber-coated garden gloves and heavy-duty steel trowels")
                    )
                )
                "evt-3" -> DonationCampaign(
                    targetAmount = 50000.0,
                    raisedAmount = 41000.0,
                    donorsCount = 72,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt3-1", "Dry Ration Family Kits", "Food Supplies", neededQuantity = 150, collectedQuantity = 118, unit = "kits", urgency = "Urgent", iconEmoji = "🍚", description = "Contains 5kg wheat flour, 2kg rice, 1kg pulses, 1L cooking oil"),
                        MaterialDonationItem("mat-evt3-2", "Clean Drinking Water Jars (20L)", "Hydration", neededQuantity = 100, collectedQuantity = 88, unit = "jars", urgency = "High Priority", iconEmoji = "💧", description = "Purified sealed potable drinking water cans"),
                        MaterialDonationItem("mat-evt3-3", "Nutrition Fruit Baskets & Biscuits", "Nutrition", neededQuantity = 80, collectedQuantity = 64, unit = "packs", urgency = "Normal", iconEmoji = "🍎", description = "Bananas, apples, glucose biscuits, and fortified milk tetrapacks"),
                        MaterialDonationItem("mat-evt3-4", "Eco-friendly Meal Containers", "Packaging", neededQuantity = 500, collectedQuantity = 420, unit = "boxes", urgency = "Normal", iconEmoji = "🍱", description = "Bagasse sugarcane-pulp biodegradable meal boxes")
                    )
                )
                "evt-4" -> DonationCampaign(
                    targetAmount = 35000.0,
                    raisedAmount = 24800.0,
                    donorsCount = 44,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt4-1", "Chew Toys & Squeaky Balls for Rescue Pups", "Toys", neededQuantity = 60, collectedQuantity = 28, unit = "toys", urgency = "High Priority", iconEmoji = "🧸", description = "Non-toxic rubber chew toys and durable rope tugs for shelter dogs"),
                        MaterialDonationItem("mat-evt4-2", "Dog & Cat Kibble Food Bags (15kg)", "Pet Food", neededQuantity = 40, collectedQuantity = 29, unit = "bags", urgency = "Urgent", iconEmoji = "🥣", description = "Nutritionally balanced adult dog and puppy dry food kibble"),
                        MaterialDonationItem("mat-evt4-3", "Night Reflective Collars & Leashes", "Safety Gear", neededQuantity = 100, collectedQuantity = 74, unit = "collars", urgency = "High Priority", iconEmoji = "🦺", description = "High-visibility reflective collars to protect street animals"),
                        MaterialDonationItem("mat-evt4-4", "Antiseptic Sprays & Healing Ointments", "Medical", neededQuantity = 50, collectedQuantity = 38, unit = "bottles", urgency = "Urgent", iconEmoji = "🩹", description = "Mag-spray, betadine, surgical cotton, and gauze rolls")
                    )
                )
                "evt-5" -> DonationCampaign(
                    targetAmount = 45000.0,
                    raisedAmount = 31500.0,
                    donorsCount = 56,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt5-1", "Prescription & Reading Glasses", "Optical", neededQuantity = 100, collectedQuantity = 68, unit = "pairs", urgency = "Urgent", iconEmoji = "👓", description = "Standard magnification spectacles (+1.0 to +3.5) for elders"),
                        MaterialDonationItem("mat-evt5-2", "Digital BP Monitors & Glucometer Strips", "Diagnostics", neededQuantity = 40, collectedQuantity = 28, unit = "kits", urgency = "Urgent", iconEmoji = "🩺", description = "Automatic blood pressure cuffs and blood glucose test strips"),
                        MaterialDonationItem("mat-evt5-3", "Children's Waiting Toys & Coloring Books", "Toys", neededQuantity = 35, collectedQuantity = 19, unit = "sets", urgency = "Normal", iconEmoji = "🧸", description = "Comfort toys, wooden puzzles and coloring pads for kids in line"),
                        MaterialDonationItem("mat-evt5-4", "Senior Medical Care & Hygiene Kits", "Hygiene", neededQuantity = 75, collectedQuantity = 55, unit = "kits", urgency = "Normal", iconEmoji = "🧴", description = "Hand sanitizers, pain relief balms, and multivitamins")
                    )
                )
                "evt-6" -> DonationCampaign(
                    targetAmount = 32000.0,
                    raisedAmount = 21000.0,
                    donorsCount = 35,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt6-1", "Study Material & Business Literacy Booklets", "Study Material", neededQuantity = 80, collectedQuantity = 58, unit = "sets", urgency = "Urgent", iconEmoji = "📚", description = "Workbooks on bookkeeping, digital banking, and government schemes"),
                        MaterialDonationItem("mat-evt6-2", "Activity & Play Toys for Participant Kids", "Toys", neededQuantity = 35, collectedQuantity = 20, unit = "packs", urgency = "Normal", iconEmoji = "🧸", description = "Keeps mothers' young children happily engaged during classes"),
                        MaterialDonationItem("mat-evt6-3", "Financial Calculators & Register Ledgers", "Supplies", neededQuantity = 50, collectedQuantity = 36, unit = "units", urgency = "High Priority", iconEmoji = "📱", description = "Handheld desktop calculators and hardbound ledger books")
                    )
                )
                "evt-14" -> DonationCampaign(
                    targetAmount = 40000.0,
                    raisedAmount = 34500.0,
                    donorsCount = 61,
                    materialItems = listOf(
                        MaterialDonationItem("mat-evt14-1", "Thermal Fleece Blankets", "Clothing & Warmth", neededQuantity = 150, collectedQuantity = 115, unit = "blankets", urgency = "Urgent", iconEmoji = "🧣", description = "Heavyweight winter fleece blankets for transit shelters"),
                        MaterialDonationItem("mat-evt14-2", "Children's Warm Clothing & Soft Toys", "Toys", neededQuantity = 40, collectedQuantity = 25, unit = "kits", urgency = "High Priority", iconEmoji = "🧸", description = "Warm woolen beanies, mittens, and plush huggable toys for shelter children"),
                        MaterialDonationItem("mat-evt14-3", "Hot Meal & Vegetable Khichdi Servings", "Food", neededQuantity = 250, collectedQuantity = 200, unit = "servings", urgency = "Normal", iconEmoji = "🍲", description = "Freshly prepared nutritious warm khichdi with ghee and pickle")
                    )
                )
                else -> createDefaultCampaignForCategory(category, title)
            }
        }

        fun createDefaultCampaignForCategory(category: String, title: String): DonationCampaign {
            val catLower = category.lowercase()
            return when {
                catLower.contains("education") || catLower.contains("teach") || catLower.contains("school") -> {
                    DonationCampaign(
                        targetAmount = 25000.0,
                        raisedAmount = 18000.0,
                        donorsCount = 29,
                        materialItems = listOf(
                            MaterialDonationItem("mat-def-edu-1", "Study Material & Exercise Notebooks", "Study Material", neededQuantity = 80, collectedQuantity = 54, unit = "sets", urgency = "Urgent", iconEmoji = "📚", description = "Writing notebooks, pencils, erasers, and geometry instruments"),
                            MaterialDonationItem("mat-def-edu-2", "Educational Toys, Blocks & Puzzles", "Toys", neededQuantity = 40, collectedQuantity = 25, unit = "packs", urgency = "High Priority", iconEmoji = "🧸", description = "Constructive wooden toys and logic games for young minds"),
                            MaterialDonationItem("mat-def-edu-3", "Drawing Kits & Sketch Books", "Art & Craft", neededQuantity = 60, collectedQuantity = 42, unit = "kits", urgency = "Normal", iconEmoji = "🎨", description = "Color pencils, wax crayons, and drawing sketchbooks")
                        )
                    )
                }
                catLower.contains("food") || catLower.contains("hunger") || catLower.contains("meal") -> {
                    DonationCampaign(
                        targetAmount = 35000.0,
                        raisedAmount = 26500.0,
                        donorsCount = 42,
                        materialItems = listOf(
                            MaterialDonationItem("mat-def-food-1", "Grain & Ration Grocery Packs", "Food", neededQuantity = 100, collectedQuantity = 75, unit = "packs", urgency = "Urgent", iconEmoji = "🍚", description = "Staple grains, lentils, salt, spices, and cooking oil"),
                            MaterialDonationItem("mat-def-food-2", "Child Nutrition Biscuits & Milk Kits", "Nutrition", neededQuantity = 60, collectedQuantity = 45, unit = "kits", urgency = "High Priority", iconEmoji = "🥛", description = "Fortified milk and high-protein biscuits"),
                            MaterialDonationItem("mat-def-food-3", "Hygiene & Sanitizer Packs", "Hygiene", neededQuantity = 80, collectedQuantity = 62, unit = "packs", urgency = "Normal", iconEmoji = "🧼", description = "Handwashing soap bars and antiseptic wipes")
                        )
                    )
                }
                catLower.contains("animal") || catLower.contains("pet") || catLower.contains("dog") -> {
                    DonationCampaign(
                        targetAmount = 30000.0,
                        raisedAmount = 22000.0,
                        donorsCount = 36,
                        materialItems = listOf(
                            MaterialDonationItem("mat-def-anim-1", "Chew Toys & Dog Activity Toys", "Toys", neededQuantity = 45, collectedQuantity = 26, unit = "toys", urgency = "High Priority", iconEmoji = "🧸", description = "Durable chew ropes and squeaker balls for rescue animals"),
                            MaterialDonationItem("mat-def-anim-2", "Puppy & Dog Kibble Food", "Food", neededQuantity = 35, collectedQuantity = 24, unit = "bags", urgency = "Urgent", iconEmoji = "🥣", description = "Nutritious dry pet food bags"),
                            MaterialDonationItem("mat-def-anim-3", "Medical First Aid & Healing Sprays", "Medical", neededQuantity = 40, collectedQuantity = 30, unit = "kits", urgency = "Urgent", iconEmoji = "🩹", description = "Wound healing sprays, gauze bandages, and deworming tablets")
                        )
                    )
                }
                catLower.contains("environment") || catLower.contains("tree") || catLower.contains("clean") -> {
                    DonationCampaign(
                        targetAmount = 35000.0,
                        raisedAmount = 24000.0,
                        donorsCount = 37,
                        materialItems = listOf(
                            MaterialDonationItem("mat-def-env-1", "Healthy Native Plant Saplings", "Plantation Supplies", neededQuantity = 120, collectedQuantity = 82, unit = "saplings", urgency = "Urgent", iconEmoji = "🌱", description = "Neem, Ashoka, Jamun, and fruit saplings"),
                            MaterialDonationItem("mat-def-env-2", "Organic Compost & Soil Fertilizer", "Plantation Supplies", neededQuantity = 50, collectedQuantity = 34, unit = "bags", urgency = "High Priority", iconEmoji = "🪴", description = "Natural vermicompost bags"),
                            MaterialDonationItem("mat-def-env-3", "Plantation Gloves & Shovels", "Tools", neededQuantity = 35, collectedQuantity = 28, unit = "sets", urgency = "Normal", iconEmoji = "🧤", description = "Gardening equipment sets")
                        )
                    )
                }
                else -> {
                    DonationCampaign(
                        targetAmount = 25000.0,
                        raisedAmount = 16500.0,
                        donorsCount = 24,
                        materialItems = listOf(
                            MaterialDonationItem("mat-def-gen-1", "Study Material & Notebooks", "Study Material", neededQuantity = 60, collectedQuantity = 40, unit = "sets", urgency = "High Priority", iconEmoji = "📚", description = "Educational supplies and writing kits"),
                            MaterialDonationItem("mat-def-gen-2", "Educational Toys & Games", "Toys", neededQuantity = 35, collectedQuantity = 21, unit = "packs", urgency = "Normal", iconEmoji = "🧸", description = "Play and learning toys for community children"),
                            MaterialDonationItem("mat-def-gen-3", "Community Care Kits", "Supplies", neededQuantity = 50, collectedQuantity = 36, unit = "kits", urgency = "Normal", iconEmoji = "📦", description = "Essential support items and first-aid kits")
                        )
                    )
                }
            }
        }

        private fun sampleChangemakers(badge1: String, badge2: String): List<Changemaker> = listOf(
            Changemaker("cm-1", "Ananya Verma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=80&q=80", "Volunteer Coordinator", badge1, "2 days ago"),
            Changemaker("cm-2", "Rohan Gupta", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80", "Youth Volunteer", badge2, "3 days ago"),
            Changemaker("cm-3", "Priya Sharma", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=80&q=80", "Active Changemaker", "Weekend Hero", "4 days ago"),
            Changemaker("cm-4", "Vikram Mehta", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=80&q=80", "Logistics Volunteer", "Community Star", "5 days ago"),
            Changemaker("cm-5", "Kavita Joshi", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=80&q=80", "Grassroots Volunteer", "Eco Champion", "6 days ago")
        )

        private fun createInitialCompletedEvents(): List<SocialCompletedEvent> = listOf(
            SocialCompletedEvent(
                id = "comp-1",
                title = "Pre-Monsoon Sapling Plantation",
                ngoName = "Green Delhi Foundation",
                dateStr = "August 24, 2026",
                hours = 4,
                category = "Environment",
                thumbImg = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=180&q=80"
            ),
            SocialCompletedEvent(
                id = "comp-2",
                title = "Slum Youth Science Mentorship",
                ngoName = "Udaan Education Foundation",
                dateStr = "July 12, 2026",
                hours = 3,
                category = "Education",
                thumbImg = "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=180&q=80"
            ),
            SocialCompletedEvent(
                id = "comp-3",
                title = "Weekend Community Meal Packaging",
                ngoName = "Care For All Foundation",
                dateStr = "June 28, 2026",
                hours = 5,
                category = "Food Distribution",
                thumbImg = "https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=180&q=80"
            )
        )

        private fun createInitialNGOs(): List<SocialNGO> = listOf(
            SocialNGO(
                id = "ngo-1",
                name = "Green Delhi Foundation",
                verified = true,
                logo = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
                cover = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=800&q=80",
                desc = "Working towards a cleaner, greener, and healthier Delhi through tree plantations, waste segregation awareness, and urban farming.",
                causes = listOf("🌱 Environment", "🌳 Sustainability"),
                volunteersCount = 1250,
                eventsCount = 35,
                hoursCount = 8500,
                followed = true,
                location = "Dwarka, Delhi"
            ),
            SocialNGO(
                id = "ngo-2",
                name = "Udaan Education Foundation",
                verified = true,
                logo = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=120&q=80",
                cover = "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=800&q=80",
                desc = "Dedicated to providing high-quality remedial education, mentorship, and life-skills training to children from low-income communities.",
                causes = listOf("📚 Education", "🎨 Child Mentorship"),
                volunteersCount = 850,
                eventsCount = 28,
                hoursCount = 5200,
                followed = false,
                location = "Najafgarh, Delhi"
            ),
            SocialNGO(
                id = "ngo-3",
                name = "Care For All Foundation",
                verified = true,
                logo = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80",
                cover = "https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=800&q=80",
                desc = "Fighting food waste and hunger by operating daily food rescue vans and supplying hot nutritious meals across Delhi NCR.",
                causes = listOf("🍱 Food Distribution", "🧑🤝🧑 Community Support"),
                volunteersCount = 1100,
                eventsCount = 42,
                hoursCount = 7900,
                followed = false,
                location = "Janakpuri, Delhi"
            ),
            SocialNGO(
                id = "ngo-4",
                name = "PawsCare India",
                verified = true,
                logo = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=120&q=80",
                cover = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?auto=format&fit=crop&w=800&q=80",
                desc = "Rescuing, rehabilitating, and vaccinating community indie street dogs and cats. Promoting humane coexistence and adoption.",
                causes = listOf("🐶 Animal Welfare", "🏥 Veterinary Care"),
                volunteersCount = 420,
                eventsCount = 19,
                hoursCount = 3100,
                followed = false,
                location = "Hauz Khas, Delhi"
            ),
            SocialNGO(
                id = "ngo-5",
                name = "Hope & Health Mission",
                verified = true,
                logo = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=120&q=80",
                cover = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=800&q=80",
                desc = "Organizing free doorstep medical checkups, cataract screenings, and subsidized medicine distributions for disadvantaged citizens.",
                causes = listOf("🏥 Healthcare", "🚨 Disaster Relief"),
                volunteersCount = 680,
                eventsCount = 24,
                hoursCount = 4800,
                followed = false,
                location = "Central Delhi"
            ),
            SocialNGO(
                id = "ngo-6",
                name = "Stree Shakti Foundation",
                verified = true,
                logo = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=120&q=80",
                cover = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=800&q=80",
                desc = "Championing women economic independence through vocational skills, digital literacy workshops, and legal rights awareness.",
                causes = listOf("👩 Women Empowerment", "💼 Skill Training"),
                volunteersCount = 530,
                eventsCount = 16,
                hoursCount = 3900,
                followed = false,
                location = "South Delhi"
            )
        )

        private fun createInitialPosts(): List<SocialPost> = listOf(
            SocialPost(
                id = "post-1",
                authorName = "Green Delhi Foundation",
                authorRole = "Verified NGO",
                authorAvatar = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
                time = "3 hours ago",
                text = "Today our amazing volunteers planted 500 native saplings in Dwarka! 🌱 Massive thank you to Rahul, Ananya, and the 30 other changemakers who showed up at 7 AM on a Sunday. Delhi breathes a little easier because of you!",
                images = listOf(
                    "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1559027615-cd4628902d4a?auto=format&fit=crop&w=600&q=80"
                ),
                likes = 142,
                liked = false,
                comments = listOf(
                    SocialComment(
                        author = "Rahul Nile",
                        avatar = "RN",
                        text = "Proud to be a part of this drive! The enthusiasm was unbelievable 🙌",
                        time = "2 hours ago"
                    ),
                    SocialComment(
                        author = "Ananya Verma",
                        avatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=80&q=80",
                        text = "Already looking forward to the tree maintenance checkup next week!",
                        time = "1 hour ago"
                    )
                )
            ),
            SocialPost(
                id = "post-2",
                authorName = "Rahul Nile",
                authorRole = "Volunteer",
                authorAvatar = "RN",
                time = "Yesterday",
                text = "Spent my Saturday morning teaching basic math and English puzzles to the kids at Udaan. Seeing a 9-year-old solve a riddle and light up with confidence is pure joy! If you have 2 hours to spare this weekend, please volunteer. 📚✨",
                images = listOf(
                    "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=800&q=80"
                ),
                likes = 68,
                liked = true,
                comments = listOf(
                    SocialComment(
                        author = "Udaan Team",
                        avatar = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?auto=format&fit=crop&w=80&q=80",
                        text = "Thank you Rahul! The kids love your storytelling sessions!",
                        time = "Yesterday"
                    )
                )
            ),
            SocialPost(
                id = "post-3",
                authorName = "Care For All Foundation",
                authorRole = "Verified NGO",
                authorAvatar = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=120&q=80",
                time = "2 days ago",
                text = "Over 450 freshly packed meals and clean drinking water kits distributed this morning across West Delhi. Big love to all the university volunteers who helped package and load the vans! 🍱❤️",
                images = listOf(
                    "https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=800&q=80"
                ),
                likes = 94,
                liked = false
            )
        )

        private fun createInitialRoster(): List<VolunteerRosterItem> = listOf(
            VolunteerRosterItem("vol-1", "Rahul Nile", "RN", "Photography, Fieldwork", "Sep 01, 2026", "Tree Plantation Drive 2026", "Registered", 4),
            VolunteerRosterItem("vol-2", "Ananya Verma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=80&q=80", "Event Logistics", "Sep 02, 2026", "Tree Plantation Drive 2026", "Attended", 4),
            VolunteerRosterItem("vol-3", "Vikram Mehta", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=80&q=80", "First Aid, Gardening", "Sep 03, 2026", "Tree Plantation Drive 2026", "Attended", 4),
            VolunteerRosterItem("vol-4", "Pooja Roy", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=80&q=80", "Social Media", "Sep 03, 2026", "Clean Yamuna Riverfront", "Registered", 0),
            VolunteerRosterItem("vol-5", "Sameer Joshi", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=80&q=80", "Crowd Coordination", "Aug 29, 2026", "Clean Yamuna Riverfront", "Registered", 0),
            VolunteerRosterItem("vol-6", "Neha Saxena", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=80&q=80", "Teaching, Mentoring", "Aug 20, 2026", "Pre-Monsoon Plantation", "Cancelled", 0),
            VolunteerRosterItem("vol-7", "Rohan Gupta", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80", "Field Operations", "Aug 15, 2026", "Tree Plantation Drive 2026", "Attended", 6),
            VolunteerRosterItem("vol-8", "Priya Sharma", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=80&q=80", "Youth Mentorship", "Aug 10, 2026", "Teach & Inspire Weekend", "Attended", 8)
        )

        private fun createInitialNotifications(): List<SocialNotificationItem> = listOf(
            SocialNotificationItem(
                id = "notif-1",
                icon = "🌱",
                title = "New Event Near You",
                desc = "An Environment drive matching your passions was posted 5 km away in Dwarka.",
                time = "10 mins ago",
                unread = true,
                action = "explore"
            ),
            SocialNotificationItem(
                id = "notif-2",
                icon = "⏰",
                title = "Upcoming Event Reminder",
                desc = "Tree Plantation Drive 2026 starts Sunday at 7:00 AM. Don't forget your water bottle!",
                time = "2 hours ago",
                unread = true,
                action = "my-events"
            ),
            SocialNotificationItem(
                id = "notif-3",
                icon = "📢",
                title = "NGO Impact Update",
                desc = "Green Delhi Foundation posted photos from the 500 saplings milestone.",
                time = "5 hours ago",
                unread = true,
                action = "community"
            )
        )
    }
}
