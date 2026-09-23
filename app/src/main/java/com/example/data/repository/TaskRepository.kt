package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.SubtaskEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.model.DashboardStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allSubtasks: Flow<List<SubtaskEntity>> = taskDao.getAllSubtasks()

    fun getTasksForUser(userId: Long): Flow<List<TaskEntity>> = taskDao.getTasksForUser(userId)

    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>> =
        taskDao.getSubtasksForTask(taskId)

    val dashboardStats: Flow<DashboardStats> =
        combine(allTasks, allSubtasks) { tasks, _ ->
            val total = tasks.size
            val completed = tasks.count { it.status == "DONE" }
            val inProgress = tasks.count { it.status == "IN_PROGRESS" }
            val todo = tasks.count { it.status == "TODO" }
            val urgent = tasks.count { it.priority == "URGENT" }
            val rate = if (total > 0) (completed.toFloat() / total) * 100f else 0f
            val totalHours = tasks.sumOf { if (it.status == "DONE") it.estimatedHours else 0 }

            DashboardStats(
                totalTasks = total,
                completedTasks = completed,
                inProgressTasks = inProgress,
                todoTasks = todo,
                urgentTasks = urgent,
                completionRate = rate,
                totalVolunteerHours = totalHours
            )
        }

    suspend fun createTask(
        task: TaskEntity,
        subtasks: List<String>
    ): Long {
        val taskId = taskDao.insertTask(task)
        if (subtasks.isNotEmpty()) {
            val entities = subtasks.filter { it.isNotBlank() }.map { title ->
                SubtaskEntity(
                    taskId = taskId,
                    title = title.trim(),
                    isCompleted = false
                )
            }
            taskDao.insertSubtasks(entities)
        }
        return taskId
    }

    suspend fun updateTaskStatus(taskId: Long, newStatus: String) {
        val progress = when (newStatus) {
            "DONE" -> 100
            "IN_PROGRESS" -> 50
            "REVIEW" -> 85
            else -> 0
        }
        taskDao.updateTaskStatus(taskId, newStatus, progress)
    }

    suspend fun toggleSubtask(subtask: SubtaskEntity) {
        val updated = subtask.copy(isCompleted = !subtask.isCompleted)
        taskDao.updateSubtask(updated)

        // Recalculate task progress
        val allSubtasksForTask = taskDao.getSubtasksForTaskSync(subtask.taskId)
        if (allSubtasksForTask.isNotEmpty()) {
            val completedCount = allSubtasksForTask.count { it.isCompleted }
            val progress = (completedCount.toFloat() / allSubtasksForTask.size * 100).toInt()
            val newStatus = if (progress == 100) {
                "DONE"
            } else if (progress > 0) {
                "IN_PROGRESS"
            } else {
                "TODO"
            }
            taskDao.updateTaskStatus(subtask.taskId, newStatus, progress)
        }
    }

    suspend fun addSubtask(taskId: Long, title: String) {
        if (title.isNotBlank()) {
            taskDao.insertSubtask(
                SubtaskEntity(
                    taskId = taskId,
                    title = title.trim(),
                    isCompleted = false
                )
            )
            // Re-evaluate progress
            val subtasks = taskDao.getSubtasksForTaskSync(taskId)
            val completed = subtasks.count { it.isCompleted }
            val progress = (completed.toFloat() / subtasks.size * 100).toInt()
            taskDao.updateTaskStatus(taskId, if (progress == 100) "DONE" else "IN_PROGRESS", progress)
        }
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }
}
