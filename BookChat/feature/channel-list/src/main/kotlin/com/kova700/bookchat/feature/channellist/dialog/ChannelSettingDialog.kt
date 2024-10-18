package com.kova700.bookchat.feature.channellist.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.channel.databinding.DialogChannelSettingBinding
import com.kova700.bookchat.feature.channellist.model.ChannelListItem
import com.kova700.bookchat.util.dialog.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChannelSettingDialog(
	private val channel: ChannelListItem.ChannelItem,
	private val onClickOkExitBtn: () -> Unit,
	private val onClickMuteRelatedBtn: () -> Unit,
	private val onClickTopPinRelatedBtn: () -> Unit,
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
		_binding = DialogChannelSettingBinding.inflate(inflater, container, false)
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

	private fun onClickMuteBtn() {
		onClickMuteRelatedBtn.invoke()
		dismiss()
	}

	private fun onClickTopPinBtn() {
		onClickTopPinRelatedBtn.invoke()
		dismiss()
	}

	private fun onClickOkExitBtn() {
		onClickOkExitBtn.invoke()
		dismiss()
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		initDialogText()
		with(binding) {
			channelMuteTv.setOnClickListener { onClickMuteBtn() }
			channelTopPinTv.setOnClickListener { onClickTopPinBtn() }
			channelExitTv.setOnClickListener { onClickOkExitBtn() }
		}
	}

	private fun initDialogText() {
		binding.channelMuteTv.setText(
			if (channel.isNotificationOn) R.string.channel_mute
			else R.string.channel_turn_on_notification
		)
		binding.channelTopPinTv.setText(
			if (channel.isTopPined) R.string.channel_un_pin_
			else R.string.channel_top_pin
		)
	}
	companion object {
		const val TAG = "DIALOG_TAG_CHANNEL_SETTING"
	}
}