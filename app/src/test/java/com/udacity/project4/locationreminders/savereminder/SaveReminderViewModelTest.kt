package com.udacity.project4.locationreminders.savereminder


import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun init() {
        stopKoin()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeDataSource()
        )
    }

    @Test
    fun check_loading_status() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        val reminderDataItem = ReminderDataItem(
            "test title",
            "test description",
            "test location",
            0.0,
            0.0
        )
        saveReminderViewModel.saveReminder(reminderDataItem)
        var loadingStatus = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingStatus, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        loadingStatus = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingStatus, `is`(false))
    }

    @Test
    fun validateEntryData_emptyTitle_setShowSnackBarIntAndReturnFalse() {
        val emptyTitleEntry = ReminderDataItem(
            "", "description", "location", 0.0, 0.0
        )

        val valid = saveReminderViewModel.validateEnteredData(emptyTitleEntry)
        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        assertThat(value, not(nullValue()))
        assert(!valid)
    }
}