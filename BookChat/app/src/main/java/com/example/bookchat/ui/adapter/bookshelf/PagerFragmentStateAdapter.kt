package com.example.bookchat.ui.adapter.bookshelf

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookchat.ui.fragment.CompleteBookShelfFragment
import com.example.bookchat.ui.fragment.ReadingBookShelfFragment
import com.example.bookchat.ui.fragment.WishBookBookShelfFragment

class PagerFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    val wishBookBookShelfFragment by lazy { WishBookBookShelfFragment() }
    val readingBookShelfFragment by lazy { ReadingBookShelfFragment() }
    val completeBookShelfFragment by lazy { CompleteBookShelfFragment() }

    private var fragments: List<Fragment> =
        listOf(wishBookBookShelfFragment,readingBookShelfFragment, completeBookShelfFragment)

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}
