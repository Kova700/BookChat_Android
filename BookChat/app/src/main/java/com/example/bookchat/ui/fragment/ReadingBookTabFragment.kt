package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.SwipeHelperCallback
import com.example.bookchat.adapter.ReadingBookTabAdapter
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.FragmentReadingBookTabBinding
import com.example.bookchat.ui.activity.MainActivity
import com.example.bookchat.ui.dialog.ReadingTapBookDialog
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ReadingBookTabFragment :Fragment() {
    private lateinit var binding : FragmentReadingBookTabBinding
    private lateinit var readingBookAdapter :ReadingBookTabAdapter
    private lateinit var bookShelfViewModel: BookShelfViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_reading_book_tab,container,false)
        bookShelfViewModel = ViewModelProvider(requireParentFragment(), ViewModelFactory()).get(
            BookShelfViewModel::class.java)
        with(binding){
            lifecycleOwner = this@ReadingBookTabFragment
            viewmodel = bookShelfViewModel
        }
        Log.d(TAG, "ReadingBookTabFragment: onCreateView() - (requireActivity() is MainActivity) : ${(requireActivity() is MainActivity)}")
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingReadingBookData()

        return binding.root
    }

    private fun observePagingReadingBookData()= lifecycleScope.launch {
        bookShelfViewModel.readingBookResult.collect{ PagingBookShelfItem ->
            Log.d(TAG, "ReadingBookTabFragment: observePagingReadingBookData() - PagingBookShelfItem :$PagingBookShelfItem")
            readingBookAdapter.submitData(PagingBookShelfItem)
        }
    }

    private fun initRecyclerView(){
        with(binding){
            readingBookRcv.adapter = readingBookAdapter
            readingBookRcv.setHasFixedSize(true)
            readingBookRcv.layoutManager = LinearLayoutManager(requireContext())
            setSwipeHelperCallback(readingBookRcv)
        }
    }

    private fun setSwipeHelperCallback(recyclerView : RecyclerView){
        val swipeHelperCallback = SwipeHelperCallback().apply { setClamp(200f) }
        val itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.setOnTouchListener { _, _ ->
            swipeHelperCallback.removePreviousClamp(recyclerView)
            false
        }
    }

    private fun initAdapter(){
        val bookItemClickListener = object: ReadingBookTabAdapter.OnItemClickListener{
            override fun onItemClick(book : BookShelfItem) {
                val dialog = ReadingTapBookDialog(book)
                dialog.show(this@ReadingBookTabFragment.childFragmentManager,"ReadingTapBookDialog")
            }
        }
        val pageBtnClickListener = object :ReadingBookTabAdapter.OnItemClickListener{
            override fun onItemClick(book: BookShelfItem) {
                //아래에서 slidingUp layout올라와서 페이지 변경 UI 노출
                openPageInputSlide(book)
            }
        }
        val deleteItemListener = object :ReadingBookTabAdapter.OnItemClickListener{
            override fun onItemClick(book: BookShelfItem) {
                //아이템 삭제 API 호출
                //UI 갱신
                Toast.makeText(requireActivity(),"아이템 삭제!!",Toast.LENGTH_SHORT).show()
            }
        }

        readingBookAdapter = ReadingBookTabAdapter()
        readingBookAdapter.setItemClickListener(bookItemClickListener)
        readingBookAdapter.setPageBtnClickListener(pageBtnClickListener)
        readingBookAdapter.setdeleteClickListener(deleteItemListener)
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutReading.setOnRefreshListener {
            readingBookAdapter.refresh()
            binding.swipeRefreshLayoutReading.isRefreshing = false
        }
    }

    fun openPageInputSlide(book: BookShelfItem){
        (requireActivity() as MainActivity).openPageInputSlide(book)
    }

}