package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var reminderListViewModel : RemindersListViewModel
    private lateinit var dataSource: FakeDataSource


    @get:Rule
    var instantExecutorRul = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        dataSource = FakeDataSource(Dispatchers.Main)
        reminderListViewModel = RemindersListViewModel(Application(),dataSource)
    }

    @After
    fun stop() {
        stopKoin()
    }

    @Test
    fun getReminderList_listTwoElements() = mainCoroutineRule.runBlockingTest {
        reminderListViewModel.loadReminders()
        val list = reminderListViewModel.remindersList.getOrAwaitValue()
        assertThat(list,(not(nullValue())))
        assertEquals(2,list.size)
    }

    @Test
    fun loadReminders_Error() = mainCoroutineRule.runBlockingTest {
        dataSource.setReturnError(true)
        reminderListViewModel.loadReminders()
        val snackBarMessage = reminderListViewModel.showSnackBar.getOrAwaitValue()
        assertEquals("Test exception",snackBarMessage)
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        val isLoading = reminderListViewModel.showLoading.getOrAwaitValue()
        assertEquals(true, isLoading)
        mainCoroutineRule.resumeDispatcher()
    }
}