package com.example.bookchat.ui.channel.userprofile.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogUserBanWarningBinding
import com.example.bookchat.utils.DialogSizeManager
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
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_user_ban_warning,
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
	}
}