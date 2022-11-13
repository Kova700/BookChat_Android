package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchat.R
import com.example.bookchat.adapter.PagerFragmentStateAdapter
import com.example.bookchat.databinding.FragmentBookShelfBinding
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class BookShelfFragment : Fragment() {

    private lateinit var binding : FragmentBookShelfBinding
    private lateinit var pagerAdapter :PagerFragmentStateAdapter
    private lateinit var bookShelfViewModel: BookShelfViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_shelf,container,false)
        bookShelfViewModel = ViewModelProvider(this, ViewModelFactory()).get(BookShelfViewModel::class.java)
        pagerAdapter = PagerFragmentStateAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        initTapLayout()

        return binding.root
    }

    private fun initTapLayout(){
        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab, position ->
            tab.text = bookShelfTapNameList[position]
        }.attach()
    }

    companion object {
        private val bookShelfTapNameList = listOf("독서예정","독서중","독서완료")
    }
}