package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogDeviceWarningBinding

class DeviceWarningDialog : DialogFragment() {

    private lateinit var okClickListener: OnOkClickListener
    private lateinit var binding: DialogDeviceWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_device_warning, container, false)
        with(binding) {
            dialog = this@DeviceWarningDialog
            lifecycleOwner = this@DeviceWarningDialog
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    fun clickCancelBtn() {
        this.dismiss()
    }

    fun clickOkBtn() {
        okClickListener.onOkClick()
        this.dismiss()
    }

    fun setOkClickListener(onOkClickListener: OnOkClickListener) {
        this.okClickListener = onOkClickListener
    }

    interface OnOkClickListener {
        fun onOkClick()
    }
}