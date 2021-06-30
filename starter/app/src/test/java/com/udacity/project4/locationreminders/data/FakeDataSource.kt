package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private val list = mutableListOf<ReminderDTO>()

    init {
        list.add(ReminderDTO("Test1","Description 1","Location 1",99.9,10.9,"1"))
        list.add(ReminderDTO("Test2","Description 2","Location 2",99.9,10.9,"2"))
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(list as List<ReminderDTO>)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        list.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val aux = list.find { reminder -> reminder.id == id } ?: return Result.Error("Not found")
        return Result.Success(aux)
    }

    override suspend fun deleteAllReminders() {
        list.clear()
    }
}