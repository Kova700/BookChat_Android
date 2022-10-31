package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.adapter.WishBookTabAdapter
import com.example.bookchat.databinding.FragmentSearchTapResultBinding

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapResultFragment : Fragment() {
    private lateinit var binding :FragmentSearchTapResultBinding

    private lateinit var chatRoomAdapter: MainChatRoomAdapter // 임시
    private lateinit var wishBookAdapter : WishBookTabAdapter // 임시

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //검색결과 가져와야함
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_result, container, false)
        initRecyclerView()
        return binding.root
    }

    private fun initRecyclerView(){
        with(binding){
            chatRoomAdapter = MainChatRoomAdapter()
            searchResultChatRoomRcv.adapter = chatRoomAdapter
            searchResultChatRoomRcv.setHasFixedSize(true)
            searchResultChatRoomRcv.layoutManager = LinearLayoutManager(requireContext())

            wishBookAdapter = WishBookTabAdapter()
            searchResultBookRcv.adapter = wishBookAdapter
            searchResultBookRcv.setHasFixedSize(true)
            searchResultBookRcv.layoutManager = GridLayoutManager(requireContext(),3) //중앙정렬 해야함 Or 개수 화면에 따라 늘어나게 설정
        }
    }
}