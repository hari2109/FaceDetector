package com.billionhearts.facedetector.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DiffUtil
import com.billionhearts.facedetector.R
import com.billionhearts.facedetector.data.DetectionItem
import com.billionhearts.facedetector.data.DetectorState
import com.billionhearts.facedetector.databinding.ActivityMainBinding
import com.billionhearts.facedetector.domain.GALLERY_PERMISSION
import com.billionhearts.facedetector.domain.galleryShowRequestPermissionRationale
import com.billionhearts.facedetector.domain.isGalleryPermissionGranted
import com.billionhearts.facedetector.domain.startInstalledAppDetailsActivity
import com.billionhearts.facedetector.gone
import com.billionhearts.facedetector.visible

class MainActivity : AppCompatActivity(), DetectedImageFragment.ShowNamePopUpListener {

    private val viewModel: FaceDetectorViewModel by viewModels()
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = permissions.values.all { it }
            if (isGranted) {
                viewModel.onStoragePermissionGranted()
            } else if (galleryShowRequestPermissionRationale) {
                showPermissionContextualDialog()
            } else {
                showPermissionToSettingsDialog()
            }
        }

    private var permissionContextualDialog: Dialog? = null
    private var permissionSettingsDialog: Dialog? = null
    private var imagesListAdapter: ImageListAdapter? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshState(isGalleryPermissionGranted)
    }

    override fun onDestroy() {
        permissionContextualDialog?.dismiss()
        permissionSettingsDialog?.dismiss()
        super.onDestroy()
    }

    override fun showNameBottomSheet(detectionItem: DetectionItem, boxIndex: Int, imageIndex: Int) {
        val bottomSheetFragment = NameBottomSheetFragment.getNewInstance(detectionItem.name)
        bottomSheetFragment.show(supportFragmentManager, NameBottomSheetFragment::class.simpleName)
        bottomSheetFragment.setNameSaveListener(object : NameBottomSheetFragment.NameSaveListener {
            override fun onNameSaved(name: String) {
                viewModel.updateName(name, boxIndex, imageIndex)
                imagesListAdapter?.notifyItemChanged(imageIndex)
            }
        })
    }

    private fun initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!isBindingInitialized()) return
        binding.btPermission.setOnClickListener {
            requestGalleryPermissions()
            Toast.makeText(this, "Proceed clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isBindingInitialized(): Boolean {
        return ::binding.isInitialized
    }

    private fun initObservers() {
        viewModel.faceDetectorState.observe(this) { state ->
            when (state) {
                is DetectorState.Empty -> {
                    showErrorMessage()
                }
                is DetectorState.Error -> {
                    showErrorMessage()
                }
                is DetectorState.RequestPermission -> {
                    with (binding) {
                        clPermissionLayout.visible()
                        viewpager.gone()
                        tvErrorEmpty.gone()
                        llLoading.gone()
                    }
                }
                is DetectorState.Loading -> {
                    with (binding) {
                        clPermissionLayout.gone()
                        tvErrorEmpty.gone()
                        llLoading.visible()
                    }
                }
                is DetectorState.Success -> {
                    showSuccess(state)
                }
            }
        }
    }

    private fun showSuccess(state: DetectorState.Success) {
        with(binding) {
            clPermissionLayout.gone()
            llLoading.gone()
            tvErrorEmpty.gone()
            viewpager.visible()
            if (imagesListAdapter == null) {
                imagesListAdapter = ImageListAdapter(this@MainActivity, state.list)
                viewpager.adapter = imagesListAdapter
            } else {
                imagesListAdapter?.let { adapter ->
                    val diffResult = DiffUtil.calculateDiff(
                        PagerDiffUtil(
                            oldItems = adapter.results,
                            newItems = state.list,
                        ),
                    )
                    diffResult.dispatchUpdatesTo(adapter)
                    viewpager.post {
                        viewpager.setCurrentItem(state.refreshIndex, false)
                    }
                }
            }
        }
    }

    private fun showErrorMessage() {
        with(binding) {
            clPermissionLayout.gone()
            llLoading.gone()
            tvErrorEmpty.visible()
        }
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
            requestGalleryPermissions()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        permissionContextualDialog = builder.create().apply {
            show()
        }
    }

    private fun requestGalleryPermissions() {
        storagePermissionLauncher.launch(arrayOf(GALLERY_PERMISSION))
    }
}