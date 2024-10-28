package com.billionhearts.facedetector.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.billionhearts.facedetector.data.DetectedImage

class ImageListAdapter(
    activity: AppCompatActivity,
    private val results: List<DetectedImage>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return results.count()
    }

    override fun createFragment(position: Int): Fragment {
        return DetectedImageFragment.getNewInstance(position)
    }
}