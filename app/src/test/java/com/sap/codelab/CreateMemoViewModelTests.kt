package com.sap.codelab

import com.google.android.gms.maps.model.LatLng
import com.sap.codelab.view.create.CreateMemoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMemoViewModelTests {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CreateMemoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateMemoViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateMemo updates memo fields correctly`() {
        val latLng = LatLng(10.0, 20.0)
        viewModel.updateMemo("Title", "Description", latLng)

        assertTrue(viewModel.isMemoValid())
        assertFalse(viewModel.hasTextError())
        assertFalse(viewModel.hasTitleError())
    }

    @Test
    fun `isMemoValid returns false when title or description is blank`() {
        val latLng = LatLng(0.0, 0.0)
        viewModel.updateMemo("", "Desc", latLng)
        assertFalse(viewModel.isMemoValid())
        assertTrue(viewModel.hasTitleError())
        assertFalse(viewModel.hasTextError())

        viewModel.updateMemo("Title", "", latLng)
        assertFalse(viewModel.isMemoValid())
        assertFalse(viewModel.hasTitleError())
        assertTrue(viewModel.hasTextError())
    }
}