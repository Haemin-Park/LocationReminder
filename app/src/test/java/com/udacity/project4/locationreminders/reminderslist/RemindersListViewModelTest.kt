package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    private val testReminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
    private val testReminders: MutableList<ReminderDTO> =
        mutableListOf(testReminder)

    @Before
    fun setUp() {
        stopKoin()
        dataSource = FakeDataSource(testReminders)
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )
    }

    @Test
    fun check_loading_status() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        var loading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(loading, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        loading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(loading, `is`(false))
    }

    @Test
    fun unavailableReminders_showError() {
        dataSource.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("No reminders found"))
    }

}