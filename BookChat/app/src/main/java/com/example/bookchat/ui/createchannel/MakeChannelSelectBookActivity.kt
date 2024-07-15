package com.example.bookchat.ui.createchannel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityMakeChatRoomSelectBookBinding
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.example.bookchat.ui.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeChannelSelectBookActivity : AppCompatActivity() {
	private lateinit var navHostFragment: NavHostFragment
	private lateinit var binding: ActivityMakeChatRoomSelectBookBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_make_chat_room_select_book)
		initNavHost()
		intent.putExtra(EXTRA_SEARCH_PURPOSE, SearchPurpose.MAKE_CHANNEL)
	}

	private fun initNavHost() {
		navHostFragment =
			supportFragmentManager.findFragmentById(R.id.container_make_channel) as NavHostFragment
	}

	fun finishBookSelect(bookIsbn: String) {
		intent.putExtra(EXTRA_SELECTED_BOOK_ISBN, bookIsbn)
		setResult(RESULT_OK, intent)
		finish()
	}
}