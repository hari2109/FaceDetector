package com.billionhearts.facedetector.domain

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.billionhearts.facedetector.isCursorUsable

const val TAG = "FaceDetector"

data class ImageFileData(
    val uri: Uri,
    val fileName: String,
)

internal suspend fun getLatestImages(
    contentResolver: ContentResolver,
    startTimeStampInSeconds: Long,
): List<ImageFileData> {
    return getNewImagesForDetector(
        contentResolver = contentResolver,
        queryStartTimeStampInSeconds = startTimeStampInSeconds,
    )
}

private fun getNewImagesForDetector(
    contentResolver: ContentResolver,
    queryStartTimeStampInSeconds: Long,
): List<ImageFileData> {

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    val selection =
        "${MediaStore.Images.Media.DATA} like ? and ${MediaStore.Images.Media.DATE_ADDED} > ?"
    val selectionArgs = arrayOf("%DCIM%", queryStartTimeStampInSeconds.toString())

    Log.d(TAG, "lastQueriedTimeStamp $queryStartTimeStampInSeconds")

    return contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder,
    )?.use { cursor ->
        if (cursor.isCursorUsable(cursor).not() || cursor.moveToFirst().not()) {
            return emptyList()
        }
        return buildList {
            cursor.moveToFirst()
            do {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)),
                )
                val fileName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))

                val data = ImageFileData(
                    uri = uri,
                    fileName = fileName,
                )
                add(data)
            } while (cursor.moveToNext())
        }
    } ?: emptyList()
}