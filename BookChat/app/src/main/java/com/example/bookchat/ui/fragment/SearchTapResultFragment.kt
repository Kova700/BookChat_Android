package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.adapter.SearchResultBookSimpleAdapter
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.FragmentSearchTapResultBinding
import com.example.bookchat.ui.dialog.SearchTapBookDialog
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.SearchViewModel

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapResultFragment : Fragment() {
    private lateinit var binding :FragmentSearchTapResultBinding
    private lateinit var searchResultBookSimpleAdapter : SearchResultBookSimpleAdapter
    private val searchViewModel: SearchViewModel by viewModels({ requireParentFragment() })

    private lateinit var chatRoomAdapter: MainChatRoomAdapter // 임시

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_result, container, false)
        binding.viewmodel = searchViewModel
        binding.lifecycleOwner = this

        initAdapter()
        initRcv()
        requireParentFragment()
        return binding.root
    }

    private fun initAdapter(){
        initSearchResultBookAdapter()
        initSearchResultChatRoomAdapter()
    }

    private fun initRcv(){
        initSearchResultBookRcv()
        initSearchResultChatRoomRcv()
    }

    private fun initSearchResultBookRcv(){
        with(binding){
            searchResultBookSimpleRcv.adapter = searchResultBookSimpleAdapter
            searchResultBookSimpleRcv.setHasFixedSize(true)
            searchResultBookSimpleRcv.layoutManager = GridLayoutManager(requireContext(),3) //중앙정렬 해야함 Or 개수 화면에 따라 늘어나게 설정
        }
    }
    private fun initSearchResultChatRoomRcv(){
        with(binding){
            searchResultChatRoomSimpleRcv.adapter = chatRoomAdapter
            searchResultChatRoomSimpleRcv.setHasFixedSize(true)
            searchResultChatRoomSimpleRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initSearchResultBookAdapter(){
        val bookItemClickListener = object: SearchResultBookSimpleAdapter.OnItemClickListener{
            override fun onItemClick(book : Book) {
                val dialog = SearchTapBookDialog(book)
                Log.d(TAG, "SearchTapResultFragment: onItemClick() - book : $book")
                dialog.show(childFragmentManager,"SearchTapBookDialog")
            }
        }
        searchResultBookSimpleAdapter = SearchResultBookSimpleAdapter()
        searchResultBookSimpleAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initSearchResultChatRoomAdapter(){
        chatRoomAdapter = MainChatRoomAdapter() //임시
    }
}