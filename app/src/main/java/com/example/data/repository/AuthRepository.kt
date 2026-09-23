package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val userDao: UserDao) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun login(email: String, password: String):Result<UserEntity> {
        val user = userDao.getUserByEmail(email.trim())
            ?: return Result.failure(Exception("No account found with this email."))

        if (user.passwordHash != password.trim()) {
            return Result.failure(Exception("Invalid password. Please check your credentials."))
        }

        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String,
        org: String,
        phone: String
    ): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email.trim())
        if (existing != null) {
            return Result.failure(Exception("An account already exists with this email."))
        }

        val newUser = UserEntity(
            email = email.trim(),
            passwordHash = password.trim(),
            fullName = name.trim(),
            role = role,
            organization = org.trim(),
            phone = phone.trim(),
            volunteerHours = if (role == "VOLUNTEER") 4 else 25,
            badges = if (role == "VOLUNTEER") "New Contributor" else "Community Leader"
        )
        val id = userDao.insertUser(newUser)
        val saved = newUser.copy(id = id)
        _currentUser.value = saved
        return Result.success(saved)
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
    }

    suspend fun authenticateWithOtp(
        identifier: String,
        name: String,
        role: String,
        phone: String = ""
    ): Result<UserEntity> {
        val trimmed = identifier.trim()
        val isEmail = trimmed.contains("@")

        val existing = if (isEmail) {
            userDao.getUserByEmail(trimmed)
        } else {
            userDao.getUserByPhone(trimmed)
        }

        if (existing != null) {
            val updatedUser = if (name.isNotBlank() && existing.fullName != name.trim()) {
                val u = existing.copy(fullName = name.trim())
                userDao.updateUser(u)
                u
            } else {
                existing
            }
            _currentUser.value = updatedUser
            return Result.success(updatedUser)
        }

        val newUser = UserEntity(
            email = if (isEmail) trimmed else "${trimmed.filter { it.isDigit() }}@volunteer.socialconnect.org",
            passwordHash = "otp_verified",
            fullName = name.trim().ifBlank { "Diya Sarge" },
            role = role,
            organization = if (role == "NGO_LEADER") "Seva Foundation" else "Community Volunteer",
            phone = if (!isEmail) trimmed else phone.trim().ifBlank { "+91 98765 43210" },
            volunteerHours = if (role == "VOLUNTEER") 12 else 45,
            badges = if (role == "VOLUNTEER") "OTP Verified,Active Volunteer" else "Verified Leader"
        )
        val id = userDao.insertUser(newUser)
        val saved = newUser.copy(id = id)
        _currentUser.value = saved
        return Result.success(saved)
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun addVolunteerHours(hours: Int) {
        val current = _currentUser.value ?: return
        userDao.addVolunteerHours(current.id, hours)
        val updated = userDao.getUserByIdSync(current.id)
        if (updated != null) {
            _currentUser.value = updated
        }
    }
}
