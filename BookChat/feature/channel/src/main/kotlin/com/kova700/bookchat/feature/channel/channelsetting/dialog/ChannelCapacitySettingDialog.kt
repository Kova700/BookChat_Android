package com.kova700.bookchat.feature.channel.channelsetting.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.feature.channel.databinding.DialogChannelCapacitySettingBinding
import com.kova700.bookchat.util.dialog.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChannelCapacitySettingDialog(
	private val currentCapacity: Int,
	private val onClickOkBtn: (Int) -> Unit,
) : DialogFragment() {
	private var _binding: DialogChannelCapacitySettingBinding? = null
	private val binding get() = _binding!!

	private val selectableValues = arrayOf(
		"10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "200", "300"
	).filter { it.toInt() >= currentCapacity }

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogChannelCapacitySettingBinding.inflate(inflater, container, false)
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

	fun onClickOkBtn() {
		onClickOkBtn.invoke(selectableValues[binding.channelCapacityNp.value].toInt())
		dismiss()
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		initNumberPickerState()
		with(binding) {
			cancelBtn.setOnClickListener { dismiss() }
			okBtn.setOnClickListener { onClickOkBtn() }
		}
	}

	private fun initNumberPickerState() {
		with(binding) {
			channelCapacityNp.minValue = 0
			channelCapacityNp.maxValue = selectableValues.lastIndex
			channelCapacityNp.displayedValues = selectableValues.toTypedArray()
			channelCapacityNp.value = selectableValues.indexOfFirst { it.toInt() == currentCapacity }
		}
	}
	companion object{
		const val TAG = "DIALOG_TAG_CHANNEL_CAPACITY_DIALOG"
	}
}