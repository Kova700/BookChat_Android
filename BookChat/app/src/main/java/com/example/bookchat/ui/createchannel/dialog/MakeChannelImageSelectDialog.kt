package com.example.bookchat.ui.createchannel.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.bookchat.databinding.DialogMakeChannelImageSelectBinding
import com.example.bookchat.utils.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MakeChannelImageSelectDialog(
	private val onSelectChangeDefaultImage: () -> Unit,
	private val onSelectGallery: () -> Unit,
) : DialogFragment() {
	private var _binding: DialogMakeChannelImageSelectBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogMakeChannelImageSelectBinding.inflate(inflater, container, false)
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
			changeDefaultImageBtn.setOnClickListener { onClickChangeDefaultImage() }
			selectPictureFromGalleryBtn.setOnClickListener { onClickGallery() }
		}
	}

	private fun onClickChangeDefaultImage() {
		onSelectChangeDefaultImage.invoke()
		dismiss()
	}

	private fun onClickGallery() {
		onSelectGallery.invoke()
		dismiss()
	}
}