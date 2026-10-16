package com.example.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Result data class for Supabase Auth operations
 */
data class SupabaseAuthResult(
    val success: Boolean,
    val hasActiveSession: Boolean,
    val isEmailVerified: Boolean = true,
    val message: String,
    val error: String? = null
)

/**
 * Native Supabase Auth integration matching src/supabaseClient.js configuration.
 */
object SupabaseAuthService {
    const val SUPABASE_URL = "https://rrxsynkopobniyzqzveu.supabase.co"
    const val SUPABASE_PUBLIC_KEY = "sb_publishable_L0u894yZ3rzGzU3MAtlrqw_0NhGiUU_"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun signUp(email: String, pass: String, displayName: String? = null): Result<SupabaseAuthResult> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email.trim())
                put("password", pass)
                if (!displayName.isNullOrBlank()) {
                    put("data", JSONObject().apply {
                        put("display_name", displayName.trim())
                        put("full_name", displayName.trim())
                        put("name", displayName.trim())
                    })
                }
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/signup")
                .addHeader("apikey", SUPABASE_PUBLIC_KEY)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonObj = try { JSONObject(body) } catch (_: Exception) { null }
                val hasSession = jsonObj?.has("session") == true && !jsonObj.isNull("session")
                val message = if (hasSession) {
                    "Sign up successful"
                } else {
                    "Check your email and confirm your account before logging in."
                }
                Result.success(SupabaseAuthResult(success = true, hasActiveSession = hasSession, message = message))
            } else {
                val errorMsg = try {
                    val jsonObj = JSONObject(body)
                    when {
                        jsonObj.has("msg") -> jsonObj.getString("msg")
                        jsonObj.has("message") -> jsonObj.getString("message")
                        jsonObj.has("error_description") -> jsonObj.getString("error_description")
                        else -> "Sign up failed (${response.code})"
                    }
                } catch (e: Exception) {
                    "Sign up failed (${response.code})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithPassword(email: String, pass: String): Result<SupabaseAuthResult> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email.trim())
                put("password", pass)
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_PUBLIC_KEY)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonObj = try { JSONObject(body) } catch (_: Exception) { null }
                val userObj = jsonObj?.optJSONObject("user")
                val emailConfirmedAt = userObj?.optString("email_confirmed_at", "") ?: ""
                val confirmedAt = userObj?.optString("confirmed_at", "") ?: ""
                val isEmailVerified = (emailConfirmedAt.isNotBlank() && emailConfirmedAt != "null") ||
                        (confirmedAt.isNotBlank() && confirmedAt != "null")
                val hasSession = jsonObj?.has("access_token") == true || (jsonObj?.has("session") == true && !jsonObj.isNull("session"))
                Result.success(
                    SupabaseAuthResult(
                        success = true,
                        hasActiveSession = hasSession,
                        isEmailVerified = isEmailVerified,
                        message = "Sign in successful"
                    )
                )
            } else {
                val errorMsg = try {
                    val jsonObj = JSONObject(body)
                    when {
                        jsonObj.has("error_description") -> jsonObj.getString("error_description")
                        jsonObj.has("msg") -> jsonObj.getString("msg")
                        jsonObj.has("message") -> jsonObj.getString("message")
                        else -> "Sign in failed (${response.code})"
                    }
                } catch (e: Exception) {
                    "Sign in failed (${response.code})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends password recovery / reset email via Supabase Auth (/auth/v1/recover)
     */
    suspend fun resetPasswordForEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email.trim())
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/recover")
                .addHeader("apikey", SUPABASE_PUBLIC_KEY)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success("Password reset instructions dispatched! Check your inbox.")
            } else {
                val errorMsg = try {
                    val jsonObj = JSONObject(body)
                    when {
                        jsonObj.has("msg") -> jsonObj.getString("msg")
                        jsonObj.has("message") -> jsonObj.getString("message")
                        jsonObj.has("error_description") -> jsonObj.getString("error_description")
                        else -> "Unable to send recovery email (${response.code})"
                    }
                } catch (e: Exception) {
                    "Unable to send recovery email (${response.code})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resends email verification confirmation link via Supabase Auth (/auth/v1/resend)
     */
    suspend fun resendVerificationEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("type", "signup")
                put("email", email.trim())
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/resend")
                .addHeader("apikey", SUPABASE_PUBLIC_KEY)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success("Verification link resent! Please check your email inbox.")
            } else {
                val errorMsg = try {
                    val jsonObj = JSONObject(body)
                    when {
                        jsonObj.has("msg") -> jsonObj.getString("msg")
                        jsonObj.has("message") -> jsonObj.getString("message")
                        jsonObj.has("error_description") -> jsonObj.getString("error_description")
                        else -> "Unable to resend verification email (${response.code})"
                    }
                } catch (e: Exception) {
                    "Unable to resend verification email (${response.code})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
