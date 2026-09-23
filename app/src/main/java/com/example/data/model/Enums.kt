package com.example.data.model

enum class UserRole(val displayName: String) {
    VOLUNTEER("Volunteer"),
    NGO_LEADER("NGO Leader / Team Admin")
}

enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    REVIEW("In Review"),
    DONE("Completed")
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

enum class TaskCategory(val displayName: String, val iconName: String) {
    EDUCATION("Education", "School"),
    ENVIRONMENT("Environment", "Eco"),
    HEALTH("Health & Care", "Favorite"),
    COMMUNITY("Community Relief", "Groups"),
    DISASTER_RELIEF("Disaster Support", "Warning")
}

data class TaskWithSubtasks(
    val task: com.example.data.local.entity.TaskEntity,
    val subtasks: List<com.example.data.local.entity.SubtaskEntity> = emptyList()
)

data class DashboardStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val todoTasks: Int = 0,
    val urgentTasks: Int = 0,
    val completionRate: Float = 0f,
    val totalVolunteerHours: Int = 0,
    val activeEventsCount: Int = 0
)
