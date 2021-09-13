package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    var reminders: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("No reminders found")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }

        val reminder = reminders.firstOrNull {
            it.id == id
        }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("No reminder found with given id: $id")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}