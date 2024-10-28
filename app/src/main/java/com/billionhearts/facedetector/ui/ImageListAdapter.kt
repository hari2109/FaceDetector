package com.billionhearts.facedetector.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.billionhearts.facedetector.data.DetectedImage

class ImageListAdapter(
    private val activity: AppCompatActivity,
    val results: List<DetectedImage>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return results.count()
    }

    override fun createFragment(position: Int): Fragment {
        return DetectedImageFragment.getNewInstance(position)
    }

    override fun getItemId(position: Int): Long {
        return results[position].fileName.hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return results.any { it.fileName.hashCode().toLong() == itemId }
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val tag = "f" + holder.itemId
            val fragment = activity.supportFragmentManager.findFragmentByTag(tag)
            // safe check, but fragment should not be null here
            if (fragment != null) {
                (fragment as DetectedImageFragment).setResult(results[position])
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}