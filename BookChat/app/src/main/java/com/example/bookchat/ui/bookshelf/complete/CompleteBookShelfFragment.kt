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
import com.example.bookchat.ui.bookshelf.complete.adapter.CompleteBookShelfDataAdapter
import com.example.bookchat.ui.bookshelf.complete.dialog.CompleteBookDialog
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CompleteBookShelfFragment : Fragment() {

	private var _binding: FragmentCompleteBookshelfBinding? = null
	private val binding get() = _binding!!
	private val completeBookShelfViewModel: CompleteBookShelfViewModel by viewModels()

	@Inject
	lateinit var completeBookShelfDataAdapter: CompleteBookShelfDataAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.fragment_complete_bookshelf, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = completeBookShelfViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		observeUiEvent()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		completeBookShelfViewModel.eventFlow.collect(::handleEvent)
	}

	//TODO : List Empty UI 연결
	//TODO : swipeRefreshLayoutComplete API 연결
	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		completeBookShelfViewModel.uiState.collect { uiState ->
			completeBookShelfDataAdapter.submitList(uiState.completeItems)
		}
	}

	private fun initAdapter() {
		completeBookShelfDataAdapter.onItemClick = { itemPosition ->
			completeBookShelfViewModel.onItemClick(
				(completeBookShelfDataAdapter.currentList[itemPosition] as CompleteBookShelfItem.Item)
					.bookShelfListItem
			)
		}
		completeBookShelfDataAdapter.onLongItemClick = { itemPosition, isSwipe ->
			completeBookShelfViewModel.onItemLongClick(
				(completeBookShelfDataAdapter.currentList[itemPosition] as CompleteBookShelfItem.Item)
					.bookShelfListItem, isSwipe
			)
		}
		completeBookShelfDataAdapter.onDeleteClick = { itemPosition ->
			completeBookShelfViewModel.onItemDeleteClick(
				(completeBookShelfDataAdapter.currentList[itemPosition] as CompleteBookShelfItem.Item)
					.bookShelfListItem
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
		with(binding.completeBookRcv) {
			adapter = completeBookShelfDataAdapter
			setHasFixedSize(true)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun moveToCompleteBookDialog(bookShelfListItem: BookShelfListItem) {
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
		}
	}

	companion object {
		private const val DIALOG_TAG_COMPLETE = "DIALOG_TAG_COMPLETE"
	}

}