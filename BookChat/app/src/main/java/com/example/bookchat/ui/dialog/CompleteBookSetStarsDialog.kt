package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.databinding.DialogCompleteBookSetStarsBinding
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.ui.viewmodel.SearchTapBookDialogViewModel
import com.example.bookchat.ui.viewmodel.SearchTapBookDialogViewModel.SearchTapDialogState
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.RefreshManager
import com.example.bookchat.utils.RefreshManager.BookShelfRefreshFlag
import com.example.bookchat.utils.toStarRating
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CompleteBookSetStarsDialog @AssistedInject constructor(
	private val bookShelfRepository: BookShelfRepository,
	@Assisted val book: Book
) : DialogFragment() {

	private lateinit var binding: DialogCompleteBookSetStarsBinding
	private val searchTapBookDialogViewModel: SearchTapBookDialogViewModel by viewModels({ requireParentFragment() })

	val starRating = MutableStateFlow<Float>(0.0F)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_complete_book_set_stars, container, false)
		with(binding) {
			lifecycleOwner = this@CompleteBookSetStarsDialog
			dialog = this@CompleteBookSetStarsDialog
		}
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

		return binding.root
	}

	fun clickOkBtn() {
		if (starRating.value == 0F) {
			makeToast(R.string.complete_bookshelf_star_set_empty)
			return
		}
		requestRegisterCompleteBook()
	}

	fun clickCancelBtn() {
		this.dismiss()
	}

	private fun requestRegisterCompleteBook() = lifecycleScope.launch {
		val requestRegisterBookShelfBook =
			RequestRegisterBookShelfBook(book, ReadingStatus.COMPLETE, starRating.value.toStarRating())
		runCatching { bookShelfRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
			.onSuccess { registerCompleteBookSuccessCallBack() }
			.onFailure { makeToast(R.string.complete_bookshelf_register_fail) }
	}

	private fun registerCompleteBookSuccessCallBack() {
		makeToast(R.string.complete_bookshelf_register_success)
		RefreshManager.addBookShelfRefreshFlag(BookShelfRefreshFlag.Complete)
		searchTapBookDialogViewModel.setDialogState(
			SearchTapDialogState.AlreadyInBookShelf(
				ReadingStatus.COMPLETE
			)
		)
		this@CompleteBookSetStarsDialog.dismiss()
	}

	private fun makeToast(stringId: Int) {
		Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
	}

	@dagger.assisted.AssistedFactory
	interface AssistedFactory {
		fun create(book: Book): CompleteBookSetStarsDialog
	}
}