package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.adapter.PagerFragmentStateAdapter
import com.example.bookchat.databinding.FragmentBookShelfBinding
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.BookShelfViewModel.BookShelfEvent
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookShelfFragment : Fragment() {

    lateinit var binding : FragmentBookShelfBinding
    lateinit var pagerAdapter :PagerFragmentStateAdapter
    val bookShelfViewModel: BookShelfViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_shelf,container,false)
        pagerAdapter = PagerFragmentStateAdapter(this)
        binding.lifecycleOwner = this
        binding.viewPager.adapter = pagerAdapter
        initTapLayout()
        changeTab(1)
        observeEvent()

        return binding.root
    }

    private fun inflateFirstTap(){
//        if(bookShelfViewModel.isBookShelfEmpty.value == false) {
//            changeTab(1)
//            return
//        }
        changeTab(0)
    }

    private fun observeEvent(){
        lifecycleScope.launch {
            bookShelfViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun initTapLayout(){
        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab, position ->
            tab.text = bookShelfTapNameList[position]
        }.attach()
    }

    //자식 Fragment로부터 서재 탭 이동이 가능한 메소드
    fun changeTab(tapIndex :Int){
        binding.viewPager.currentItem = tapIndex
    }

    private fun handleEvent(event: BookShelfEvent) = when(event){
        is BookShelfEvent.ChangeBookShelfTab -> { changeTab(event.tapIndex) }
    }

    companion object {
        private val bookShelfTapNameList = listOf("독서예정","독서중","독서완료")
    }
}