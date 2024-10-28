package com.billionhearts.facedetector.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.billionhearts.facedetector.databinding.LayoutNameFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NameBottomSheetFragment: BottomSheetDialogFragment() {

    private lateinit var binding: LayoutNameFragmentBinding
    private var nameChangeListener: NameSaveListener? = null
    private var name: String? = null

    override fun setArguments(args: Bundle?) {
        name = args?.getString(ARG_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutNameFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Setting default state to fully expanded
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = (it as? BottomSheetDialog)
                ?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { bottomSheetView ->
                val behavior = BottomSheetBehavior.from(bottomSheetView)
                behavior.apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    isGestureInsetBottomIgnored = true
                    peekHeight = 0
                }
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(p0: View, p1: Float) {}
                    override fun onStateChanged(p0: View, state: Int) {
                        if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                            dialog.dismiss()
                        }
                    }
                })
            }
        }
        return dialog
    }
    private fun initViews() {
        with(binding) {
            if (!name.isNullOrBlank()) {
                etName.setText(name?: "")
            }
            btSaveName.setOnClickListener {
                val name = etName.text.toString()
                if (name.isNotBlank()) {
                    nameChangeListener?.onNameSaved(name)
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun setNameSaveListener(nameSaveListener: NameSaveListener) {
        this.nameChangeListener = nameSaveListener
    }

    interface NameSaveListener {
        fun onNameSaved(name: String)
    }

    companion object {
        const val ARG_NAME = "name"

        fun getNewInstance(name: String?): NameBottomSheetFragment {
            val fragment = NameBottomSheetFragment()
            val args = Bundle()
            args.putSerializable(ARG_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }
}