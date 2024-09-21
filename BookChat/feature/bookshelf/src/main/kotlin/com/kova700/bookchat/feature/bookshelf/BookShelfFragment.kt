package com.kova700.bookchat.feature.bookshelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.complete.CompleteBookShelfFragment
import com.kova700.bookchat.feature.bookshelf.databinding.FragmentBookshelfBinding
import com.kova700.bookchat.feature.bookshelf.reading.ReadingBookShelfFragment
import com.kova700.bookchat.feature.bookshelf.wish.WishBookBookShelfFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookShelfFragment : Fragment() {

	private var _binding: FragmentBookshelfBinding? = null
	private val binding get() = _binding!!
	private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

	private val fragments: List<Fragment> =
		listOf(WishBookBookShelfFragment(), ReadingBookShelfFragment(), CompleteBookShelfFragment())

	private lateinit var viewPagerAdapter: ViewPagerAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentBookshelfBinding.inflate(inflater, container, false)
		viewPagerAdapter = ViewPagerAdapter(fragments, this)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initViewState()
		inflateFirstTab()
		observeEvent()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun initViewState() {
		binding.viewPager.adapter = viewPagerAdapter
		initTapLayout()
	}

	private fun inflateFirstTab() {
		val tabIndex = BookShelfState.READING.getTabIndex()
		binding.viewPager.setCurrentItem(tabIndex, false)
	}

	private fun observeEvent() = viewLifecycleOwner.lifecycleScope.launch {
		bookShelfViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initTapLayout() {
		TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
			tab.text = resources.getString(bookShelfTapNameList[position])
		}.attach()
	}

	private fun changeTab(targetState: BookShelfState) {
		binding.viewPager.currentItem = targetState.getTabIndex()
	}

	private fun BookShelfState.getTabIndex(): Int {
		return when (this) {
			BookShelfState.WISH -> fragments.indexOfFirst { it is WishBookBookShelfFragment }
			BookShelfState.READING -> fragments.indexOfFirst { it is ReadingBookShelfFragment }
			BookShelfState.COMPLETE -> fragments.indexOfFirst { it is CompleteBookShelfFragment }
		}
	}

	private fun handleEvent(event: BookShelfEvent) = when (event) {
		is BookShelfEvent.ChangeBookShelfTab -> changeTab(event.targetState)
	}

	companion object {
		private val bookShelfTapNameList =
			listOf(R.string.wish_book, R.string.reading_book, R.string.complete_book)
	}
}