package com.kova700.bookchat.feature.bookshelf.complete

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
import com.kova700.bookchat.core.navigation.MainNavigationViewModel
import com.kova700.bookchat.core.navigation.MainRoute
import com.kova700.bookchat.feature.bookshelf.complete.adapter.CompleteBookShelfAdapter
import com.kova700.bookchat.feature.bookshelf.complete.dialog.CompleteBookDialog
import com.kova700.bookchat.feature.bookshelf.complete.model.CompleteBookShelfItem
import com.kova700.bookchat.feature.bookshelf.databinding.FragmentCompleteBookshelfBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CompleteBookShelfFragment : Fragment() {

	private var _binding: FragmentCompleteBookshelfBinding? = null
	private val binding get() = _binding!!
	private val completeBookShelfViewModel: CompleteBookShelfViewModel by viewModels()
	private val mainNavigationViewmodel by activityViewModels<MainNavigationViewModel>()

	@Inject
	lateinit var completeBookShelfAdapter: CompleteBookShelfAdapter

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentCompleteBookshelfBinding.inflate(inflater, container, false)
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
		completeBookShelfViewModel.uiState.collect { uiState ->
			setViewState(uiState)
			completeBookShelfAdapter.submitList(uiState.completeItems)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		completeBookShelfViewModel.eventFlow.collect(::handleEvent)
	}

	private fun initViewState() {
		with(binding) {
			bookshelfEmptyLayout.addBookBtn.setOnClickListener {
				mainNavigationViewmodel.navigateTo(MainRoute.Search)
			}
			bookshelfRetryLayout.retryBtn.setOnClickListener {
				completeBookShelfViewModel.getBookShelfItems()
			}
		}
		initShimmerBook()
	}

	private fun initShimmerBook() {
		with(binding.completeBookshelfShimmerLayout) {
			bookImgSizeManager.setBookImgSize(completeBookshelfShimmer1.bookImg)
			bookImgSizeManager.setBookImgSize(completeBookshelfShimmer2.bookImg)
			bookImgSizeManager.setBookImgSize(completeBookshelfShimmer3.bookImg)
		}
	}

	private fun setViewState(uiState: CompleteBookShelfUiState) {
		with(binding) {
			bookshelfEmptyLayout.root.visibility =
				if (uiState.isEmpty) View.VISIBLE else View.GONE
			bookshelfRetryLayout.root.visibility =
				if (uiState.isInitError) View.VISIBLE else View.GONE
			bookshelfCompleteRcv.visibility =
				if (uiState.isNotEmpty) View.VISIBLE else View.GONE
			progressbar.visibility =
				if (uiState.isLoading) View.VISIBLE else View.GONE
			completeBookshelfShimmerLayout.root.visibility =
				if (uiState.isInitLoading) View.VISIBLE else View.GONE
					.also { completeBookshelfShimmerLayout.shimmerLayout.stopShimmer() }
		}
	}

	private fun initAdapter() {
		completeBookShelfAdapter.onItemClick = { itemPosition ->
			completeBookShelfViewModel.onItemClick(
				(completeBookShelfAdapter.currentList[itemPosition] as CompleteBookShelfItem.Item)
			)
		}
		completeBookShelfAdapter.onLongItemClick = { itemPosition, isSwiped ->
			completeBookShelfViewModel.onItemLongClick(
				(completeBookShelfAdapter.currentList[itemPosition] as CompleteBookShelfItem.Item),
				isSwiped
			)
		}
		completeBookShelfAdapter.onDeleteClick = { itemPosition ->
			completeBookShelfViewModel.onItemDeleteClick(
				(completeBookShelfAdapter.currentList[itemPosition] as CompleteBookShelfItem.Item)
			)
		}
	}

	private fun initRecyclerView() {
		val linearLayoutManager = LinearLayoutManager(requireContext())
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				completeBookShelfViewModel.loadNextBookShelfItems(
					linearLayoutManager.findLastVisibleItemPosition()
				)
			}
		}
		with(binding.bookshelfCompleteRcv) {
			adapter = completeBookShelfAdapter
			setHasFixedSize(true)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun moveToCompleteBookDialog(bookShelfListItem: CompleteBookShelfItem.Item) {
		val existingFragment = childFragmentManager.findFragmentByTag(DIALOG_TAG_COMPLETE)
		if (existingFragment != null) return
		val dialog = CompleteBookDialog()
		dialog.arguments = bundleOf(
			CompleteBookDialog.EXTRA_COMPLETE_BOOKSHELF_ITEM_ID to bookShelfListItem.bookShelfId
		)
		dialog.show(childFragmentManager, DIALOG_TAG_COMPLETE)
	}

	private fun handleEvent(event: CompleteBookShelfEvent) {
		when (event) {
			is CompleteBookShelfEvent.MoveToCompleteBookDialog ->
				moveToCompleteBookDialog(event.bookShelfListItem)

			is CompleteBookShelfEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.snackbarPoint
			)
		}
	}

	companion object {
		private const val DIALOG_TAG_COMPLETE = "DIALOG_TAG_COMPLETE"
	}

}