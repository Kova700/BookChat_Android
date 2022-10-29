package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchTapDefaultBinding

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapDefaultFragment : Fragment() {
    private lateinit var binding : FragmentSearchTapDefaultBinding
    private val tempData = listOf("데일카네기 인간관계론", "이펙티브 자바", "데이터베이스 개론과 실습")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //추천 검색어 받아와야함
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_default, container, false)
        val adapter = ArrayAdapter<String>(requireContext(),R.layout.item_search_tap_default,R.id.search_tap_default_tv,tempData)
        binding.recomendListView.adapter = adapter
//        binding.recomendListView.setOnItemClickListener()

        return binding.root
    }

}