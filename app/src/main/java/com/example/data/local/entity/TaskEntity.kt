package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val status: String, // TODO, IN_PROGRESS, REVIEW, DONE
    val priority: String, // LOW, MEDIUM, HIGH, URGENT
    val category: String, // EDUCATION, ENVIRONMENT, HEALTH, COMMUNITY, DISASTER_RELIEF
    val assignedToId: Long? = null,
    val assignedToName: String = "Unassigned",
    val createdById: Long = 1,
    val createdByName: String = "NGO Coordinator",
    val dueDate: String = "Today, 6:00 PM",
    val progressPercent: Int = 0,
    val estimatedHours: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)
