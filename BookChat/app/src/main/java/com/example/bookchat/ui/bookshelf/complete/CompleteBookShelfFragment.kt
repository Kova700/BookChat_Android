package com.example.bookchat.ui.bookshelf.complete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentCompleteBookshelfBinding
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.bookshelf.complete.adapter.CompleteBookShelfAdapter
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog
import com.example.bookchat.ui.bookshelf.complete.model.CompleteBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CompleteBookShelfFragment : Fragment() {

	private var _binding: FragmentCompleteBookshelfBinding? = null
	private val binding get() = _binding!!
	private val completeBookShelfViewModel: CompleteBookShelfViewModel by viewModels()

	@Inject
	lateinit var completeBookShelfAdapter: CompleteBookShelfAdapter

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding =
			DataBindingUtil.inflate(
				inflater, R.layout.fragment_complete_bookshelf,
				container, false
			)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = completeBookShelfViewModel
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
		binding.bookshelfEmptyLayout.addBookBtn.setOnClickListener {
			(requireActivity() as MainActivity).navigateToSearchFragment()
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
				if (uiState.isEmptyData) View.VISIBLE else View.GONE
			bookshelfCompleteRcv.visibility =
				if (uiState.isEmptyDataORLoading.not()) View.VISIBLE else View.GONE
			completeBookshelfShimmerLayout.root.visibility =
				if (uiState.isLoading) View.VISIBLE else View.GONE
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
		val dialog = CompleteBookDialog()
		dialog.arguments =
			bundleOf(
				CompleteBookDialog.EXTRA_COMPLETE_BOOKSHELF_ITEM_ID to bookShelfListItem.bookShelfId
			)
		dialog.show(this.childFragmentManager, DIALOG_TAG_COMPLETE)
	}

	private fun handleEvent(event: CompleteBookShelfEvent) {
		when (event) {
			is CompleteBookShelfEvent.MoveToCompleteBookDialog ->
				moveToCompleteBookDialog(event.bookShelfListItem)

			is CompleteBookShelfEvent.MakeToast -> makeToast(event.stringId)
		}
	}

	companion object {
		private const val DIALOG_TAG_COMPLETE = "DIALOG_TAG_COMPLETE"
	}

}