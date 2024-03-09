package com.example.bookchat.ui.bookshelf

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookchat.ui.bookshelf.complete.CompleteBookShelfFragment
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfFragment
import com.example.bookchat.ui.bookshelf.wish.WishBookBookShelfFragment

//TODO : 개선 필요 (여기서 굳이 Fragment 객체를 가지지 않아도 되어보임
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
