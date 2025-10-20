package com.sap.codelab

import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.view.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTests {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    private val sampleMemos = listOf(
        Memo(1, "Title 1", "Desc 1", 0, 0.0, 0.0, false),
        Memo(2, "Title 2", "Desc 2", 0, 0.0, 0.0, true),
        Memo(3, "Title 3", "Desc 3", 0, 0.0, 0.0, false),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel()
        mockkObject(Repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onFirstLaunch sets isFirstLaunch to false`() {
        viewModel.onFirstLaunch()
        assertFalse(viewModel.isFirstLaunch)
    }

    @Test
    fun `updateMemo saves memo as done when isChecked is true`() = runTest {
        val memoToUpdate = sampleMemos[0].copy(isDone = false)
        coEvery { Repository.saveMemo(any()) } returns 100L

        viewModel.updateMemo(memoToUpdate, true)
        advanceUntilIdle()

        coVerify {
            Repository.saveMemo(memoToUpdate.copy(isDone = true))
        }
    }

    @Test
    fun `updateMemo does not save when isChecked is false`() = runTest {
        val memoToUpdate = sampleMemos[0].copy(isDone = false)

        viewModel.updateMemo(memoToUpdate, false)
        advanceUntilIdle()

        coVerify(exactly = 0) { Repository.saveMemo(any()) }
    }

    @Test
    fun `isFirstLaunch toggles after onFirstLaunch called`() {
        assertTrue(viewModel.isFirstLaunch)
        viewModel.onFirstLaunch()
        assertFalse(viewModel.isFirstLaunch)
    }
}

