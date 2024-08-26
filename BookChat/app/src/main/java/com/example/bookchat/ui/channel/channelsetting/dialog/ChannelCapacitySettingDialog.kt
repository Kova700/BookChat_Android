package com.example.bookchat.ui.channel.channelsetting.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.bookchat.databinding.DialogChannelCapacitySettingBinding
import com.example.bookchat.utils.DialogSizeManager
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
	)

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

	//TODO : 현재 방 최대 용량 말고 현재 인원 수 보다 적은 용량으로 선택한다면 "현재 인원보다 적은 인원으로는 설정할 수 없습니다" 알림
	//TODO : binding.channelCapacityNp.value = currentCapacity //기본값으로 0 넘어오는데 수정해야될듯
	private fun initNumberPickerState() {
		binding.channelCapacityNp.minValue = 0
		binding.channelCapacityNp.maxValue = selectableValues.size - 1
		binding.channelCapacityNp.displayedValues = selectableValues
		binding.channelCapacityNp.value =
			selectableValues.indexOfFirst { it.toInt() == currentCapacity }
	}
}