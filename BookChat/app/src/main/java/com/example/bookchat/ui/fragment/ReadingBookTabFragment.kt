package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.ReadingBookTabAdapter
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.FragmentReadingBookTabBinding
import com.example.bookchat.utils.Constants
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.ReadingBookTapViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ReadingBookTabFragment :Fragment() {
    private lateinit var binding : FragmentReadingBookTabBinding
    private lateinit var readingBookAdapter :ReadingBookTabAdapter
    private lateinit var readingBookTapViewModel: ReadingBookTapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_reading_book_tab,container,false)
        readingBookTapViewModel = ViewModelProvider(this, ViewModelFactory()).get(ReadingBookTapViewModel::class.java)
        with(binding){
            lifecycleOwner = this@ReadingBookTabFragment
            viewmodel = readingBookTapViewModel
        }
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingReadingBookData()

        return binding.root
    }

    private fun observePagingReadingBookData()= lifecycleScope.launch {
        readingBookTapViewModel.readingBookResult.collect{ PagingBookShelfItem ->
            Log.d(TAG, "ReadingBookTabFragment: observePagingReadingBookData() - PagingBookShelfItem :$PagingBookShelfItem")
            readingBookAdapter.submitData(PagingBookShelfItem)
        }
    }

    private fun initRecyclerView(){
        with(binding){
            readingBookRcv.adapter = readingBookAdapter
            readingBookRcv.setHasFixedSize(true)
            readingBookRcv.layoutManager = LinearLayoutManager(requireContext())
            readingBookRcv.viewTreeObserver.addOnScrollChangedListener {
                binding.swipeRefreshLayoutReading.isEnabled = (readingBookRcv.scrollY == 0)
            }
        }
    }

    private fun initAdapter(){
        val bookItemClickListener = object: ReadingBookTabAdapter.OnItemClickListener{
            override fun onItemClick(book : BookShelfItem) {
                //다이얼로그 출력
            }
        }
        readingBookAdapter = ReadingBookTabAdapter()
        readingBookAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutReading.setOnRefreshListener {
            readingBookAdapter.refresh()
            binding.swipeRefreshLayoutReading.isRefreshing = false
        }
    }

}