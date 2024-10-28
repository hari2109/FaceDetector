package com.billionhearts.facedetector.data

import android.graphics.Bitmap
import com.google.mediapipe.tasks.components.containers.Detection

sealed interface DetectorState {
    data object RequestPermission : DetectorState
    data object Loading : DetectorState
    data object Empty : DetectorState
    data class Success(
        val list: List<DetectedImage>,
        val refreshIndex: Int) : DetectorState
    data class Error(val errorMessage: String? = null) : DetectorState
}

data class DetectedImage(
    val fileName: String,
    val bitMap: Bitmap,
    val detectionItems: MutableList<DetectionItem>,
    val imageHeight: Int,
    val imageWidth: Int
)

data class DetectionItem(
    val detector: Detection,
    var name: String? = null,
)