package com.example.bookchat.ui.login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogDeviceWarningBinding

class DeviceWarningDialog : DialogFragment() {

	private var _binding: DialogDeviceWarningBinding? = null
	private val binding get() = _binding!!
	private val loginViewModel: LoginViewModel by viewModels({ requireActivity() })

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_device_warning, container, false
		)
		binding.lifecycleOwner = this
		binding.dialog = this
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun onClickCancelBtn() {
		dismiss()
	}

	fun onClickOkBtn() {
		loginViewModel.onClickDeviceWarningOk()
		dismiss()
	}
}