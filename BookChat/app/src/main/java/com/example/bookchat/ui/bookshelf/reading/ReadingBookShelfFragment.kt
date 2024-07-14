package com.example.bookchat.ui.bookshelf.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentReadingBookshelfBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.bookshelf.BookShelfViewModel
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.reading.adapter.ReadingBookShelfDataAdapter
import com.example.bookchat.ui.bookshelf.reading.dialog.PageInputBottomSheetDialog
import com.example.bookchat.ui.bookshelf.reading.dialog.PageInputBottomSheetDialog.Companion.EXTRA_PAGE_INPUT_ITEM_ID
import com.example.bookchat.ui.bookshelf.reading.dialog.ReadingBookDialog
import com.example.bookchat.ui.bookshelf.reading.dialog.ReadingBookDialog.Companion.EXTRA_READING_BOOKSHELF_ITEM_ID
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReadingBookShelfFragment : Fragment() {

	private var _binding: FragmentReadingBookshelfBinding? = null
	private val binding get() = _binding!!
	private val readingBookShelfViewModel: ReadingBookShelfViewModel by viewModels()
	private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

	@Inject
	lateinit var readingBookShelfDataAdapter: ReadingBookShelfDataAdapter

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.fragment_reading_bookshelf, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = readingBookShelfViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		initViewState()
		observeUiEvent()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		readingBookShelfViewModel.uiState.collect { uiState ->
			setViewState(uiState)
			readingBookShelfDataAdapter.submitList(uiState.readingItems)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		readingBookShelfViewModel.eventFlow.collect(::handleEvent)
	}

	private fun initViewState() {
		binding.bookshelfEmptyLayout.addBookBtn.setOnClickListener {
			(requireActivity() as MainActivity).navigateToSearchFragment()
		}
	}

	private fun setViewState(uiState: ReadingBookShelfUiState) {
		binding.bookshelfEmptyLayout.root.visibility =
			if (uiState.isEmptyReadingData) View.VISIBLE else View.GONE
		binding.bookshelfReadingRcv.visibility =
			if (uiState.isEmptyReadingData.not()) View.VISIBLE else View.GONE
	}

	private fun initRecyclerView() {
		val linearLayoutManager = LinearLayoutManager(requireContext())
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				readingBookShelfViewModel.loadNextBookShelfItems(
					linearLayoutManager.findLastVisibleItemPosition()
				)
			}
		}
		with(binding.bookshelfReadingRcv) {
			adapter = readingBookShelfDataAdapter
			setHasFixedSize(true)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun initAdapter() {
		readingBookShelfDataAdapter.onItemClick = { itemPosition ->
			readingBookShelfViewModel.onItemClick(
				(readingBookShelfDataAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
					.bookShelfListItem
			)
		}
		readingBookShelfDataAdapter.onLongItemClick = { itemPosition, isSwipe ->
			readingBookShelfViewModel.onItemLongClick(
				(readingBookShelfDataAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
					.bookShelfListItem, isSwipe
			)
		}
		readingBookShelfDataAdapter.onPageInputBtnClick = { itemPosition ->
			readingBookShelfViewModel.onPageInputBtnClick(
				(readingBookShelfDataAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
					.bookShelfListItem
			)
		}
		readingBookShelfDataAdapter.onDeleteClick = { itemPosition ->
			readingBookShelfViewModel.onItemDeleteClick(
				(readingBookShelfDataAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
					.bookShelfListItem
			)
		}
	}

	private fun moveToReadingBookDialog(bookShelfListItem: BookShelfListItem) {
		val dialog = ReadingBookDialog()
		dialog.arguments = bundleOf(EXTRA_READING_BOOKSHELF_ITEM_ID to bookShelfListItem.bookShelfId)
		dialog.show(this.childFragmentManager, DIALOG_TAG_READING)
	}

	private fun moveToPageInputDialog(bookShelfListItem: BookShelfListItem) {
		val dialog = PageInputBottomSheetDialog()
		dialog.arguments = bundleOf(EXTRA_PAGE_INPUT_ITEM_ID to bookShelfListItem.bookShelfId)
		dialog.show(childFragmentManager, DIALOG_TAG_PAGE_INPUT)
	}

	private fun changeBookShelfTab(bookShelfState: BookShelfState) {
		bookShelfViewModel.moveToOtherTab(bookShelfState)
	}

	private fun handleEvent(event: ReadingBookShelfEvent) {
		when (event) {
			is ReadingBookShelfEvent.MoveToReadingBookDialog ->
				moveToReadingBookDialog(event.bookShelfListItem)

			is ReadingBookShelfEvent.MoveToPageInputDialog ->
				moveToPageInputDialog(event.bookShelfListItem)

			is ReadingBookShelfEvent.ChangeBookShelfTab ->
				changeBookShelfTab(bookShelfState = event.targetState)

			is ReadingBookShelfEvent.MakeToast -> makeToast(event.stringId)
		}
	}

	companion object {
		private const val DIALOG_TAG_READING = "DIALOG_TAG_READING"
		private const val DIALOG_TAG_PAGE_INPUT = "DIALOG_TAG_PAGE_INPUT"
	}
}