package com.example.bookchat.ui.createchannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogMakeChannelSelectBookBinding
import com.example.bookchat.domain.model.Book
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DialogSizeManager
import javax.inject.Inject

class MakeChannelBookSelectDialog(
	private val onClickMakeChannel: () -> Unit,
	val selectedBook: Book,
) : DialogFragment() {

	private var _binding: DialogMakeChannelSelectBookBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.dialog_make_channel_select_book, container, false
		)
		binding.lifecycleOwner = this.viewLifecycleOwner
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

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		dialogSizeManager.setDialogSize(binding.makeChannelSelectBookDialogLayout)
	}

	fun onClickMakeChannel() {
		dismiss()
		onClickMakeChannel.invoke()
	}
}