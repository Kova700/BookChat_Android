package com.example.bookchat.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.SearchResultBookDetailAdapter
import com.example.bookchat.adapter.SearchResultBookSimpleAdapter
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ActivitySearchTapResultDetailBinding
import com.example.bookchat.ui.fragment.SearchFragment
import com.example.bookchat.utils.Constants
import com.example.bookchat.viewmodel.SearchDetailViewModel
import com.example.bookchat.viewmodel.SearchViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchTapResultDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchTapResultDetailBinding
    private lateinit var searchDetailViewModel : SearchDetailViewModel
    private lateinit var searchResultBookDetailAdapter : SearchResultBookDetailAdapter
    var searchResultTotalItemCount = 0.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_tap_result_detail)
        val searchKeyword = getSearchKeyWord() ?: ""
        searchResultTotalItemCount = getItemCount() ?: "0"
        searchDetailViewModel = ViewModelProvider(this, ViewModelFactory(searchKeyword)).get(SearchDetailViewModel::class.java)
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
                // 책 다이얼로그 출력
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

    private fun getSearchKeyWord(): String? {
        return intent.getStringExtra(SearchFragment.EXTRA_SEARCH_KEYWORD)
    }
    private fun getItemCount() :String? {
        return intent.getStringExtra(SearchFragment.EXTRA_SEARCH_RESULT_ITEM_COUNT)
    }

    fun clickBackBtn(){
        //뒤로기가 전에
        //_searchTapStatus.value Result로 변경해줘야함 혹은 값 전달해줘야함함

        finish()
    }

}