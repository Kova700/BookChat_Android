package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchTapDefaultBinding

/*추천 검색어 추가 예정*/
class SearchTapDefaultFragment : Fragment() {
    private lateinit var binding : FragmentSearchTapDefaultBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_default, container, false)
        return binding.root
    }

}