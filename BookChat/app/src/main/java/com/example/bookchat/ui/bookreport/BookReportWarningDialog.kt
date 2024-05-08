package com.example.bookchat.ui.bookreport

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogBookReportWarningBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookReportWarningDialog(
	private val warningTextStringId: Int,
	private val onOkClick: () -> Unit
) : DialogFragment() {

	private var _binding: DialogBookReportWarningBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_book_report_warning, container, false
		)
		binding.dialog = this
		binding.lifecycleOwner = this
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
		binding.warningTextView.setText(warningTextStringId)
	}

	fun onClickCancelBtn() {
		dismiss()
	}

	fun onClickOkBtn() {
		onOkClick.invoke()
		dismiss()
	}
}