package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.ui.adapter.search.booksearch.SearchResultBookDetailDataAdapter
import com.example.bookchat.ui.adapter.search.booksearch.SearchResultBookDummyAdapter
import com.example.bookchat.ui.adapter.search.chatroomsearch.SearchChatRoomDetailAdapter
import com.example.bookchat.data.Book
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.databinding.ActivitySearchTapResultDetailBinding
import com.example.bookchat.ui.dialog.MakeChatRoomSelectBookDialog
import com.example.bookchat.ui.dialog.SearchTapBookDialog
import com.example.bookchat.ui.fragment.SearchFragment
import com.example.bookchat.ui.fragment.SearchFragment.Companion.EXTRA_CHAT_SEARCH_FILTER
import com.example.bookchat.ui.fragment.SearchFragment.Companion.EXTRA_NECESSARY_DATA
import com.example.bookchat.ui.fragment.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.example.bookchat.ui.fragment.SearchTapResultFragment.Companion.DIALOG_TAG_SEARCH_BOOK
import com.example.bookchat.ui.fragment.SearchTapResultFragment.Companion.DIALOG_TAG_SELECT_BOOK
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.ChatSearchFilter
import com.example.bookchat.utils.SearchPurpose
import com.example.bookchat.ui.viewmodel.SearchDetailViewModel
import com.example.bookchat.ui.viewmodel.SearchViewModel.NecessaryDataFlagInDetail
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapResultDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var searchDetailViewModelFactory: SearchDetailViewModel.AssistedFactory

    private lateinit var binding: ActivitySearchTapResultDetailBinding
    private lateinit var searchResultBookDetailDataAdapter: SearchResultBookDetailDataAdapter
    private lateinit var searchResultBookDummyAdapter: SearchResultBookDummyAdapter
    private lateinit var searchChatRoomDetailAdapter: SearchChatRoomDetailAdapter
    private val necessaryDataFlag by lazy { getExtraNecessaryDataFlag() }

    private val searchPurpose: SearchPurpose by lazy { getExtraSearchPurpose() }
    private val searchDetailViewModel: SearchDetailViewModel by viewModels {
        SearchDetailViewModel.provideFactory(
            searchDetailViewModelFactory,
            searchKeyword = getExtraSearchKeyWord(),
            necessaryDataFlag = getExtraNecessaryDataFlag(),
            chatSearchFilter = getExtraChatSearchFilter()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_tap_result_detail)
        with(binding) {
            lifecycleOwner = this@SearchTapResultDetailActivity
            activity = this@SearchTapResultDetailActivity
            viewmodel = searchDetailViewModel
        }
        initRcvAdapter()
        initRcv()
        observePagingData()

        if (necessaryDataFlag is NecessaryDataFlagInDetail.Book) {
            observeBookAdapterLoadState()
        }
    }

    private fun initRcvAdapter() {
        when (necessaryDataFlag) {
            is NecessaryDataFlagInDetail.Book -> initBookRcvAdapter()
            is NecessaryDataFlagInDetail.ChatRoom -> initChatRcvAdapter()
        }
    }

    private fun initRcv() {
        when (necessaryDataFlag) {
            is NecessaryDataFlagInDetail.Book -> initBookRcv()
            is NecessaryDataFlagInDetail.ChatRoom -> initChatRoomtRcv()
        }
    }

    private fun observePagingData() = lifecycleScope.launch {
        when (necessaryDataFlag) {
            is NecessaryDataFlagInDetail.Book -> observePagingBookData()
            is NecessaryDataFlagInDetail.ChatRoom -> observePagingChatRoomData()
        }
    }

    private suspend fun observePagingBookData() {
        searchDetailViewModel.pagingBookData.collect { pagingBookData ->
            searchResultBookDetailDataAdapter.submitData(pagingBookData)
        }
    }

    private suspend fun observePagingChatRoomData() {
        searchDetailViewModel.pagingChatRoomData.collect { pagingBookData ->
            searchChatRoomDetailAdapter.submitData(pagingBookData)
        }
    }

    private fun observeBookAdapterLoadState() = lifecycleScope.launch {
        searchResultBookDetailDataAdapter.loadStateFlow.collectLatest { loadState ->
            if (loadState.append.endOfPaginationReached) {
                searchResultBookDummyAdapter.dummyItemCount =
                    BookImgSizeManager.getFlexBoxDummyItemCount(searchResultBookDetailDataAdapter.itemCount.toLong())
                searchResultBookDummyAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initBookRcvAdapter() {
        val bookItemClickListener = object : SearchResultBookDetailDataAdapter.OnItemClickListener {
            override fun onItemClick(book: Book) {
                when (searchPurpose) {
                    is SearchPurpose.DefaultSearch -> {
                        val dialog = SearchTapBookDialog(book)
                        dialog.show(
                            this@SearchTapResultDetailActivity.supportFragmentManager,
                            DIALOG_TAG_SEARCH_BOOK
                        )
                    }
                    is SearchPurpose.MakeChatRoom -> {
                        val dialog = MakeChatRoomSelectBookDialog(book)
                        dialog.show(
                            this@SearchTapResultDetailActivity.supportFragmentManager,
                            DIALOG_TAG_SELECT_BOOK
                        )
                    }
                    else -> {}
                }
            }
        }
        searchResultBookDetailDataAdapter = SearchResultBookDetailDataAdapter()
        searchResultBookDummyAdapter = SearchResultBookDummyAdapter()
        searchResultBookDetailDataAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initChatRcvAdapter() {
        val chatRoomItemClickListener = object : SearchChatRoomDetailAdapter.OnItemClickListener {
            override fun onItemClick(wholeChatRoomListItem: WholeChatRoomListItem) {
                val intent = Intent(
                    this@SearchTapResultDetailActivity,
                    ChatRoomInfoActivity::class.java
                )
                intent.putExtra(EXTRA_CLICKED_CHAT_ROOM_ITEM, wholeChatRoomListItem)
                startActivity(intent)
            }
        }
        searchChatRoomDetailAdapter = SearchChatRoomDetailAdapter()
        searchChatRoomDetailAdapter.setItemClickListener(chatRoomItemClickListener)
    }

    private fun initBookRcv() {
        with(binding) {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                searchResultBookDetailDataAdapter,
                searchResultBookDummyAdapter
            )
            searchResultDetailBookRcv.adapter = concatAdapter
            searchResultDetailBookRcv.setHasFixedSize(true)
            val flexboxLayoutManager =
                FlexboxLayoutManager(this@SearchTapResultDetailActivity).apply {
                    justifyContent = JustifyContent.CENTER
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
            searchResultDetailBookRcv.layoutManager = flexboxLayoutManager
        }
    }

    private fun initChatRoomtRcv() {
        with(binding) {
            searchResultDetailChatRoomRcv.adapter = searchChatRoomDetailAdapter
            searchResultDetailChatRoomRcv.setHasFixedSize(true)
            searchResultDetailChatRoomRcv.layoutManager =
                LinearLayoutManager(this@SearchTapResultDetailActivity)
        }
    }

    private fun getExtraSearchPurpose(): SearchPurpose {
        return intent.getSerializableExtra(EXTRA_SEARCH_PURPOSE) as? SearchPurpose
            ?: throw Exception("SearchPurpose does not exist.")
    }

    private fun getExtraSearchKeyWord(): String {
        return intent.getStringExtra(SearchFragment.EXTRA_SEARCH_KEYWORD)
            ?: throw Exception("SearchKeyWord does not exist.")
    }

    private fun getExtraNecessaryDataFlag(): NecessaryDataFlagInDetail {
        return intent.getSerializableExtra(EXTRA_NECESSARY_DATA) as? NecessaryDataFlagInDetail
            ?: throw Exception("NecessaryDataFlag does not exist.")
    }

    private fun getExtraChatSearchFilter(): ChatSearchFilter {
        return intent.getSerializableExtra(EXTRA_CHAT_SEARCH_FILTER) as? ChatSearchFilter
            ?: throw Exception("ChatSearchFilter does not exist.")
    }

    fun returnSelectedBook(book: Book) {
        intent.putExtra(EXTRA_SELECTED_BOOK, book)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun clickBackBtn() {
        finish()
    }

    companion object {
        const val EXTRA_SELECTED_BOOK = "SELECTED_BOOK"
        const val EXTRA_CLICKED_CHAT_ROOM_ITEM = "EXTRA_CLICKED_CHAT_ROOM_ITEM"
    }
}