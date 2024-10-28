package com.billionhearts.facedetector.ui

import androidx.recyclerview.widget.DiffUtil
import com.billionhearts.facedetector.data.DetectedImage

class PagerDiffUtil(val oldItems: List<DetectedImage>,
    val newItems: List<DetectedImage>) : DiffUtil.Callback() {

    enum class PayloadKey {
        VALUE
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return oldItem.fileName != newItem.fileName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]

        if (oldItem.fileName != newItem.fileName) return false
        val oldDetectorBoxes = oldItem.detectionItems
        val newDetectorBoxes = newItem.detectionItems
        if (oldDetectorBoxes.size != newDetectorBoxes.size) return false
        val oldNames = oldDetectorBoxes.map { it.name }
        val newNames = newDetectorBoxes.map { it.name }
        return oldNames.containsAll(newNames)
    }

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return PayloadKey.VALUE
    }
}