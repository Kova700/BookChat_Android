package com.example.bookchat.ui.channel.channelsetting.authoritymanage.host.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.bookchat.databinding.DialogHostChangeSuccessBinding
import com.example.bookchat.utils.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HostChangeSuccessDialog(
	private val onClickOk: () -> Unit,
) : DialogFragment() {

	private var _binding: DialogHostChangeSuccessBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogHostChangeSuccessBinding.inflate(inflater, container, false)
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

	private fun onClickOkBtn() {
		dismiss()
		onClickOk.invoke()
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		binding.okBtn.setOnClickListener { onClickOkBtn() }
	}
}