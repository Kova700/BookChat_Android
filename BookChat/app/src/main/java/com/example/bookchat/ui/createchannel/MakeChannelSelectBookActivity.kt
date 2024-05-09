package com.example.bookchat.ui.createchannel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMakeChatRoomSelectBookBinding
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.ui.search.SearchFragment
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.example.bookchat.ui.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeChannelSelectBookActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMakeChatRoomSelectBookBinding
	//TODO : Navigation 사용으로 수정
	private val searchFragment by lazy {
		SearchFragment().apply {
			//여기서 searchFragment로 이동할 때, Extra Flag 날려야함
			arguments = bundleOf(EXTRA_SEARCH_PURPOSE to SearchPurpose.MAKE_CHANNEL)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_make_chat_room_select_book)
		inflateFragment(searchFragment, FRAGMENT_TAG_SEARCH)
	}

	private fun inflateFragment(newFragment: Fragment, tag: String) {
		val fragmentTransaction = supportFragmentManager.beginTransaction()
		with(fragmentTransaction) {
			setReorderingAllowed(true)
			add(R.id.select_book_frame_layout, newFragment, tag)
			commitNow()
		}
	}

	fun finishBookSelect(bookIsbn: String) {
		intent.putExtra(EXTRA_SELECTED_BOOK_ISBN, bookIsbn)
		setResult(RESULT_OK, intent)
		finish()
	}

	companion object {
		private const val FRAGMENT_TAG_SEARCH = "Search"
	}
}