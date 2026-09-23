package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // VOLUNTEER or NGO_LEADER
    val organization: String = "",
    val volunteerHours: Int = 0,
    val badges: String = "Eco Hero,Rising Star",
    val phone: String = "+91 98765 43210"
)
