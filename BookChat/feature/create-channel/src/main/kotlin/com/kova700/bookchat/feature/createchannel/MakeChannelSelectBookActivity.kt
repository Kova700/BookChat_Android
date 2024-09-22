package com.kova700.bookchat.feature.createchannel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.kova700.bookchat.feature.createchannel.databinding.ActivityMakeChannelSelectBookBinding
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import dagger.hilt.android.AndroidEntryPoint
import com.kova700.bookchat.feature.createchannel.R as ccR

@AndroidEntryPoint
class MakeChannelSelectBookActivity : AppCompatActivity() {
	private lateinit var navHostFragment: NavHostFragment
	private lateinit var binding: ActivityMakeChannelSelectBookBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMakeChannelSelectBookBinding.inflate(layoutInflater)
		setContentView(binding.root)
		intent.putExtra(EXTRA_SEARCH_PURPOSE, SearchPurpose.MAKE_CHANNEL)
		initNavHost()
		setFragmentResultListener()
	}

	private fun initNavHost() {
		navHostFragment =
			supportFragmentManager.findFragmentById(ccR.id.container_make_channel) as NavHostFragment
	}

	private fun setFragmentResultListener() {
		supportFragmentManager.setFragmentResultListener(
			EXTRA_SELECTED_BOOK_ISBN,
			this
		) { requestKey, bundle ->
			val bookIsbn = bundle.getString(EXTRA_SELECTED_BOOK_ISBN)
			bookIsbn?.let { finishBookSelect(it) }
		}
	}

	private fun finishBookSelect(bookIsbn: String) {
		intent.putExtra(EXTRA_SELECTED_BOOK_ISBN, bookIsbn)
		setResult(RESULT_OK, intent)
		finish()
	}
}