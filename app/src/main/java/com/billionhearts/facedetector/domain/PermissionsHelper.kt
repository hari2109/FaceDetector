package com.billionhearts.facedetector.domain

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

val GALLERY_PERMISSION =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_EXTERNAL_STORAGE
    } else Manifest.permission.READ_MEDIA_IMAGES

val Context.isGalleryPermissionGranted: Boolean
    get() =
        ContextCompat.checkSelfPermission(
            this,
            GALLERY_PERMISSION,
        ) == PackageManager.PERMISSION_GRANTED


val Activity.galleryShowRequestPermissionRationale: Boolean
    get() = ActivityCompat.shouldShowRequestPermissionRationale(this, GALLERY_PERMISSION)

fun startInstalledAppDetailsActivity(context: Context?) {
    if (context == null) {
        return
    }
    val i = Intent()
    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    i.addCategory(Intent.CATEGORY_DEFAULT)
    i.setData(Uri.parse("package:" + context.packageName))
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    context.startActivity(i)
}