package com.kova700.bookchat.core.fcm.forced_logout.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.core.fcm.forcedLogout.databinding.DialogForcedLogoutNoticeBinding
import com.kova700.bookchat.util.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForcedLogoutNoticeDialog(
	private val onClickOkBtn: () -> Unit,
) : DialogFragment() {
	private var _binding: DialogForcedLogoutNoticeBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogForcedLogoutNoticeBinding.inflate(inflater, container, false)
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
		binding.okBtn.setOnClickListener { onClickOkBtn() }
		dialogSizeManager.setDialogSize(binding.root)
	}

	private fun onClickOkBtn() {
		dismiss()
		onClickOkBtn.invoke()
	}
}