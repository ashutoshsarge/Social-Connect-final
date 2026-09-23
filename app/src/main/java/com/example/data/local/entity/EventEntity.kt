package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val ngoName: String,
    val category: String,
    val date: String,
    val location: String,
    val requiredVolunteers: Int,
    val registeredVolunteers: Int,
    val description: String,
    val isJoined: Boolean = false
)
