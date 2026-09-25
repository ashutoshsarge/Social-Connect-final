package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Data model representing a user document persisted in Firebase Firestore ("users" collection).
 * Enables real-time synchronization of user profiles, volunteer hours, badges, and drive registrations.
 */
@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "VOLUNTEER", // "VOLUNTEER" or "NGO_LEADER"
    val organization: String = "",
    val volunteerHours: Int = 0,
    val badges: List<String> = emptyList(),
    val phone: String = "",
    val joinedEventIds: List<String> = emptyList(),
    val bookmarkedEventIds: List<String> = emptyList(),
    val causes: List<String> = listOf("Environment", "Education", "Animal Welfare", "Food Distribution"),
    val preferredLocation: String = "Delhi",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
