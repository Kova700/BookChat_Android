package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.SwipeHelperCallback
import com.example.bookchat.SwipeHelperCallback.SwipeViewType
import com.example.bookchat.adapter.ReadingBookTabAdapter
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.FragmentReadingBookTabBinding
import com.example.bookchat.ui.dialog.PageInputBottomSheetDialog
import com.example.bookchat.ui.dialog.ReadingTapBookDialog
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ReadingBookTabFragment :Fragment() {
    lateinit var binding : FragmentReadingBookTabBinding
    lateinit var readingBookAdapter :ReadingBookTabAdapter
    lateinit var bookShelfViewModel: BookShelfViewModel

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
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingReadingBookData()
        observeAdapterLoadState()

        return binding.root
    }

    private fun observePagingReadingBookData() {
        bookShelfViewModel.readingBookCombined.observe(viewLifecycleOwner){ PagingBookShelfItem ->
            readingBookAdapter.submitData(viewLifecycleOwner.lifecycle,PagingBookShelfItem)
        }
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch{
        readingBookAdapter.loadStateFlow.collect{ combinedLoadStates ->
            if(combinedLoadStates.refresh is LoadState.NotLoading) initializeModificationEvents()
        }
    }

    private fun initializeModificationEvents(){
        if(bookShelfViewModel.readingBookModificationEvents.value.isNotEmpty()){
            bookShelfViewModel.readingBookModificationEvents.value = emptyList()
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
        val swipeHelperCallback = SwipeHelperCallback(SwipeViewType.Reading)
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
                dialog.show(this@ReadingBookTabFragment.childFragmentManager,DIALOG_TAG_READING)
            }
        }
        val pageBtnClickListener = object :ReadingBookTabAdapter.OnItemClickListener{
            override fun onItemClick(book: BookShelfItem) {
                //아래에서 slidingUp layout올라와서 페이지 변경 UI 노출
                val pageInputBottomSheetDialog = PageInputBottomSheetDialog()
                pageInputBottomSheetDialog.show(childFragmentManager,DIALOG_TAG_PAGE_INPUT)
            }
        }

        readingBookAdapter = ReadingBookTabAdapter(bookShelfViewModel)
        readingBookAdapter.setItemClickListener(bookItemClickListener)
        readingBookAdapter.setPageBtnClickListener(pageBtnClickListener)
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutReading.setOnRefreshListener {
            readingBookAdapter.refresh()
            binding.swipeRefreshLayoutReading.isRefreshing = false
        }
    }

    companion object {
        private const val DIALOG_TAG_READING = "ReadingTapBookDialog"
        private const val DIALOG_TAG_PAGE_INPUT = "PageInputBottomSheetDialog"
    }
}