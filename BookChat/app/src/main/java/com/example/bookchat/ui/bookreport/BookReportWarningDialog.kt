package com.example.bookchat.ui.bookreport

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.bookchat.databinding.DialogBookReportWarningBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookReportWarningDialog(
	private val warningTextStringId: Int,
	private val onOkClick: () -> Unit,
) : DialogFragment() {

	private var _binding: DialogBookReportWarningBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogBookReportWarningBinding.inflate(inflater, container, false)
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
			warningTextView.setText(warningTextStringId)
			cancelBtn.setOnClickListener { dismiss() }
			okBtn.setOnClickListener { onClickOkBtn() }
		}
	}

	fun onClickOkBtn() {
		onOkClick.invoke()
		dismiss()
	}
}