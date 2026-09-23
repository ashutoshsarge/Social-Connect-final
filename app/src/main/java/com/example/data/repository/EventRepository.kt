package com.example.data.repository

import com.example.data.local.dao.EventDao
import com.example.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun toggleJoinEvent(event: EventEntity) {
        val willJoin = !event.isJoined
        val newCount = if (willJoin) {
            event.registeredVolunteers + 1
        } else {
            maxOf(0, event.registeredVolunteers - 1)
        }
        eventDao.updateEventJoinedStatus(event.id, willJoin, newCount)
    }

    suspend fun createEvent(event: EventEntity): Long {
        return eventDao.insertEvent(event)
    }
}
