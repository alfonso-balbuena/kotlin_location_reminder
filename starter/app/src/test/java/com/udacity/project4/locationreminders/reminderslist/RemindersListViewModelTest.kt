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


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var reminderListViewModel : RemindersListViewModel



    @get:Rule
    var instantExecutorRul = InstantTaskExecutorRule()

    @Before
    fun init() {
        val dataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(Application(),dataSource)
    }

    @Test
    fun getReminderList_listTwoElements() {
        reminderListViewModel.loadReminders()
        val list = reminderListViewModel.remindersList.getOrAwaitValue()
        assertThat(list,(not(nullValue())))
        assertEquals(2,list.size)
    }
}