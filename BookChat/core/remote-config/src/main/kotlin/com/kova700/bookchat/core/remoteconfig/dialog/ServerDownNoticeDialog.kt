package com.kova700.bookchat.core.remoteconfig.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.core.remoteconfig.databinding.DialogServerDownNoticeBinding

class ServerDownNoticeDialog(
	private val onClickOkBtn: () -> Unit,
	private val noticeMessage: String,
) : DialogFragment() {
	private var _binding: DialogServerDownNoticeBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogServerDownNoticeBinding.inflate(inflater, container, false)
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
			noticeContentTv.text = noticeMessage
			okBtn.setOnClickListener { onClickOkBtn() }
		}
	}

	private fun onClickOkBtn() {
		onClickOkBtn.invoke()
		dismiss()
	}

	companion object {
		const val TAG = "DIALOG_TAG_SERVER_DOWN_NOTICE"
	}
}