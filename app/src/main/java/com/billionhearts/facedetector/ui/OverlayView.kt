/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.billionhearts.facedetector.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.billionhearts.facedetector.R
import com.billionhearts.facedetector.data.DetectionItem
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var detectionItems: MutableList<DetectionItem> = mutableListOf()
    private var rectangleList: MutableList<RectF> = mutableListOf()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f

    private var bounds = Rect()
    private var boxClickedListener: BoxClickedListener? = null

    init {
        initPaints()
    }

    fun clear() {
        detectionItems.clear()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    fun setBoxClickListener(boxClickedListener: BoxClickedListener) {
        this.boxClickedListener = boxClickedListener
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.white)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        detectionItems.takeIf { it.isNotEmpty() }.let {
            rectangleList.clear()
            for (detectionItem in detectionItems) {
                val detection = detectionItem.detector
                val boundingBox = detection.boundingBox()

                val top = boundingBox.top * scaleFactor
                val bottom = boundingBox.bottom * scaleFactor
                val left = boundingBox.left * scaleFactor
                val right = boundingBox.right * scaleFactor

                // Draw bounding box around detected faces
                val drawableRect = RectF(left, top, right, bottom)
                canvas.drawRect(drawableRect, boxPaint)
                rectangleList.add(drawableRect)

                // Create text to display alongside detected faces
                detection.categories()
                val name = detectionItem.name
                val drawableText = if (name.isNullOrBlank()) {
                  detection.categories()[0].categoryName() +
                                " " +
                                String.format(
                                    "%.2f",
                                    detection.categories()[0].score()
                                )
                } else {
                    name
                }

                // Draw rect behind display text
                textBackgroundPaint.getTextBounds(
                    drawableText,
                    0,
                    drawableText.length,
                    bounds
                )
                val textWidth = bounds.width()
                val textHeight = bounds.height()
                canvas.drawRect(
                    left,
                    bottom,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    bottom + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
                )

                // Draw text for detected face
                canvas.drawText(
                    drawableText,
                    left,
                    bottom + bounds.height(),
                    textPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(null)
        }
        val touchX = event.x;
        val touchY = event.y;

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rectangleList.forEachIndexed { index, rectF ->
                    if (rectF.contains(touchX, touchY)) {
                        Log.d(TAG, "Rectangle touched")
                        boxClickedListener?.onBoxClicked(index)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_MOVE -> {}
        }
        return true;
    }

    fun setResults(
        detectionItems: List<DetectionItem>,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        this.detectionItems.clear()
        this.detectionItems.addAll(detectionItems)

        // Images, videos and camera live streams are displayed in FIT_START mode. So we need to scale
        // up the bounding box to match with the size that the images/videos/live streams being
        // displayed.
        scaleFactor = min(width * 1f / imageWidth, height * 1f / imageHeight)

        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
        private const val TAG = "OverlayView"
    }

    interface BoxClickedListener {
        fun onBoxClicked(boxIndex: Int)
    }
}