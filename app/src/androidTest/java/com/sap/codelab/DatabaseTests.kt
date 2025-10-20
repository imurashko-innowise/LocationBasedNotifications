package com.sap.codelab

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Database
import com.sap.codelab.repository.MemoDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTests {

    private lateinit var database: Database
    private lateinit var memoDao: MemoDao

    @Before
    fun setup() {
        // Create an in-memory database so it doesn't persist between tests
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            Database::class.java
        ).build()
        memoDao = database.getMemoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetMemoById() = runBlocking {
        val memo = Memo(
            id = 0L,
            title = "Test Memo",
            description = "Description",
            reminderDate = System.currentTimeMillis(),
            reminderLatitude = 12.34,
            reminderLongitude = 56.78,
            isDone = false
        )

        val newId = memoDao.insert(memo)
        assertTrue(newId > 0)

        val fetchedMemo = memoDao.getMemoById(newId)
        assertEquals("Test Memo", fetchedMemo.title)
        assertEquals("Description", fetchedMemo.description)
        assertEquals(false, fetchedMemo.isDone)
    }

    @Test
    fun getAll_returnsAllMemos() = runBlocking {
        val memo1 = Memo(
            id = 0L,
            title = "Memo 1",
            description = "Desc 1",
            reminderDate = 0L,
            reminderLatitude = 0.0,
            reminderLongitude = 0.0,
            isDone = false
        )
        val memo2 = Memo(
            id = 0L,
            title = "Memo 2",
            description = "Desc 2",
            reminderDate = 0L,
            reminderLatitude = 0.0,
            reminderLongitude = 0.0,
            isDone = true
        )

        memoDao.insert(memo1)
        memoDao.insert(memo2)

        val allMemos = memoDao.getAll()
        assertEquals(2, allMemos.size)
    }

    @Test
    fun getOpen_returnsOnlyOpenMemos() = runBlocking {
        val openMemo = Memo(
            id = 0L,
            title = "Open Memo",
            description = "Open Desc",
            reminderDate = 0L,
            reminderLatitude = 0.0,
            reminderLongitude = 0.0,
            isDone = false
        )
        val doneMemo = Memo(
            id = 0L,
            title = "Done Memo",
            description = "Done Desc",
            reminderDate = 0L,
            reminderLatitude = 0.0,
            reminderLongitude = 0.0,
            isDone = true
        )

        memoDao.insert(openMemo)
        memoDao.insert(doneMemo)

        val openMemos = memoDao.getOpen()
        assertEquals(1, openMemos.size)
        assertEquals("Open Memo", openMemos[0].title)
    }
}