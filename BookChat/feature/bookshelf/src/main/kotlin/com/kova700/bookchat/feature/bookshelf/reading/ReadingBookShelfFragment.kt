package com.kova700.bookchat.feature.bookshelf.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.navigation.MainNavigationViewModel
import com.kova700.bookchat.core.navigation.MainRoute
import com.kova700.bookchat.feature.bookshelf.BookShelfViewModel
import com.kova700.bookchat.feature.bookshelf.databinding.FragmentReadingBookshelfBinding
import com.kova700.bookchat.feature.bookshelf.reading.adapter.ReadingBookShelfAdapter
import com.kova700.bookchat.feature.bookshelf.reading.dialog.PageInputBottomSheetDialog
import com.kova700.bookchat.feature.bookshelf.reading.dialog.PageInputBottomSheetDialog.Companion.EXTRA_PAGE_INPUT_ITEM_ID
import com.kova700.bookchat.feature.bookshelf.reading.dialog.ReadingBookDialog
import com.kova700.bookchat.feature.bookshelf.reading.dialog.ReadingBookDialog.Companion.EXTRA_READING_BOOKSHELF_ITEM_ID
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReadingBookShelfFragment : Fragment() {
	private var _binding: FragmentReadingBookshelfBinding? = null
	private val binding get() = _binding!!
	private val readingBookShelfViewModel: ReadingBookShelfViewModel by viewModels()
	private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()
	private val mainNavigationViewmodel by activityViewModels<MainNavigationViewModel>()

	@Inject
	lateinit var readingBookShelfAdapter: ReadingBookShelfAdapter

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentReadingBookshelfBinding.inflate(inflater, container, false)
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
			readingBookShelfAdapter.submitList(uiState.readingItems)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		readingBookShelfViewModel.eventFlow.collect(::handleEvent)
	}

	private fun initViewState() {
		with(binding) {
			bookshelfEmptyLayout.addBookBtn.setOnClickListener {
				mainNavigationViewmodel.navigateTo(MainRoute.Search)
			}
			bookshelfRetryLayout.retryBtn.setOnClickListener {
				readingBookShelfViewModel.getInitBookShelfItems()
			}
		}
		initShimmerBook()
	}

	private fun initShimmerBook() {
		with(binding.readingBookshelfShimmerLayout) {
			bookImgSizeManager.setBookImgSize(readingBookshelfShimmer1.bookImg)
			bookImgSizeManager.setBookImgSize(readingBookshelfShimmer2.bookImg)
			bookImgSizeManager.setBookImgSize(readingBookshelfShimmer3.bookImg)
		}
	}

	private fun setViewState(uiState: ReadingBookShelfUiState) {
		with(binding) {
			bookshelfEmptyLayout.root.visibility =
				if (uiState.isEmpty) View.VISIBLE else View.GONE
			bookshelfRetryLayout.root.visibility =
				if (uiState.isInitError) View.VISIBLE else View.GONE
			bookshelfReadingRcv.visibility =
				if (uiState.isNotEmpty) View.VISIBLE else View.GONE
			progressbar.visibility =
				if (uiState.isPagingLoading) View.VISIBLE else View.GONE
			readingBookshelfShimmerLayout.root.visibility =
				if (uiState.isInitLoading) View.VISIBLE else View.GONE
					.also { readingBookshelfShimmerLayout.shimmerLayout.stopShimmer() }
		}
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
			adapter = readingBookShelfAdapter
			setHasFixedSize(true)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun initAdapter() {
		readingBookShelfAdapter.onItemClick = { itemPosition ->
			readingBookShelfViewModel.onItemClick(
				(readingBookShelfAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
			)
		}
		readingBookShelfAdapter.onLongItemClick = { itemPosition, isSwiped ->
			readingBookShelfViewModel.onItemLongClick(
				(readingBookShelfAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item),
				isSwiped
			)
		}
		readingBookShelfAdapter.onPageInputBtnClick = { itemPosition ->
			readingBookShelfViewModel.onPageInputBtnClick(
				(readingBookShelfAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
			)
		}
		readingBookShelfAdapter.onDeleteClick = { itemPosition ->
			readingBookShelfViewModel.onItemDeleteClick(
				(readingBookShelfAdapter.currentList[itemPosition] as ReadingBookShelfItem.Item)
			)
		}
		readingBookShelfAdapter.onClickPagingRetryBtn = {
			readingBookShelfViewModel.getBookShelfItems()
		}
	}

	private fun moveToReadingBookDialog(bookShelfListItem: ReadingBookShelfItem.Item) {
		val existingFragment = childFragmentManager.findFragmentByTag(DIALOG_TAG_READING)
		if (existingFragment != null) return
		val dialog = ReadingBookDialog()
		dialog.arguments = bundleOf(EXTRA_READING_BOOKSHELF_ITEM_ID to bookShelfListItem.bookShelfId)
		dialog.show(this.childFragmentManager, DIALOG_TAG_READING)
	}

	private fun moveToPageInputDialog(bookShelfListItem: ReadingBookShelfItem.Item) {
		val existingFragment = childFragmentManager.findFragmentByTag(DIALOG_TAG_PAGE_INPUT)
		if (existingFragment != null) return
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

			is ReadingBookShelfEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.snackbarPoint
			)
		}
	}

	companion object {
		private const val DIALOG_TAG_READING = "DIALOG_TAG_READING"
		private const val DIALOG_TAG_PAGE_INPUT = "DIALOG_TAG_PAGE_INPUT"
	}
}