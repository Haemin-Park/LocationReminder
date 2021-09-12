package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository


    @Before
    fun initialSetup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO,
                Dispatchers.Main
            )
    }

    @After
    fun closeDataBase() = database.close()

    @Test
    fun saveReminder_getReminderById_existInDB() = mainCoroutineRule.runBlockingTest {
        // WHEN: saving data in database
        val reminder = ReminderDTO(
            title = "My School",
            description = ":)",
            location = "Seoul, Republic of Korea",
            latitude = 1.1,
            longitude = 1.1
        )
        repository.saveReminder(reminder)
        // THEN: get saved data
        val reminderLoaded = repository.getReminder(reminder.id) as Result.Success<ReminderDTO>
        val reminderDTO = reminderLoaded.data
        // RESULT: Successfully saved and loaded
        reminderDTO.apply {
            assertThat(this, Matchers.notNullValue())
            assertThat(id, `is`(reminder.id))
            assertThat(description, `is`(reminder.description))
            assertThat(location, `is`(reminder.location))
            assertThat(latitude, `is`(reminder.latitude))
            assertThat(longitude, `is`(reminder.longitude))
        }
    }
}