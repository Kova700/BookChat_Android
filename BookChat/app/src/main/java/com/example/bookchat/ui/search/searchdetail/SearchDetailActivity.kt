package com.example.bookchat.ui.search.searchdetail

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySearchTapResultDetailBinding
import com.example.bookchat.domain.model.Book
import com.example.bookchat.ui.createchannel.MakeChannelBookSelectDialog
import com.example.bookchat.ui.search.SearchFragment
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_CLICKED_CHANNEL_ID
import com.example.bookchat.ui.search.adapter.SearchItemAdapter
import com.example.bookchat.ui.search.channelInfo.ChannelInfoActivity
import com.example.bookchat.ui.search.dialog.SearchBookDialog
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.ui.search.model.SearchTarget
import com.example.bookchat.utils.showSnackBar
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchDetailActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySearchTapResultDetailBinding

	@Inject
	lateinit var searchItemAdapter: SearchItemAdapter
	private val searchDetailViewModel: SearchDetailViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySearchTapResultDetailBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initAdapter()
		initRecyclerView()
		initViewState()
		initHeaderTitle()
		observeUiState()
		observeUiEvent()
	}

	private fun observeUiState() = lifecycleScope.launch {
		searchDetailViewModel.uiState.collect { state ->
			searchItemAdapter.submitList(state.searchItems)
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		searchDetailViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { searchDetailViewModel.onClickBackBtn() }
	}

	private fun setViewState(state: SearchDetailUiState) {
		binding.searchResultRcv.visibility =
			if (state.searchItems.isNotEmpty()) RecyclerView.VISIBLE else RecyclerView.GONE
		binding.resultEmptyLayout.root.visibility =
			if (state.searchItems.isEmpty()) RecyclerView.VISIBLE else RecyclerView.GONE
	}

	private fun initAdapter() {
		searchItemAdapter.onBookItemClick = { position ->
			searchDetailViewModel.onBookItemClick(
				((searchItemAdapter.currentList[position]) as SearchResultItem.BookItem)
			)
		}
		searchItemAdapter.onChannelItemClick = { position ->
			searchDetailViewModel.onChannelItemClick(
				((searchItemAdapter.currentList[position]) as SearchResultItem.ChannelItem).roomId
			)
		}
	}

	private fun initRecyclerView() {
		val flexboxLayoutManager =
			FlexboxLayoutManager(this).apply {
				justifyContent = JustifyContent.CENTER
				flexDirection = FlexDirection.ROW
				flexWrap = FlexWrap.WRAP
			}
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				searchDetailViewModel.loadNextData(
					flexboxLayoutManager.findLastVisibleItemPosition()
				)
			}
		}
		with(binding.searchResultRcv) {
			adapter = searchItemAdapter
			layoutManager = flexboxLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun initHeaderTitle() {
		binding.headerTitle.text =
			when (searchDetailViewModel.uiState.value.searchTarget) {
				SearchTarget.BOOK -> resources.getString(R.string.book)
				SearchTarget.CHANNEL -> resources.getString(R.string.chat_room)
				else -> ""
			}
	}

	private fun moveToChannelInfo(channelId: Long) {
		val intent = Intent(this, ChannelInfoActivity::class.java)
		intent.putExtra(EXTRA_CLICKED_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun moveToSearchTapBookDialog(book: Book) {
		val dialog = SearchBookDialog()
		dialog.arguments = bundleOf(SearchFragment.EXTRA_SEARCHED_BOOK_ITEM_ID to book.isbn)
		dialog.show(supportFragmentManager, SearchFragment.DIALOG_TAG_SEARCH_BOOK)
	}

	private fun moveToMakeChannelSelectBookDialog(book: Book) {
		val dialog = MakeChannelBookSelectDialog(
			onClickMakeChannel = { finishWithSelectedBook(book.isbn) },
			selectedBook = book
		)
		dialog.show(supportFragmentManager, SearchFragment.DIALOG_TAG_SELECT_BOOK)
	}

	private fun finishWithSelectedBook(bookIsbn: String) {
		intent.putExtra(EXTRA_SELECTED_BOOK_ISBN, bookIsbn)
		setResult(RESULT_OK, intent)
		finish()
	}

	private fun handleEvent(event: SearchDetailEvent) {
		when (event) {
			is SearchDetailEvent.MoveToChannelInfo -> moveToChannelInfo(event.channelId)
			is SearchDetailEvent.MoveToMakeChannelSelectBookDialog ->
				moveToMakeChannelSelectBookDialog(event.book)

			is SearchDetailEvent.MoveToSearchBookDialog -> moveToSearchTapBookDialog(event.book)
			SearchDetailEvent.MoveToBack -> finish()
			is SearchDetailEvent.ShowSnackBar -> binding.root.showSnackBar(textId = event.stringId)
		}
	}

	companion object {
		const val EXTRA_SELECTED_BOOK_ISBN = "EXTRA_SELECTED_BOOK_ISBN"
	}
}