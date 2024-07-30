package com.example.bookchat.ui.bookshelf.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.FragmentWishBookshelfBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.bookshelf.BookShelfViewModel
import com.example.bookchat.ui.bookshelf.wish.adapter.WishBookShelfAdapter
import com.example.bookchat.ui.bookshelf.wish.dialog.WishBookDialog
import com.example.bookchat.ui.bookshelf.wish.dialog.WishBookDialog.Companion.EXTRA_WISH_BOOKSHELF_ITEM_ID
import com.example.bookchat.ui.bookshelf.wish.model.WishBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.showSnackBar
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WishBookBookShelfFragment : Fragment() {

	private var _binding: FragmentWishBookshelfBinding? = null
	private val binding get() = _binding!!
	private val wishBookShelfViewModel: WishBookShelfViewModel by viewModels()
	private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

	@Inject
	lateinit var wishBookShelfAdapter: WishBookShelfAdapter

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentWishBookshelfBinding.inflate(inflater, container, false)
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

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		wishBookShelfViewModel.eventFlow.collect(::handleEvent)
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		wishBookShelfViewModel.uiState.collect { uiState ->
			setViewState(uiState)
			wishBookShelfAdapter.submitList(uiState.wishItems)
		}
	}

	private fun initViewState() {
		binding.bookshelfEmptyLayout.addBookBtn.setOnClickListener {
			(requireActivity() as MainActivity).navigateToSearchFragment()
		}
		initShimmerBook()
	}

	private fun initShimmerBook() {
		with(binding.wishBookshelfShimmerLayout) {
			bookImgSizeManager.setBookImgSize(shimmerBook1.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook2.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook3.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook4.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook5.bookImg)
		}
	}

	private fun setViewState(uiState: WishBookShelfUiState) {
		with(binding) {
			bookshelfEmptyLayout.root.visibility =
				if (uiState.isEmpty) View.VISIBLE else View.GONE
			bookshelfWishRcv.visibility =
				if (uiState.isInitLoading.not()) View.VISIBLE else View.GONE
			progressbar.visibility =
				if (uiState.isLoading) View.VISIBLE else View.GONE
			wishBookshelfShimmerLayout.root.visibility =
				if (uiState.isInitLoading) View.VISIBLE else View.GONE
					.also { wishBookshelfShimmerLayout.shimmerLayout.stopShimmer() }
		}
	}

	private fun initAdapter() {
		wishBookShelfAdapter.onItemClick = { itemPosition ->
			wishBookShelfViewModel.onItemClick(
				(wishBookShelfAdapter.currentList[itemPosition] as WishBookShelfItem.Item)
			)
		}
	}

	private fun initRecyclerView() {
		val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
			.apply {
				justifyContent = JustifyContent.CENTER
				flexDirection = FlexDirection.ROW
				flexWrap = FlexWrap.WRAP
			}
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				wishBookShelfViewModel.loadNextBookShelfItems(
					flexboxLayoutManager.findLastVisibleItemPosition()
				)
			}
		}
		with(binding.bookshelfWishRcv) {
			adapter = wishBookShelfAdapter
			layoutManager = flexboxLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun moveToWishBookDialog(bookShelfListItem: WishBookShelfItem.Item) {
		val dialog = WishBookDialog()
		dialog.arguments = bundleOf(EXTRA_WISH_BOOKSHELF_ITEM_ID to bookShelfListItem.bookShelfId)
		dialog.show(this.childFragmentManager, DIALOG_TAG_WISH)
	}

	private fun changeBookShelfTab(bookShelfState: BookShelfState) {
		bookShelfViewModel.moveToOtherTab(bookShelfState)
	}

	private fun handleEvent(event: WishBookShelfEvent) {
		when (event) {
			is WishBookShelfEvent.MoveToWishBookDialog ->
				moveToWishBookDialog(event.item)

			is WishBookShelfEvent.ChangeBookShelfTab ->
				changeBookShelfTab(bookShelfState = event.targetState)

			is WishBookShelfEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.snackbarPoint
			)
		}
	}

	companion object {
		private const val DIALOG_TAG_WISH = "DIALOG_TAG_WISH"
	}
}