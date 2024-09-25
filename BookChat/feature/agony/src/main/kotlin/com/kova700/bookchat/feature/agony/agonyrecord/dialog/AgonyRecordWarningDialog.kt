package com.kova700.bookchat.feature.agony.agonyrecord.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.feature.agony.databinding.DialogAgonyRecordWarningBinding

class AgonyRecordWarningDialog(
	private val onClickOkBtn: () -> Unit,
) : DialogFragment() {

	private var _binding: DialogAgonyRecordWarningBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogAgonyRecordWarningBinding.inflate(inflater, container, false)
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
			cancelBtn.setOnClickListener { onClickCancelBtn() }
			okBtn.setOnClickListener { onClickOkBtn() }
		}
	}

	private fun onClickCancelBtn() {
		dismiss()
	}

	private fun onClickOkBtn() {
		onClickOkBtn.invoke()
		dismiss()
	}
}