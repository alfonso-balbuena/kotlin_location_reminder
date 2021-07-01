package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var saveReminderViewModel : SaveReminderViewModel
    private lateinit var fakeDataSource : ReminderDataSource
    @get:Rule
    var instantExecutorRul = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initViewModel() {
        fakeDataSource = FakeDataSource(Dispatchers.Main)
        saveReminderViewModel = SaveReminderViewModel(MyApp(),fakeDataSource)

    }

    @After
    fun stop() {
        stopKoin()
    }

    private fun getReminderDataItem() : ReminderDataItem {
        return ReminderDataItem("Test3","Description 3","Location 3",99.9,10.9,"3")
    }

    @Test
    fun validateData_allData_true() {
        val reminderDataItem = getReminderDataItem()
        val dataValidated = saveReminderViewModel.validateEnteredData(reminderDataItem)
        Assert.assertEquals(true,dataValidated)
    }

    @Test
    fun validateData_noTitle_false() {
        val reminderDataItem = ReminderDataItem(null,"Description 3","Location 3",99.9,10.9,"3")
        val dataValidated = saveReminderViewModel.validateEnteredData(reminderDataItem)
        Assert.assertEquals(false,dataValidated)
    }

    @Test
    fun validateData_noLocation_false() {
        val reminderDataItem = ReminderDataItem("Test title 32","description",null,99.9,10.9,"3")
        val dataValidated = saveReminderViewModel.validateEnteredData(reminderDataItem)
        Assert.assertEquals(false,dataValidated)
    }
}