package com.example.bookchat.ui.channelList.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogChannelSettingBinding
import com.example.bookchat.ui.channelList.model.ChannelListItem
import com.example.bookchat.utils.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChannelSettingDialog(
	private val channel: ChannelListItem.ChannelItem,
	private val onClickMuteBtn: () -> Unit,
	private val onClickUnMuteBtn: () -> Unit,
	private val onClickTopPinBtn: () -> Unit,
	private val onClickUnPinBtn: () -> Unit,
	private val onClickOkExitBtn: () -> Unit,
) : DialogFragment() {
	private var _binding: DialogChannelSettingBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_channel_setting,
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

	fun onClickMuteBtn() {
		if (channel.notificationFlag) onClickMuteBtn.invoke()
		else onClickUnMuteBtn.invoke()
		dismiss()
	}

	fun onClickTopPinBtn() {
		if (channel.isTopPined) onClickUnPinBtn.invoke()
		else onClickTopPinBtn.invoke()
		dismiss()
	}

	fun onClickOkExitBtn() {
		onClickOkExitBtn.invoke()
		dismiss()
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		initDialogText()
	}

	private fun initDialogText() {

		binding.channelMuteTv.setText(
			if (channel.notificationFlag) R.string.channel_mute
			else R.string.channel_turn_on_notification
		)
		binding.channelTopPinTv.setText(
			if (channel.isTopPined) R.string.channel_un_pin_
			else R.string.channel_top_pin
		)
	}
}