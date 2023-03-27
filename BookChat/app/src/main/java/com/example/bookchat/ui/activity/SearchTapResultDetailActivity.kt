package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import com.example.bookchat.R
import com.example.bookchat.adapter.booksearch.SearchResultBookDetailDataAdapter
import com.example.bookchat.adapter.booksearch.SearchResultBookDummyAdapter
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ActivitySearchTapResultDetailBinding
import com.example.bookchat.ui.dialog.SearchTapBookDialog
import com.example.bookchat.ui.fragment.SearchFragment
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.viewmodel.SearchDetailViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapResultDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var searchDetailViewModelFactory: SearchDetailViewModel.AssistedFactory

    private lateinit var binding: ActivitySearchTapResultDetailBinding
    private lateinit var searchResultBookDetailDataAdapter: SearchResultBookDetailDataAdapter
    private lateinit var searchResultBookDummyAdapter: SearchResultBookDummyAdapter
    private val searchDetailViewModel: SearchDetailViewModel by viewModels {
        SearchDetailViewModel.provideFactory(searchDetailViewModelFactory, getSearchKeyWord())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_tap_result_detail)
        with(binding) {
            lifecycleOwner = this@SearchTapResultDetailActivity
            activity = this@SearchTapResultDetailActivity
            viewmodel = searchDetailViewModel
        }

        initAdapter()
        initRecyclerView()
        observePagingBookData()
        observeAdapterLoadState()
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch {
        searchResultBookDetailDataAdapter.loadStateFlow.collectLatest { loadState ->
            if (loadState.append.endOfPaginationReached) {
                searchResultBookDummyAdapter.dummyItemCount =
                    BookImgSizeManager.getFlexBoxDummyItemCount(searchResultBookDetailDataAdapter.itemCount.toLong())
                searchResultBookDummyAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observePagingBookData() = lifecycleScope.launch {
        searchDetailViewModel.pagingBookData.collect { pagingBookData ->
            searchResultBookDetailDataAdapter.submitData(pagingBookData)
        }
    }

    private fun initAdapter() {
        val bookItemClickListener = object : SearchResultBookDetailDataAdapter.OnItemClickListener {
            override fun onItemClick(book: Book) {
                val dialog = SearchTapBookDialog(book)
                dialog.show(
                    this@SearchTapResultDetailActivity.supportFragmentManager,
                    DIALOG_TAG_SEARCH
                )
            }
        }
        searchResultBookDetailDataAdapter = SearchResultBookDetailDataAdapter()
        searchResultBookDummyAdapter = SearchResultBookDummyAdapter()
        searchResultBookDetailDataAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initRecyclerView() {
        with(binding) {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                searchResultBookDetailDataAdapter,
                searchResultBookDummyAdapter
            )
            searchResultDetailRcv.adapter = concatAdapter
            searchResultDetailRcv.setHasFixedSize(true)
            val flexboxLayoutManager =
                FlexboxLayoutManager(this@SearchTapResultDetailActivity).apply {
                    justifyContent = JustifyContent.CENTER
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
            searchResultDetailRcv.layoutManager = flexboxLayoutManager
        }
    }

    private fun getSearchKeyWord(): String {
        return intent.getStringExtra(SearchFragment.EXTRA_SEARCH_KEYWORD) ?: ""
    }

    fun clickBackBtn() {
        finish()
    }

    companion object {
        private const val DIALOG_TAG_SEARCH = "SearchTapBookDialog"
    }

}