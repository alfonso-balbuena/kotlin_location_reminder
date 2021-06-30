package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database : RemindersDatabase

    @Before
    fun initDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context,RemindersDatabase::class.java).build()
    }

    @After
    fun closeDB() {
        database.close()
    }

    private fun getReminderDTO() : ReminderDTO {
        return ReminderDTO("Test1","Description1","Location test",19.99,34.2,"1")
    }

    @Test
    fun saveReminder_reminderDTO_success() = runBlockingTest {
        val reminderItem = getReminderDTO()
        database.reminderDao().saveReminder(reminderItem)
        val loaded = database.reminderDao().getReminderById("1")
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`(reminderItem.title) )
        assertThat(loaded.description, `is`(reminderItem.description) )
        assertThat(loaded.location, `is`(reminderItem.location) )
        assertThat(loaded.latitude, `is`(reminderItem.latitude) )
        assertThat(loaded.longitude, `is`(reminderItem.longitude) )
    }

    @Test
    fun cleanDataBase_NoRecords() = runBlockingTest {
        val reminderDTO = getReminderDTO()
        database.reminderDao().saveReminder(reminderDTO)
        val listReminders = database.reminderDao().getReminders()
        database.reminderDao().deleteAllReminders()
        val listAfterDeleteAll = database.reminderDao().getReminders()
        assertThat(listReminders.isEmpty(),`is`(false))
        assertThat(listAfterDeleteAll.isEmpty(),`is`(true))
    }

    @Test
    fun saveReminder_twoRemindersDTO_twoElements() = runBlockingTest {
        val reminderDTO1 = getReminderDTO()
        val reminderDTO2 = getReminderDTO()
        reminderDTO2.id = "2"
        database.reminderDao().saveReminder(reminderDTO2)
        database.reminderDao().saveReminder(reminderDTO1)
        val list = database.reminderDao().getReminders()
        assertThat(list.size,`is`(2))
    }
}