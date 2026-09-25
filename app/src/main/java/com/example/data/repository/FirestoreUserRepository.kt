package com.example.data.repository

import android.util.Log
import com.example.data.model.FirestoreUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Repository for managing user data persistence in Firebase Firestore ("users" collection).
 * Enables real-time synchronization of user profiles, volunteer hours, badges, and drive registrations.
 */
class FirestoreUserRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }

    private val usersCollection
        get() = firestore?.collection(COLLECTION_USERS)

    /**
     * Real-time stream for observing a user profile document in Firestore.
     */
    fun getUserProfile(uid: String): Flow<FirestoreUser?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val collection = usersCollection
        if (collection == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = collection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to user profile: ${error.message}")
                    // Don't close flow on transient connection errors; return null gracefully
                    trySend(null)
                    return@addSnapshotListener
                }

                val user = snapshot?.takeIf { it.exists() }?.let { doc ->
                    doc.toObject(FirestoreUser::class.java)?.copy(uid = doc.id)
                }
                trySend(user)
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Saves or merges a user profile document in Firestore.
     */
    suspend fun saveUserProfile(user: FirestoreUser): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val collection = usersCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val docId = if (user.uid.isNotBlank()) user.uid else user.email.replace(".", "_")
            val userToSave = user.copy(
                uid = docId,
                lastLoginAt = System.currentTimeMillis()
            )

            collection.document(docId)
                .set(userToSave, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "User profile persisted to Firestore successfully: $docId")
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Failed to persist user profile to Firestore: ${exception.message}")
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }

    /**
     * Creates or synchronizes a user profile upon Google Sign-In or Firebase Auth verification.
     */
    suspend fun syncUserOnAuth(
        uid: String,
        email: String,
        fullName: String,
        role: String = "VOLUNTEER",
        organization: String = "",
        phone: String = ""
    ): Result<FirestoreUser> = suspendCancellableCoroutine { continuation ->
        val docId = if (uid.isNotBlank()) uid else email.replace(".", "_")
        val collection = usersCollection

        if (collection == null) {
            val localUser = FirestoreUser(
                uid = docId,
                email = email,
                fullName = fullName.ifBlank { "Verified Changemaker" },
                role = role,
                organization = organization.ifBlank { if (role == "NGO_LEADER") "Green Delhi Foundation" else "Delhi Youth Volunteers" },
                phone = phone.ifBlank { "+91 98765 43210" },
                volunteerHours = if (role == "VOLUNTEER") 42 else 340,
                badges = if (role == "VOLUNTEER") {
                    listOf("Eco Champion", "Weekend Hero", "7-Day Streak", "Verified Volunteer")
                } else {
                    listOf("Master Organizer", "Green Delhi Pioneer", "FCRA & 80G Certified", "Top Rated Partner")
                },
                lastLoginAt = System.currentTimeMillis()
            )
            continuation.resume(Result.success(localUser))
            return@suspendCancellableCoroutine
        }

        val docRef = collection.document(docId)

        docRef.get().addOnSuccessListener { snapshot ->
            val existing = snapshot.takeIf { it.exists() }?.toObject(FirestoreUser::class.java)
            val updatedUser = if (existing != null) {
                existing.copy(
                    uid = docId,
                    email = if (email.isNotBlank()) email else existing.email,
                    fullName = if (fullName.isNotBlank()) fullName else existing.fullName,
                    role = if (role.isNotBlank()) role else existing.role,
                    organization = if (organization.isNotBlank()) organization else existing.organization,
                    phone = if (phone.isNotBlank()) phone else existing.phone,
                    lastLoginAt = System.currentTimeMillis()
                )
            } else {
                FirestoreUser(
                    uid = docId,
                    email = email,
                    fullName = fullName.ifBlank { "Verified Changemaker" },
                    role = role,
                    organization = organization.ifBlank { if (role == "NGO_LEADER") "Green Delhi Foundation" else "Delhi Youth Volunteers" },
                    phone = phone.ifBlank { "+91 98765 43210" },
                    volunteerHours = if (role == "VOLUNTEER") 42 else 340,
                    badges = if (role == "VOLUNTEER") {
                        listOf("Eco Champion", "Weekend Hero", "7-Day Streak", "Verified Volunteer")
                    } else {
                        listOf("Master Organizer", "Green Delhi Pioneer", "FCRA & 80G Certified", "Top Rated Partner")
                    },
                    lastLoginAt = System.currentTimeMillis()
                )
            }

            docRef.set(updatedUser, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "User synced with Firestore on auth: ${updatedUser.uid}")
                    if (continuation.isActive) {
                        continuation.resume(Result.success(updatedUser))
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing synced user to Firestore: ${e.message}")
                    if (continuation.isActive) {
                        // Return the in-memory updated user even if remote sync failed (offline-first)
                        continuation.resume(Result.success(updatedUser))
                    }
                }
        }.addOnFailureListener { error ->
            Log.w(TAG, "Error reading user from Firestore on auth: ${error.message}")
            val fallbackUser = FirestoreUser(
                uid = docId,
                email = email,
                fullName = fullName,
                role = role,
                organization = organization,
                phone = phone
            )
            if (continuation.isActive) {
                continuation.resume(Result.success(fallbackUser))
            }
        }
    }

    /**
     * Atomically adds volunteer hours to the user's Firestore record.
     */
    suspend fun addVolunteerHours(uid: String, hours: Int): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            if (uid.isBlank()) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val collection = usersCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            collection.document(uid).update(
                FIELD_VOLUNTEER_HOURS, FieldValue.increment(hours.toLong())
            ).addOnSuccessListener {
                Log.d(TAG, "Logged $hours volunteer hours in Firestore for $uid")
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }.addOnFailureListener { e ->
                Log.w(TAG, "Failed to log volunteer hours in Firestore: ${e.message}")
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
        }

    /**
     * Updates joined event IDs in Firestore.
     */
    suspend fun toggleJoinedEvent(uid: String, eventId: String, isJoined: Boolean): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            if (uid.isBlank() || eventId.isBlank()) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val collection = usersCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val updateAction = if (isJoined) {
                FieldValue.arrayUnion(eventId)
            } else {
                FieldValue.arrayRemove(eventId)
            }

            collection.document(uid).update(FIELD_JOINED_EVENT_IDS, updateAction)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }

    /**
     * Updates bookmarked event IDs in Firestore.
     */
    suspend fun toggleBookmarkedEvent(uid: String, eventId: String, isBookmarked: Boolean): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            if (uid.isBlank() || eventId.isBlank()) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val collection = usersCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val updateAction = if (isBookmarked) {
                FieldValue.arrayUnion(eventId)
            } else {
                FieldValue.arrayRemove(eventId)
            }

            collection.document(uid).update(FIELD_BOOKMARKED_EVENT_IDS, updateAction)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }

    /**
     * Updates user causes and preferred location in Firestore.
     */
    suspend fun updatePreferences(uid: String, causes: List<String>, location: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            if (uid.isBlank()) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val collection = usersCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val updates = mapOf(
                FIELD_CAUSES to causes,
                FIELD_PREFERRED_LOCATION to location
            )

            collection.document(uid).update(updates)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }

    companion object {
        private const val TAG = "FirestoreUserRepository"
        const val COLLECTION_USERS = "users"
        const val FIELD_VOLUNTEER_HOURS = "volunteerHours"
        const val FIELD_JOINED_EVENT_IDS = "joinedEventIds"
        const val FIELD_BOOKMARKED_EVENT_IDS = "bookmarkedEventIds"
        const val FIELD_CAUSES = "causes"
        const val FIELD_PREFERRED_LOCATION = "preferredLocation"
    }
}
