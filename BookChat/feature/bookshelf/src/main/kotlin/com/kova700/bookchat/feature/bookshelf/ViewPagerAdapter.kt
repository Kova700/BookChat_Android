package com.kova700.bookchat.feature.bookshelf

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
	private val childFragments: List<Fragment>,
	private val parentFragment: Fragment
) : FragmentStateAdapter(parentFragment) {

	override fun getItemCount(): Int {
		return childFragments.size
	}

	override fun createFragment(position: Int): Fragment {
		return childFragments[position]
	}

}
