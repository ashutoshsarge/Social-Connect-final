package com.example.data.repository

import com.example.data.model.VolunteerEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Boilerplate Repository class for managing volunteer event data in Firebase Firestore.
 *
 * Provides real-time synchronization via Kotlin Coroutines [Flow] and asynchronous
 * CRUD operations using coroutines.
 *
 * @param firestore The [FirebaseFirestore] instance. Defaults to [FirebaseFirestore.getInstance()].
 */
class FirestoreVolunteerEventRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    private val eventsCollection
        get() = firestore?.collection(COLLECTION_VOLUNTEER_EVENTS)

    /**
     * Real-time stream of all volunteer events, ordered by creation date descending.
     */
    fun getAllVolunteerEvents(): Flow<List<VolunteerEvent>> = callbackFlow {
        val collection = eventsCollection
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VolunteerEvent::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(events)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Real-time stream for a single volunteer event by ID.
     */
    fun getVolunteerEventById(eventId: String): Flow<VolunteerEvent?> = callbackFlow {
        val collection = eventsCollection
        if (collection == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = collection.document(eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val event = snapshot?.takeIf { it.exists() }?.let { doc ->
                    doc.toObject(VolunteerEvent::class.java)?.copy(id = doc.id)
                }

                trySend(event)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Real-time stream of volunteer events filtered by category.
     */
    fun getVolunteerEventsByCategory(category: String): Flow<List<VolunteerEvent>> = callbackFlow {
        val collection = eventsCollection
        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = collection
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VolunteerEvent::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(events)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Creates or updates a volunteer event in Firestore.
     * If the event ID is blank, a new unique document ID is generated.
     *
     * @param event The [VolunteerEvent] to save.
     * @return [Result] containing the document ID on success or an exception on failure.
     */
    suspend fun saveVolunteerEvent(event: VolunteerEvent): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val collection = eventsCollection
            if (collection == null) {
                continuation.resume(Result.success(event.id.ifBlank { "offline_event_${System.currentTimeMillis()}" }))
                return@suspendCancellableCoroutine
            }

            val docRef = if (event.id.isBlank()) {
                collection.document()
            } else {
                collection.document(event.id)
            }

            val eventToSave = event.copy(id = docRef.id)

            docRef.set(eventToSave)
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Result.success(docRef.id))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }

    /**
     * Updates specific fields of an existing volunteer event document.
     */
    suspend fun updateVolunteerEventFields(
        eventId: String,
        fields: Map<String, Any>
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val collection = eventsCollection
        if (collection == null) {
            continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }

        collection.document(eventId)
            .update(fields)
            .addOnSuccessListener {
                if (continuation.isActive) {
                    continuation.resume(Result.success(Unit))
                }
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resume(Result.failure(exception))
                }
            }
    }

    /**
     * Deletes a volunteer event document by its ID.
     */
    suspend fun deleteVolunteerEvent(eventId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val collection = eventsCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            collection.document(eventId)
                .delete()
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }

    /**
     * Registers a volunteer for an event. Adds their user ID to [registeredVolunteerIds]
     * and increments [registeredVolunteersCount] atomically.
     */
    suspend fun registerVolunteer(eventId: String, volunteerId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val collection = eventsCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            collection.document(eventId).update(
                FIELD_REGISTERED_VOLUNTEER_IDS, FieldValue.arrayUnion(volunteerId),
                FIELD_REGISTERED_COUNT, FieldValue.increment(1)
            ).addOnSuccessListener {
                if (continuation.isActive) {
                    continuation.resume(Result.success(Unit))
                }
            }.addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resume(Result.failure(exception))
                }
            }
        }

    /**
     * Unregisters a volunteer from an event. Removes their user ID and decrements the count atomically.
     */
    suspend fun unregisterVolunteer(eventId: String, volunteerId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val collection = eventsCollection
            if (collection == null) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            collection.document(eventId).update(
                FIELD_REGISTERED_VOLUNTEER_IDS, FieldValue.arrayRemove(volunteerId),
                FIELD_REGISTERED_COUNT, FieldValue.increment(-1)
            ).addOnSuccessListener {
                if (continuation.isActive) {
                    continuation.resume(Result.success(Unit))
                }
            }.addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resume(Result.failure(exception))
                }
            }
        }

    companion object {
        const val COLLECTION_VOLUNTEER_EVENTS = "volunteer_events"
        const val FIELD_REGISTERED_VOLUNTEER_IDS = "registeredVolunteerIds"
        const val FIELD_REGISTERED_COUNT = "registeredVolunteersCount"
    }
}
