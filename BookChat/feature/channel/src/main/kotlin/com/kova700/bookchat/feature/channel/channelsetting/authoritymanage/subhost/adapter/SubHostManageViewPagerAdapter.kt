package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SubHostManageViewPagerAdapter(
	private val childFragments: List<Fragment>,
	private val fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

	override fun getItemCount(): Int {
		return childFragments.size
	}

	override fun createFragment(position: Int): Fragment {
		return childFragments[position]
	}

}
