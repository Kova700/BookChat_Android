package com.example.bookchat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookchat.ui.fragment.CompleteBookTabFragment
import com.example.bookchat.ui.fragment.ReadingBookTabFragment
import com.example.bookchat.ui.fragment.WishBookTabFragment

class PagerFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val wishBookTabFragment by lazy { WishBookTabFragment() }
    private val readingBookTabFragment by lazy { ReadingBookTabFragment() }
    private val completeBookTabFragment by lazy { CompleteBookTabFragment() }

    private var fragments: List<Fragment> =
        listOf(wishBookTabFragment,readingBookTabFragment, completeBookTabFragment)

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
