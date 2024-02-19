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
import com.example.bookchat.ui.adapter.bookshelf.PagerFragmentStateAdapter
import com.example.bookchat.databinding.FragmentBookShelfBinding
import com.example.bookchat.ui.viewmodel.BookShelfViewModel
import com.example.bookchat.ui.viewmodel.BookShelfViewModel.BookShelfEvent
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookShelfFragment : Fragment() {

	lateinit var binding: FragmentBookShelfBinding
	lateinit var pagerAdapter: PagerFragmentStateAdapter
	val bookShelfViewModel: BookShelfViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_shelf, container, false)
		pagerAdapter = PagerFragmentStateAdapter(this)
		with(binding) {
			lifecycleOwner = this@BookShelfFragment
			viewPager.adapter = pagerAdapter
		}
		initTapLayout()
		inflateFirstTab(1)
		observeEvent()

		return binding.root
	}

	private fun inflateFirstTab(tabIndex: Int) {
		binding.viewPager.setCurrentItem(tabIndex, false)
	}

	private fun observeEvent() {
		lifecycleScope.launch {
			bookShelfViewModel.eventFlow.collect { event -> handleEvent(event) }
		}
	}

	private fun initTapLayout() {
		TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
			tab.text = resources.getString(bookShelfTapNameList[position])
		}.attach()
	}

	fun changeTab(tabIndex: Int) {
		binding.viewPager.currentItem = tabIndex
	}

	private fun handleEvent(event: BookShelfEvent) = when (event) {
		is BookShelfEvent.ChangeBookShelfTab -> {
			changeTab(event.tapIndex)
		}
	}

	companion object {
		private val bookShelfTapNameList =
			listOf(R.string.wish_book, R.string.reading_book, R.string.complete_book)
	}
}