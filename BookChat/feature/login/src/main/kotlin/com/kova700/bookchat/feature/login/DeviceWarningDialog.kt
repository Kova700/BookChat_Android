package com.kova700.bookchat.feature.login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.kova700.bookchat.feature.login.databinding.DialogDeviceWarningBinding

class DeviceWarningDialog : DialogFragment() {

	private var _binding: DialogDeviceWarningBinding? = null
	private val binding get() = _binding!!
	private val loginViewModel: LoginViewModel by viewModels({ requireActivity() })

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogDeviceWarningBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun initViewState() {
		with(binding) {
			cancelBtn.setOnClickListener { dismiss() }
			okBtn.setOnClickListener { onClickOkBtn() }
		}
	}

	private fun onClickOkBtn() {
		loginViewModel.onClickDeviceWarningOk()
		dismiss()
	}
}