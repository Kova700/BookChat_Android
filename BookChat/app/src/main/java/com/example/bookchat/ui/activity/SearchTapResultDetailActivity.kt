package com.example.bookchat.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.SearchResultBookDetailAdapter
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ActivitySearchTapResultDetailBinding
import com.example.bookchat.ui.dialog.SearchTapBookDialog
import com.example.bookchat.ui.fragment.SearchFragment
import com.example.bookchat.utils.Constants
import com.example.bookchat.viewmodel.SearchDetailViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapResultDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var searchDetailViewModelFactory : SearchDetailViewModel.AssistedFactory

    private lateinit var binding : ActivitySearchTapResultDetailBinding
    private val searchDetailViewModel : SearchDetailViewModel by viewModels{
        SearchDetailViewModel.provideFactory(searchDetailViewModelFactory, getSearchKeyWord())
    }
    private lateinit var searchResultBookDetailAdapter : SearchResultBookDetailAdapter
    var searchResultTotalItemCount = 0.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_tap_result_detail)
        searchResultTotalItemCount = getItemCount() ?: "0"
        binding.lifecycleOwner = this
        binding.activity = this
        binding.viewmodel = searchDetailViewModel

        initAdapter()
        initSearchResultDetailRcv()
        observePagingBookData()
    }

    private fun observePagingBookData() = lifecycleScope.launch {
        searchDetailViewModel.pagingBookData.collect{ pagingBookData ->
            Log.d(Constants.TAG, "SearchTapResultFragment: observePagingBookData() - pagingBookData : $pagingBookData")
            searchResultBookDetailAdapter.submitData(pagingBookData)
        }
    }

    private fun initAdapter(){
        val bookItemClickListener = object: SearchResultBookDetailAdapter.OnItemClickListener{
            override fun onItemClick(book : Book) {
                val dialog = SearchTapBookDialog(book)
                dialog.show(this@SearchTapResultDetailActivity.supportFragmentManager,"SearchTapBookDialog")
            }
        }
        searchResultBookDetailAdapter = SearchResultBookDetailAdapter()
        searchResultBookDetailAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initSearchResultDetailRcv(){
        with(binding){
            searchResultDetailRcv.adapter = searchResultBookDetailAdapter
            searchResultDetailRcv.setHasFixedSize(true)
            searchResultDetailRcv.layoutManager = GridLayoutManager(this@SearchTapResultDetailActivity,3)
        }
    }

    private fun getSearchKeyWord(): String {
        return intent.getStringExtra(SearchFragment.EXTRA_SEARCH_KEYWORD) ?: ""
    }
    private fun getItemCount() :String? {
        return intent.getStringExtra(SearchFragment.EXTRA_SEARCH_RESULT_ITEM_COUNT)
    }

    fun clickBackBtn(){
        finish()
    }

}