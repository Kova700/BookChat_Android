package com.example.bookchat.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookchat.ui.fragment.CompleteBookTabFragment
import com.example.bookchat.ui.fragment.ReadingBookShelfFragment
import com.example.bookchat.ui.fragment.WishBookTabFragment

class PagerFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    val wishBookTabFragment by lazy { WishBookTabFragment() }
    val readingBookShelfFragment by lazy { ReadingBookShelfFragment() }
    val completeBookTabFragment by lazy { CompleteBookTabFragment() }

    private var fragments: List<Fragment> =
        listOf(wishBookTabFragment,readingBookShelfFragment, completeBookTabFragment)

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
