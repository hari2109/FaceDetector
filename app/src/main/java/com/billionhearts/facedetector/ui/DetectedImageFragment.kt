package com.billionhearts.facedetector.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.billionhearts.facedetector.data.DetectedImage
import com.billionhearts.facedetector.data.DetectionItem
import com.billionhearts.facedetector.data.DetectorState
import com.billionhearts.facedetector.databinding.LayoutDetectedImageBinding

class DetectedImageFragment: Fragment() {

    private var imageIndex: Int = 0
    private lateinit var binding: LayoutDetectedImageBinding
    private lateinit var viewModel: FaceDetectorViewModel
    private var showNamePopupCallback: ShowNamePopUpListener? = null

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        imageIndex = args?.getInt(KEY_IMAGE_INDEX) ?: 0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        showNamePopupCallback = context as? ShowNamePopUpListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDetectedImageBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[FaceDetectorViewModel::class]
        initViews()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initViews() {
        with (binding) {
            val state = viewModel.faceDetectorState.value
            if (state !is DetectorState.Success) return
            val result = state.list[imageIndex]
            setResult(result)
        }
    }

    fun setResult(value: DetectedImage) {
        binding.setResult(value)
    }

    private fun LayoutDetectedImageBinding.setResult(result: DetectedImage) {
        ivResult.setImageBitmap(result.bitMap)
        tvFilename.text = result.fileName

        ivResult.post {
            overlay.setResults(
                detectionItems = result.detectionItems,
                imageHeight = result.imageHeight,
                imageWidth = result.imageWidth
            )
            overlay.setBoxClickListener(object : OverlayView.BoxClickedListener {
                override fun onBoxClicked(boxIndex: Int) {
                    val detectionItem = result.detectionItems.getOrNull(boxIndex) ?: return
                    showNamePopupCallback?.showNameBottomSheet(
                        detectionItem = detectionItem,
                        boxIndex = boxIndex,
                        imageIndex = imageIndex
                    )
                }
            })
        }
    }

    interface ShowNamePopUpListener {
        fun showNameBottomSheet(detectionItem: DetectionItem, boxIndex: Int, imageIndex: Int)
    }

    companion object {
        private const val KEY_IMAGE_INDEX = "detected_image"

        fun getNewInstance(index: Int): DetectedImageFragment {
            val fragment = DetectedImageFragment()
            val args = Bundle().apply {
                putInt(KEY_IMAGE_INDEX, index)
            }
            fragment.arguments = args
            return fragment
        }
    }
}