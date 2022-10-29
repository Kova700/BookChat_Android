package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapSearchingFragment : Fragment() {
    private val tempData = listOf("이펙트", "이펙트 효과", "이펙티브")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //추천 검색어 가져와야함

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}