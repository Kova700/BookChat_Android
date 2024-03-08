package com.example.bookchat.ui.bookshelf.wish

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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentWishBookshelfBinding
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.BookShelfViewModel
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.wish.adapter.WishBookShelfDataAdapter
import com.example.bookchat.ui.bookshelf.wish.adapter.WishBookShelfDummyDataAdapter
import com.example.bookchat.ui.bookshelf.wish.adapter.WishBookShelfHeaderAdapter
import com.example.bookchat.ui.bookshelf.wish.dialog.WishBookDialog
import com.example.bookchat.ui.bookshelf.wish.dialog.WishBookDialog.Companion.EXTRA_WISH_BOOKSHELF_ITEM_ID
import com.example.bookchat.utils.BookImgSizeManager
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
	lateinit var wishBookShelfHeaderAdapter: WishBookShelfHeaderAdapter

	@Inject
	lateinit var wishBookShelfDataAdapter: WishBookShelfDataAdapter

	@Inject
	lateinit var wishBookShelfDummyDataAdapter: WishBookShelfDummyDataAdapter

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.fragment_wish_bookshelf, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = wishBookShelfViewModel
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
		wishBookShelfViewModel.eventFlow.collect(::handleEvent)
	}

	//TODO : List Empty UI 연결
	//TODO : swipeRefreshLayoutComplete API 연결
	//TODO : DummyItem없애고 GridLayout spanCount를 화면 사이즈로 계산해서 사용가능하게 수정
	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		wishBookShelfViewModel.uiState.collect { uiState ->
			wishBookShelfDataAdapter.submitList(uiState.wishItems)
			wishBookShelfHeaderAdapter.totalItemCount = uiState.totalItemCount
			wishBookShelfHeaderAdapter.notifyItemChanged(0)
			wishBookShelfDummyDataAdapter.dummyItemCount =
				BookImgSizeManager.getFlexBoxDummyItemCount(uiState.wishItems.size)
			wishBookShelfDummyDataAdapter.notifyDataSetChanged()
		}
	}

	private fun initAdapter() {
		wishBookShelfDataAdapter.onItemClick = { itemPosition ->
			wishBookShelfViewModel.onItemClick(
				wishBookShelfDataAdapter.currentList[itemPosition]
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
		val concatAdapterConfig =
			ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
		val concatAdapter = ConcatAdapter(
			concatAdapterConfig,
			wishBookShelfHeaderAdapter,
			wishBookShelfDataAdapter,
			wishBookShelfDummyDataAdapter
		)
		with(binding.wishBookRcv) {
			adapter = concatAdapter
			layoutManager = flexboxLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun moveToWishBookDialog(bookShelfListItem: BookShelfListItem) {
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
		}
	}

	companion object {
		private const val DIALOG_TAG_WISH = "DIALOG_TAG_WISH"
	}
}