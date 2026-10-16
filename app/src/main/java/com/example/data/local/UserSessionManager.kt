package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.entity.UserEntity

sealed class UserSessionResult {
    object NoSession : UserSessionResult()
    data class ActiveSession(
        val user: UserEntity,
        val loginCount: Int,
        val maxLogins: Int
    ) : UserSessionResult()
    data class SecurityAutoLogout(
        val expiredEmail: String,
        val message: String
    ) : UserSessionResult()
}

/**
 * Manages persistent user credentials, session state, and automated
 * security auto-logout after a configurable threshold of logins (default 5).
 */
class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "socialconnect_user_session"
        const val MAX_ALLOWED_LOGINS = 5

        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_PASSWORD_HASH = "user_password"
        private const val KEY_FULL_NAME = "user_full_name"
        private const val KEY_ROLE = "user_role"
        private const val KEY_ORGANIZATION = "user_org"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_VOLUNTEER_HOURS = "user_hours"
        private const val KEY_BADGES = "user_badges"
        private const val KEY_LOGIN_COUNT = "login_session_count"
        private const val KEY_SECURITY_LOGOUT_MESSAGE = "security_logout_message"
    }

    /**
     * Checks existing session upon app launch.
     * Enforces the security limit: if the login count has reached 5, auto-logout is triggered.
     */
    fun checkAndResumeSession(): UserSessionResult {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) {
            val pendingSecurityMessage = getSecurityLogoutMessage()
            val savedEmail = prefs.getString(KEY_EMAIL, "") ?: ""
            return if (!pendingSecurityMessage.isNullOrBlank()) {
                UserSessionResult.SecurityAutoLogout(savedEmail, pendingSecurityMessage)
            } else {
                UserSessionResult.NoSession
            }
        }

        val currentCount = prefs.getInt(KEY_LOGIN_COUNT, 1)

        // Security check: if login count reaches or exceeds threshold, trigger security auto-logout
        if (currentCount >= MAX_ALLOWED_LOGINS) {
            val expiredEmail = prefs.getString(KEY_EMAIL, "") ?: ""
            val securityNotice = "For your account security, your session has expired after $MAX_ALLOWED_LOGINS logins. Please enter your credentials to continue."
            
            // Invalidate session for security
            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putInt(KEY_LOGIN_COUNT, 0)
                .putString(KEY_SECURITY_LOGOUT_MESSAGE, securityNotice)
                .apply()

            return UserSessionResult.SecurityAutoLogout(expiredEmail, securityNotice)
        }

        // Increment session count for this entry
        val nextCount = currentCount + 1
        prefs.edit().putInt(KEY_LOGIN_COUNT, nextCount).apply()

        val user = getSavedUserEntity()
        return if (user != null) {
            UserSessionResult.ActiveSession(user, nextCount, MAX_ALLOWED_LOGINS)
        } else {
            UserSessionResult.NoSession
        }
    }

    /**
     * Persists user credentials and initializes a fresh session upon explicit login/signup.
     */
    fun saveLoginSession(user: UserEntity, password: String): Int {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PASSWORD_HASH, password)
            .putString(KEY_FULL_NAME, user.fullName)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_ORGANIZATION, user.organization)
            .putString(KEY_PHONE, user.phone)
            .putInt(KEY_VOLUNTEER_HOURS, user.volunteerHours)
            .putString(KEY_BADGES, user.badges)
            .putInt(KEY_LOGIN_COUNT, 1) // Fresh cycle starts at 1
            .remove(KEY_SECURITY_LOGOUT_MESSAGE)
            .apply()

        return 1
    }

    /**
     * Resets the active session login counter to 1 upon manual security renewal.
     */
    fun renewSession(): Int {
        prefs.edit().putInt(KEY_LOGIN_COUNT, 1).apply()
        return 1
    }

    /**
     * Returns the current login session count.
     */
    fun getLoginCount(): Int {
        return prefs.getInt(KEY_LOGIN_COUNT, 1)
    }

    /**
     * Clears all session credentials on user logout.
     */
    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putInt(KEY_LOGIN_COUNT, 0)
            .remove(KEY_SECURITY_LOGOUT_MESSAGE)
            .apply()
    }

    fun getSecurityLogoutMessage(): String? {
        return prefs.getString(KEY_SECURITY_LOGOUT_MESSAGE, null)
    }

    fun clearSecurityLogoutMessage() {
        prefs.edit().remove(KEY_SECURITY_LOGOUT_MESSAGE).apply()
    }

    fun getSavedUserEntity(): UserEntity? {
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val id = prefs.getLong(KEY_USER_ID, 1L)
        val password = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
        val name = prefs.getString(KEY_FULL_NAME, "Volunteer") ?: "Volunteer"
        val role = prefs.getString(KEY_ROLE, "VOLUNTEER") ?: "VOLUNTEER"
        val org = prefs.getString(KEY_ORGANIZATION, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val hours = prefs.getInt(KEY_VOLUNTEER_HOURS, 0)
        val badges = prefs.getString(KEY_BADGES, "Verified Volunteer") ?: "Verified Volunteer"

        return UserEntity(
            id = id,
            email = email,
            passwordHash = password,
            fullName = name,
            role = role,
            organization = org,
            phone = phone,
            volunteerHours = hours,
            badges = badges
        )
    }
}
