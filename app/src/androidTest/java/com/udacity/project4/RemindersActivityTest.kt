package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.RecyclerViewItemCountAssertion
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun saveReminder_check() = runBlocking {
        val title = "Welcome test"
        val description = "XD"
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // save reminder
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText(title))
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText(description))
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map_select_location)).perform(ViewActions.longClick())
        pressBack()
        repository.saveReminder(
            ReminderDTO(
                title,
                description,
                "test location",
                0.0,
                0.0
            )
        )

        // check
        onView(ViewMatchers.withText(title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(description))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun addReminder_showSuccessToast() = runBlockingTest {
        val reminderDto = ReminderDTO(
            "title",
            "description",
            "location",
            0.0,
            0.0
        )

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssertion(0))

        // click on the add reminder fab
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText(reminderDto.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminderDto.description))

        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map_select_location)).perform(longClick())
        onView(withId(R.id.btn_select_location_save)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        // List items count must be 1
        onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssertion(1))

        // check success toast message
        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(not(`is`(getActivity(appContext)?.window?.decorView))))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun invalidTitle_showSnackBar() = runBlockingTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(
            allOf(
                withId(com.google.android.material.R.id.snackbar_text),
                withText(appContext.getString(R.string.err_enter_title))
            )
        ).check(matches(isDisplayed()))
    }

}
