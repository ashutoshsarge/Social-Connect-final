package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Data model representing a volunteer event stored in Firebase Firestore.
 * Default values are provided for all fields to allow Firestore's automatic
 * serialization and deserialization using [com.google.firebase.firestore.DocumentSnapshot.toObject].
 */
@IgnoreExtraProperties
data class VolunteerEvent(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ngoId: String = "",
    val ngoName: String = "",
    val category: String = "",
    val location: String = "",
    val dateStr: String = "",
    val timeStr: String = "",
    val maxVolunteers: Int = 0,
    val registeredVolunteersCount: Int = 0,
    val registeredVolunteerIds: List<String> = emptyList(),
    val skillsNeeded: List<String> = emptyList(),
    val whatToBring: List<String> = emptyList(),
    val contactEmail: String = "",
    val status: String = "OPEN", // "OPEN", "FULL", "COMPLETED", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)
