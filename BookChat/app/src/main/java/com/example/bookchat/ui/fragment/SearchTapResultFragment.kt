package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.search.booksearch.SearchResultBookDummyAdapter
import com.example.bookchat.adapter.search.booksearch.SearchResultBookSimpleDataAdapter
import com.example.bookchat.adapter.search.chatroomsearch.SearchChatRoomSimpleAdapter
import com.example.bookchat.data.Book
import com.example.bookchat.data.SearchChatRoomListItem
import com.example.bookchat.databinding.FragmentSearchTapResultBinding
import com.example.bookchat.ui.dialog.MakeChatRoomSelectBookDialog
import com.example.bookchat.ui.dialog.SearchTapBookDialog
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.SearchPurpose
import com.example.bookchat.viewmodel.SearchViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchTapResultFragment : Fragment() {
    private lateinit var binding: FragmentSearchTapResultBinding
    private lateinit var searchResultBookSimpleDataAdapter: SearchResultBookSimpleDataAdapter
    private lateinit var searchResultBookDummyAdapter: SearchResultBookDummyAdapter
    private lateinit var searchChatRoomSimpleAdapter: SearchChatRoomSimpleAdapter

    private val searchViewModel: SearchViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_result, container, false)
        binding.viewmodel = searchViewModel
        binding.lifecycleOwner = this

        initAdapter()
        initRcv()
        observeSimpleBookSearchResult()
        observeSimpleChatRoomResult()
        return binding.root
    }

    private fun initAdapter() {
        initSearchResultBookAdapter()
        initSearchResultChatRoomAdapter()
    }

    private fun initRcv() {
        initSearchResultBookRcv()
        initSearchResultChatRoomRcv()
    }

    private fun initSearchResultBookAdapter() {
        val bookItemClickListener = object : SearchResultBookSimpleDataAdapter.OnItemClickListener {
            override fun onItemClick(book: Book) {
                when(searchViewModel.searchPurpose){
                    is SearchPurpose.Search -> {
                        val dialog = SearchTapBookDialog(book)
                        dialog.show(childFragmentManager, DIALOG_TAG_SEARCH_BOOK)
                    }
                    is SearchPurpose.MakeChatRoom -> {
                        val dialog = MakeChatRoomSelectBookDialog(book)
                        dialog.show(childFragmentManager, DIALOG_TAG_SELECT_BOOK)
                    }
                }

            }
        }
        searchResultBookSimpleDataAdapter = SearchResultBookSimpleDataAdapter()
        searchResultBookDummyAdapter = SearchResultBookDummyAdapter()
        searchResultBookSimpleDataAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initSearchResultChatRoomAdapter(){
        val chatRoomItemClickListener = object :SearchChatRoomSimpleAdapter.OnItemClickListener{
            override fun onItemClick(searchChatRoomListItem: SearchChatRoomListItem) {
                //채팅방 소개 페이지로 이동 (입장 가능)
            }
        }
        searchChatRoomSimpleAdapter =  SearchChatRoomSimpleAdapter()
        searchChatRoomSimpleAdapter.setItemClickListener(chatRoomItemClickListener)
    }

    private fun initSearchResultBookRcv() {
        with(binding) {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                searchResultBookSimpleDataAdapter,
                searchResultBookDummyAdapter
            )
            searchResultBookSimpleRcv.adapter = concatAdapter
            searchResultBookSimpleRcv.setHasFixedSize(true)
            val flexboxLayoutManager =
                FlexboxLayoutManager(requireContext()).apply {
                    justifyContent = JustifyContent.CENTER
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
            searchResultBookSimpleRcv.layoutManager = flexboxLayoutManager
        }
    }

    private fun initSearchResultChatRoomRcv(){
        with(binding){
            searchResultChatRoomSimpleRcv.adapter = searchChatRoomSimpleAdapter
            searchResultChatRoomSimpleRcv.setHasFixedSize(true)
            searchResultChatRoomSimpleRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeSimpleBookSearchResult() = lifecycleScope.launch{
        searchViewModel.simpleBookSearchResult.collectLatest{ books ->
            searchResultBookSimpleDataAdapter.books = books
            searchResultBookSimpleDataAdapter.notifyDataSetChanged()
            setDummyBookCount()
        }
    }

    private fun setDummyBookCount(){
        searchResultBookDummyAdapter.dummyItemCount =
            BookImgSizeManager.getFlexBoxDummyItemCount(searchResultBookSimpleDataAdapter.itemCount.toLong())
        searchResultBookDummyAdapter.notifyDataSetChanged()
    }

    private fun observeSimpleChatRoomResult() = lifecycleScope.launch{
        searchViewModel.simpleChatRoomSearchResult.collectLatest { chatRooms ->
            searchChatRoomSimpleAdapter.chatRooms = chatRooms
            searchChatRoomSimpleAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        const val DIALOG_TAG_SEARCH_BOOK = "SearchTapBookDialog"
        const val DIALOG_TAG_SELECT_BOOK = "MakeChatRoomSelectBookDialog"
    }
}