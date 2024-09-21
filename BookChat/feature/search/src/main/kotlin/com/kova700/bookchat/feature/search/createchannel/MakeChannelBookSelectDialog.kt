package com.kova700.bookchat.feature.search.createchannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.feature.search.databinding.DialogMakeChannelSelectBookBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.dialog.DialogSizeManager
import com.kova700.bookchat.util.image.image.loadUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
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
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogMakeChannelSelectBookBinding.inflate(inflater, container, false)
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
		with(binding) {
			bookImgSizeManager.setBookImgSize(bookImg)
			dialogSizeManager.setDialogSize(makeChannelSelectBookDialogLayout)
			bookImg.loadUrl(selectedBook.bookCoverImageUrl)
			selectedBookTitleTv.isSelected = true
			selectedBookAuthorsTv.isSelected = true
			selectedBookPublishAtTv.isSelected = true
			makeChannelBtn.setOnClickListener { onClickMakeChannel() }
		}
	}

	private fun onClickMakeChannel() {
		dismiss()
		onClickMakeChannel.invoke()
	}
}