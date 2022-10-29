package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapHistoryFragment : Fragment() {
    private val tempData = listOf("이펙티브", "인간관계론", "UI/UX")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //최근 검색어 받아와야함
        
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}