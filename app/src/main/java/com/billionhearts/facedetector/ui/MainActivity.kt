package com.billionhearts.facedetector.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.billionhearts.facedetector.R
import com.billionhearts.facedetector.domain.GALLERY_PERMISSION
import com.billionhearts.facedetector.domain.galleryShowRequestPermissionRationale
import com.billionhearts.facedetector.domain.isMediaVisualUserSelected
import com.billionhearts.facedetector.domain.startInstalledAppDetailsActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = permissions.values.all { it }
            if (isGranted) {
                viewModel.onStoragePermissionGranted()
            } else if (galleryShowRequestPermissionRationale && !isMediaVisualUserSelected) {
                showPermissionContextualDialog()
            } else {
                showPermissionToSettingsDialog()
            }
        }

    private var permissionContextualDialog: Dialog? = null
    private var permissionSettingsDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initViews()
        initObservers()
    }

    private fun initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun initObservers() {
        TODO("Not yet implemented")
    }

    private fun showPermissionToSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.permission_contextual_dialog_title)
        builder.setMessage(R.string.storage_permission_contextual_dialog_settings)
        builder.setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int ->
            startInstalledAppDetailsActivity(this)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        permissionSettingsDialog = builder.create().apply {
            show()
        }
    }

    private fun showPermissionContextualDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.permission_contextual_dialog_title)
        builder.setMessage(R.string.storage_permission_contextual_dialog_settings)
        builder.setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int ->
            checkAndAskPermissionsForGallery()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        permissionContextualDialog = builder.create().apply {
            show()
        }
    }

    private fun checkAndAskPermissionsForGallery() {
        if (isMediaVisualUserSelected) {
            showPermissionToSettingsDialog()
        } else {
            storagePermissionLauncher.launch(arrayOf(GALLERY_PERMISSION))
        }
    }
}