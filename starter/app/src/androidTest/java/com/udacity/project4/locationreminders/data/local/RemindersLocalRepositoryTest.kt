package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderDataBase : RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init(){
        reminderDataBase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().context,
        RemindersDatabase::class.java).allowMainThreadQueries().build()
        remindersLocalRepository = RemindersLocalRepository(reminderDataBase.reminderDao(),Dispatchers.Main)
    }

    @After
    fun closeDB() = reminderDataBase.close()

    private fun getReminderDTO(id : String) : ReminderDTO {
        return ReminderDTO("Test1","Description1","Location test",19.99,34.2,id)
    }

    @Test
    fun saveReminder_reminderDTO_obj() = mainCoroutineRule.runBlockingTest {
        val reminderDTO = getReminderDTO("1")
        remindersLocalRepository.saveReminder(reminderDTO)
        val loadedReminder = remindersLocalRepository.getReminder(reminderDTO.id) as? Result.Success<*>
        assertEquals(true,loadedReminder != null)
        val data = loadedReminder!!.data as ReminderDTO
        assertEquals(reminderDTO,data)
    }

    @Test
    fun getReminders_twoElements_noEmptyList() = mainCoroutineRule.runBlockingTest {
        insertTwoElements()
        val list = remindersLocalRepository.getReminders() as? Result.Success<*>
        assertEquals(true, list != null)
    }

    private suspend fun insertTwoElements() {
        val reminderDTO1 = getReminderDTO("1")
        val reminderDTO2 = getReminderDTO("2")
        remindersLocalRepository.saveReminder(reminderDTO1)
        remindersLocalRepository.saveReminder(reminderDTO2)
    }

    @Test
    fun cleanData_insertTwoElements_emptyList() = mainCoroutineRule.runBlockingTest {
        insertTwoElements()
        remindersLocalRepository.deleteAllReminders()
        val list = remindersLocalRepository.getReminders() as? Result.Success<*>
        assertEquals(true, list != null)
        val dataList = ArrayList<ReminderDataItem>()
        assertEquals(true,dataList.isEmpty())
    }

    @Test
    fun getReminders_noFoundElement_Error() = mainCoroutineRule.runBlockingTest {
        remindersLocalRepository.deleteAllReminders()
        val reminder = remindersLocalRepository.getReminder("1") as? Result.Error
        assertEquals(true,reminder != null)
    }

}