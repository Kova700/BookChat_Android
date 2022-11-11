package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.bookchat.R
import com.example.bookchat.adapter.PagerFragmentStateAdapter
import com.example.bookchat.databinding.FragmentBookShelfBinding
import com.example.bookchat.utils.Constants.TAG
import com.google.android.material.tabs.TabLayoutMediator

class BookShelfFragment : Fragment() {

    lateinit var binding : FragmentBookShelfBinding
    lateinit var pagerAdapter :PagerFragmentStateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_shelf,container,false)
        pagerAdapter = PagerFragmentStateAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        initTapLayout()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "BookShelfFragment: onPageSelected() - Page ${position+1}")
                Log.d(TAG, "BookShelfFragment: onCreateView() - parentFragmentManager : ${parentFragmentManager.fragments}")
                Log.d(TAG, "BookShelfFragment: onCreateView() - childFragmentManager : ${childFragmentManager.fragments}")
            }
        })

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