package com.example.bookchat.ui.agony.agonyrecord.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogAgonyRecordWarningBinding
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordViewModel

class AgonyRecordWarningDialog : DialogFragment() {

	private var _binding: DialogAgonyRecordWarningBinding? = null
	private val binding get() = _binding!!

	private val agonyRecordViewModel: AgonyRecordViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_agony_record_warning, container, false)
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
		this.dismiss()
	}

	fun onClickOkBtn() {
		agonyRecordViewModel.clearEditingState()
		this.dismiss()
	}
}