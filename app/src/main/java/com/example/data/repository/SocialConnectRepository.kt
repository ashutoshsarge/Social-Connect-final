package com.example.data.repository

import com.example.data.model.SocialComment
import com.example.data.model.SocialCompletedEvent
import com.example.data.model.SocialEvent
import com.example.data.model.SocialNGO
import com.example.data.model.SocialNotificationItem
import com.example.data.model.SocialPost
import com.example.data.model.VolunteerRosterItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SocialConnectRepository {

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

    fun toggleJoinEvent(eventId: String): String {
        var resultMsg = ""
        _events.value = _events.value.map { event ->
            if (event.id == eventId) {
                val newJoined = !event.isJoined
                val newCount = if (newJoined) event.joinedCount + 1 else maxOf(0, event.joinedCount - 1)
                resultMsg = if (newJoined) "Registered for \"${event.title}\"! See in My Events." else "Cancelled registration for \"${event.title}\""
                event.copy(isJoined = newJoined, joinedCount = newCount)
            } else event
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

    fun addCommentToPost(postId: String, text: String, authorName: String = "Diya Sarge") {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newComment = SocialComment(
                    author = authorName,
                    avatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=80&q=80",
                    text = text,
                    time = "Just now"
                )
                post.copy(comments = post.comments + newComment)
            } else post
        }
    }

    fun publishPost(text: String, authorName: String = "Diya Sarge") {
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
        imageUrl: String?
    ) {
        val newEvent = SocialEvent(
            id = "evt-${System.currentTimeMillis()}",
            title = title,
            ngoId = "ngo-1",
            ngoName = "Green Delhi Foundation",
            ngoLogo = "https://images.unsplash.com/photo-1579208575657-c595a05383b7?auto=format&fit=crop&w=120&q=80",
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
            isWeekend = true
        )
        _events.value = listOf(newEvent) + _events.value
    }

    companion object {
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
                isWeekend = true
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
                isWeekend = true
            )
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
                        author = "Diya Sarge",
                        avatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=80&q=80",
                        text = "Proud to be a part of this drive! The enthusiasm was unbelievable 🙌",
                        time = "2 hours ago"
                    ),
                    SocialComment(
                        author = "Ananya Verma",
                        avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80",
                        text = "Already looking forward to the tree maintenance checkup next week!",
                        time = "1 hour ago"
                    )
                )
            ),
            SocialPost(
                id = "post-2",
                authorName = "Diya Sarge",
                authorRole = "Volunteer",
                authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80",
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
                        avatar = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=80&q=80",
                        text = "Thank you Diya! The kids love your storytelling sessions!",
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
            VolunteerRosterItem("vol-1", "Diya Sarge", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=80&q=80", "Photography, Fieldwork", "Sep 01, 2026", "Tree Plantation Drive 2026", "Registered", 4),
            VolunteerRosterItem("vol-2", "Ananya Verma", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=80&q=80", "Event Logistics", "Sep 02, 2026", "Tree Plantation Drive 2026", "Attended", 4),
            VolunteerRosterItem("vol-3", "Vikram Mehta", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=80&q=80", "First Aid, Gardening", "Sep 03, 2026", "Tree Plantation Drive 2026", "Attended", 4),
            VolunteerRosterItem("vol-4", "Pooja Roy", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=80&q=80", "Social Media", "Sep 03, 2026", "Clean Yamuna Riverfront", "Registered", 0),
            VolunteerRosterItem("vol-5", "Sameer Joshi", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=80&q=80", "Crowd Coordination", "Aug 29, 2026", "Clean Yamuna Riverfront", "Registered", 0),
            VolunteerRosterItem("vol-6", "Neha Saxena", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=80&q=80", "Teaching, Mentoring", "Aug 20, 2026", "Pre-Monsoon Plantation", "Cancelled", 0)
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
