package com.kova700.bookchat.feature.channel.userprofile.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.feature.channel.databinding.DialogUserBanWarningBinding
import com.kova700.bookchat.util.dialog.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserBanWarningDialog(
	private val onClickOkBtn: () -> Unit,
) : DialogFragment() {

	private var _binding: DialogUserBanWarningBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogUserBanWarningBinding.inflate(inflater, container, false)
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
		onClickOkBtn.invoke()
		dismiss()
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		with(binding) {
			cancelBtn.setOnClickListener { dismiss() }
			okBtn.setOnClickListener { onClickOkBtn() }
		}
	}
}