package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initialiseDataBase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDataBase() = database.close()

    @Test
    fun saveReminder_getReminderById_existInDB() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "My School",
            description = ":)",
            location = "Seoul, Republic of Korea",
            latitude = 1.1,
            longitude = 1.1
        )
        database.reminderDao().saveReminder(reminder)
        val dto = database.reminderDao().getReminderById(reminder.id)
        dto?.apply {
            assertThat(this, notNullValue())
            assertThat(id, `is`(reminder.id))
            assertThat(description, `is`(reminder.description))
            assertThat(location, `is`(reminder.location))
            assertThat(latitude, `is`(reminder.latitude))
            assertThat(longitude, `is`(reminder.longitude))
        }
    }
}