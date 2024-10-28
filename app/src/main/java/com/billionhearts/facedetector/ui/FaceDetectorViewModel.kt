package com.billionhearts.facedetector.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.billionhearts.facedetector.data.DetectedImage
import com.billionhearts.facedetector.data.DetectionItem
import com.billionhearts.facedetector.data.DetectorState
import com.billionhearts.facedetector.domain.FaceDetectorHelper
import com.billionhearts.facedetector.domain.ImageFileData
import com.billionhearts.facedetector.domain.TAG
import com.billionhearts.facedetector.domain.getLatestImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class FaceDetectorViewModel(application: Application) : AndroidViewModel(application) {

    private val mutableStateFlow = MutableStateFlow<DetectorState>(DetectorState.Empty)

    private fun getContext(): Application {
        return getApplication() as Application
    }

    private lateinit var faceDetectorHelper: FaceDetectorHelper

    val faceDetectorState: LiveData<DetectorState>
        get() = mutableStateFlow.asLiveData()

    fun onCreate() {
        faceDetectorHelper = FaceDetectorHelper(context = getContext())
    }

    fun refreshState(hasStoragePermission: Boolean) {
        if (!hasStoragePermission) {
            mutableStateFlow.value = DetectorState.RequestPermission
            return
        }
        mutableStateFlow.value = DetectorState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val imagesList = getLatestImages(
                contentResolver = getContext().contentResolver,
                startTimeStampInSeconds = getTimeStampStartInSeconds(),
            )
            if (imagesList.isEmpty()) {
                mutableStateFlow.value = DetectorState.Empty
                return@launch
            }
            try {
                processImages(imagesList)
            } catch (e: Exception) {
                setError(e.message)
            }
        }
    }

    private fun processImages(imagesList: List<ImageFileData>) {
        val detectedImages = mutableListOf<DetectedImage>()
        imagesList.forEach { imageItem ->
            getSourceFromImage(imageItem)
                ?.copy(Bitmap.Config.ARGB_8888, true)
                ?.let { bitmap ->
                    faceDetectorHelper.detectImage(bitmap)?.let { result ->
                        val detectorResult = result.results[0]
                        val detectionItems = detectorResult.detections().map {
                            DetectionItem(detector = it, name = "")
                        }.toMutableList()

                        if (detectionItems.isNotEmpty()) {
                            val image = DetectedImage(
                                fileName = imageItem.fileName,
                                bitMap = bitmap,
                                detectionItems = detectionItems,
                                imageHeight = bitmap.height,
                                imageWidth = bitmap.width
                            )
                            detectedImages.add(image)
                        }
                    } ?: run {
                        Log.d(TAG, "Face detection failed for image: ${imageItem.uri}")
                    }
                } ?: run {
                Log.d(TAG, "Bitmap creation failed for image: ${imageItem.uri}")
            }
        }
        if (imagesList.isEmpty()) {
            setError("Bitmap detection failed")
            return
        }
        mutableStateFlow.value = DetectorState.Success(detectedImages)
    }

    private fun getSourceFromImage(sample: ImageFileData): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                getContext().contentResolver,
                sample.uri
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                getContext().contentResolver,
                sample.uri
            )
        }

    fun onStoragePermissionGranted() {
        refreshState(true)
    }

    fun updateName(name: String, boxIndex: Int, imageIndex: Int) {
        val currentValue = mutableStateFlow.value
        if (currentValue !is DetectorState.Success) return
        val state = currentValue.list.toMutableList()
        val result = state.getOrNull(imageIndex) ?: return
        val newDetectionItems = mutableListOf<DetectionItem>()
        result.detectionItems.forEachIndexed { index, item ->
            if (index == boxIndex) {
                newDetectionItems.add(DetectionItem(item.detector, name))
            } else {
                newDetectionItems.add(item.copy())
            }
        }
        val detectedImageCopy = result.copy(detectionItems = newDetectionItems)
        state[imageIndex] = detectedImageCopy
        mutableStateFlow.value = DetectorState.Success(state)
    }

    private fun setError(errorMessage: String? = null) {
        Log.d(TAG, "Error: $errorMessage")
        mutableStateFlow.value = DetectorState.Error(errorMessage)
    }

    private fun getTimeStampStartInSeconds(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_WEEK, -7)
        return calendar.timeInMillis / 1000
    }
}