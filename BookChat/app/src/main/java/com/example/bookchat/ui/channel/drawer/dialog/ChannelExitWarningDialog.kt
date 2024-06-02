package com.example.bookchat.ui.channel.drawer.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogChannelExitWarningBinding
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.utils.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChannelExitWarningDialog(
	private val clientAuthority: ChannelMemberAuthority,
	private val onClickOkBtn: () -> Unit,
) : DialogFragment() {

	private var _binding: DialogChannelExitWarningBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_channel_exit_warning,
			container, false
		)
		binding.lifecycleOwner = viewLifecycleOwner
		binding.dialog = this
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

	fun onClickCancelBtn() {
		dismiss()
	}

	fun onClickOkBtn() {
		onClickOkBtn.invoke()
		dismiss()
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		binding.hostWarningContentTv.visibility =
			if (clientAuthority == ChannelMemberAuthority.HOST) View.VISIBLE else View.GONE

	}
}