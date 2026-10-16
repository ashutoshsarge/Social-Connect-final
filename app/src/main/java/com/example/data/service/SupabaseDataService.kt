package com.example.data.service

import android.util.Log
import com.example.data.local.entity.UserEntity
import com.example.data.model.SocialEvent
import com.example.data.model.getCategoryDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Native Supabase PostgREST Client service for SocialConnect.
 * Directly integrates with Supabase REST endpoints for real-time live events,
 * user profile persistence, and event registrations.
 */
object SupabaseDataService {
    private const val TAG = "SupabaseDataService"
    private const val REST_URL = "${SupabaseAuthService.SUPABASE_URL}/rest/v1"
    private const val API_KEY = SupabaseAuthService.SUPABASE_PUBLIC_KEY

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Fetches all volunteer events from Supabase (/rest/v1/events)
     */
    suspend fun fetchEvents(): Result<List<SocialEvent>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$REST_URL/events?select=*&order=created_at.desc")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonArray = JSONArray(body)
                val events = mutableListOf<SocialEvent>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", "evt-$i")
                    val title = obj.optString("title", "Volunteer Drive")
                    val ngoId = obj.optString("ngo_id", "ngo-1")
                    val ngoName = obj.optString("ngo_name", "Community Partner")
                    val ngoLogo = obj.optString("ngo_logo", "")
                    val verified = obj.optBoolean("verified", true)
                    val category = obj.optString("category", "Environment")
                    val dateStr = obj.optString("date_str", "Upcoming")
                    val timeStr = obj.optString("time_str", "09:00 AM")
                    val location = obj.optString("location", "City Center")
                    val distanceKm = obj.optInt("distance_km", 3)
                    val joinedCount = obj.optInt("joined_count", 0)
                    val maxVolunteers = obj.optInt("max_volunteers", 50)
                    val coverImg = obj.optString("cover_img", "").takeIf { it.isNotBlank() }
                    val description = obj.optString("description", "")
                    val createdAt = obj.optLong("created_at", System.currentTimeMillis())

                    val whatYoullDo = parseStringList(obj.opt("what_youll_do"))
                    val skillsNeeded = parseStringList(obj.opt("skills_needed"))
                    val whatToBring = parseStringList(obj.opt("what_to_bring"))

                    events.add(
                        SocialEvent(
                            id = id,
                            title = title,
                            ngoId = ngoId,
                            ngoName = ngoName,
                            ngoLogo = ngoLogo.ifBlank { "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=120&q=80" },
                            verified = verified,
                            category = category,
                            dateStr = dateStr,
                            timeStr = timeStr,
                            location = location,
                            distanceKm = distanceKm,
                            joinedCount = joinedCount,
                            maxVolunteers = maxVolunteers,
                            coverImg = coverImg ?: "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&w=800&q=80",
                            description = description,
                            whatYoullDo = whatYoullDo.ifEmpty { listOf("Arrive on time", "Participate actively", "Support the community") },
                            skillsNeeded = skillsNeeded.ifEmpty { listOf("No prior experience required") },
                            whatToBring = whatToBring.ifEmpty { listOf("Water bottle", "Comfortable shoes") },
                            isJoined = false,
                            isBookmarked = false,
                            isWeekend = true,
                            localDrawableRes = getCategoryDrawable(category),
                            createdAt = createdAt
                        )
                    )
                }
                Result.success(events)
            } else {
                Log.w(TAG, "Failed fetching Supabase events (${response.code}): $body")
                Result.failure(Exception("Supabase fetch failed with code ${response.code}"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network exception fetching Supabase events: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Publishes a new event to Supabase (/rest/v1/events)
     */
    suspend fun createEvent(event: SocialEvent): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", event.id)
                put("title", event.title)
                put("ngo_id", event.ngoId)
                put("ngo_name", event.ngoName)
                put("ngo_logo", event.ngoLogo)
                put("verified", event.verified)
                put("category", event.category)
                put("date_str", event.dateStr)
                put("time_str", event.timeStr)
                put("location", event.location)
                put("distance_km", event.distanceKm)
                put("joined_count", event.joinedCount)
                put("max_volunteers", event.maxVolunteers)
                put("cover_img", event.coverImg)
                put("description", event.description)
                put("what_youll_do", JSONArray(event.whatYoullDo))
                put("skills_needed", JSONArray(event.skillsNeeded))
                put("what_to_bring", JSONArray(event.whatToBring))
                put("created_at", event.createdAt)
            }

            val request = Request.Builder()
                .url("$REST_URL/events")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Event ${event.id} persisted to Supabase successfully")
                Result.success(true)
            } else {
                val err = response.body?.string() ?: ""
                Log.w(TAG, "Failed to persist event to Supabase (${response.code}): $err")
                Result.failure(Exception("Failed creating event on Supabase (${response.code})"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception creating event on Supabase: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates an event's volunteer join count on Supabase
     */
    suspend fun updateEventJoinedCount(eventId: String, newCount: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("joined_count", newCount)
            }
            val request = Request.Builder()
                .url("$REST_URL/events?id=eq.$eventId")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Update failed (${response.code})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Synchronizes a user's profile to Supabase user_profiles table
     */
    suspend fun syncUserProfile(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", user.email.replace(".", "_"))
                put("email", user.email)
                put("full_name", user.fullName)
                put("role", user.role)
                put("organization", user.organization)
                put("phone", user.phone)
                put("volunteer_hours", user.volunteerHours)
                put("badges", user.badges)
            }
            val request = Request.Builder()
                .url("$REST_URL/user_profiles")
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Profile sync failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStringList(raw: Any?): List<String> {
        if (raw == null) return emptyList()
        return try {
            when (raw) {
                is JSONArray -> {
                    val list = mutableListOf<String>()
                    for (i in 0 until raw.length()) {
                        list.add(raw.getString(i))
                    }
                    list
                }
                is String -> {
                    if (raw.startsWith("[")) {
                        val arr = JSONArray(raw)
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getString(i))
                        }
                        list
                    } else {
                        listOf(raw)
                    }
                }
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
