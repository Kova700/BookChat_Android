package com.example.bookchat.ui.mypage.useredit.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogProfileEditBinding
import com.example.bookchat.utils.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileEditDialog(
	private val onSelectDefaultImage: () -> Unit,
	private val onSelectGallery: () -> Unit,
) : DialogFragment() {

	private var _binding: DialogProfileEditBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_profile_edit,
			container, false
		)
		binding.dialog = this
		binding.lifecycleOwner = viewLifecycleOwner
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
		dialogSizeManager.setDialogSize(binding.root)
		with(binding) {
			selectPictureDefaultImageBtn.setOnClickListener { onClickDefaultImage() }
			selectPictureFromGalleryBtn.setOnClickListener { onClickGallery() }
		}
	}

	private fun onClickDefaultImage() {
		onSelectDefaultImage.invoke()
		dismiss()
	}

	private fun onClickGallery() {
		onSelectGallery.invoke()
		dismiss()
	}

}