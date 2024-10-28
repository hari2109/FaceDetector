package com.billionhearts.facedetector.ui

import android.app.Application
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.billionhearts.facedetector.data.DetectorState
import com.billionhearts.facedetector.domain.FileDataHelper
import com.ibm.icu.util.Calendar
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class FaceDetectorViewModelTest {

    private lateinit var viewModel: FaceDetectorViewModel

    private lateinit var application: Application

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        viewModel = FaceDetectorViewModel(application)
    }

    @Test
    fun stateRequestPermission() {
        viewModel.refreshState(false)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val result = viewModel.faceDetectorState.getOrAwaitValue()
        Assert.assertTrue(result is DetectorState.RequestPermission)
    }

    @Test
    fun stateEmptyShown() {
        // Reading from gallery fails or gives empty list of images
        mockkObject(FileDataHelper)
        every {
            FileDataHelper.getNewImagesForDetector(
                application.contentResolver,
                Calendar.getInstance().timeInMillis
            )
        }.returns(emptyList())
        viewModel.refreshState(true)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val result = viewModel.faceDetectorState.getOrAwaitValue()
        Assert.assertTrue(result is DetectorState.Loading)

        val result1 = viewModel.faceDetectorState.getOrAwaitValue()
        Assert.assertTrue(result1 is DetectorState.Empty)
    }
}