package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchTapHistoryBinding

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapHistoryFragment : Fragment() {
    private lateinit var binding : FragmentSearchTapHistoryBinding
    private val tempData = listOf("이펙티브", "인간관계론", "UI/UX")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //최근 검색어 받아와야함
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_history, container, false)

        //아이템 연결하고 클릭 이벤트 연결해야함

        return binding.root
    }
}