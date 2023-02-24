package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.reading_bookshelf.ReadingBookShelfDataAdapter
import com.example.bookchat.adapter.reading_bookshelf.ReadingBookShelfHeaderAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.FragmentReadingBookshelfBinding
import com.example.bookchat.ui.dialog.PageInputBottomSheetDialog
import com.example.bookchat.ui.dialog.ReadingTapBookDialog
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.BookShelfViewModel.PagingViewEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadingBookShelfFragment : Fragment() {
    lateinit var binding: FragmentReadingBookshelfBinding
    private lateinit var readingBookShelfHeaderAdapter: ReadingBookShelfHeaderAdapter
    lateinit var readingBookShelfDataAdapter: ReadingBookShelfDataAdapter
    val bookShelfViewModel: BookShelfViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_reading_bookshelf, container, false)
        with(binding) {
            lifecycleOwner = this@ReadingBookShelfFragment
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
        bookShelfViewModel.readingBookCombined.observe(viewLifecycleOwner) { PagingBookShelfItem ->
            readingBookShelfDataAdapter.submitData(viewLifecycleOwner.lifecycle, PagingBookShelfItem)
        }
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch {
        readingBookShelfDataAdapter.loadStateFlow.collect { combinedLoadStates ->
            if (combinedLoadStates.refresh is LoadState.NotLoading) {
                if(readingBookShelfDataAdapter.itemCount == 0){
                    bookShelfViewModel.readingBookTotalCountCache = 0
                }
                initializeModificationEvents()
            }
        }
    }

    private fun initializeModificationEvents() {
        bookShelfViewModel.readingBookModificationEvents.value =
            bookShelfViewModel.readingBookModificationEvents.value.filterIsInstance<PagingViewEvent.RemoveWaiting>()
        bookShelfViewModel.renewTotalItemCount(BookShelfViewModel.MODIFICATION_EVENT_FLAG_READING)
    }

    private fun initRecyclerView() {
        with(binding) {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                readingBookShelfHeaderAdapter,
                readingBookShelfDataAdapter
            )
            readingBookRcv.adapter = concatAdapter
            readingBookRcv.setHasFixedSize(true)
            readingBookRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initAdapter() {
        val bookItemClickListener = object : ReadingBookShelfDataAdapter.OnItemClickListener {
            override fun onItemClick(bookShelfDataItem: BookShelfDataItem) {
                val dialog = ReadingTapBookDialog(bookShelfDataItem)
                dialog.show(childFragmentManager, DIALOG_TAG_READING)
            }
        }
        val pageBtnClickListener = object : ReadingBookShelfDataAdapter.OnItemClickListener {
            override fun onItemClick(bookShelfDataItem: BookShelfDataItem) {
                val pageInputBottomSheetDialog = PageInputBottomSheetDialog(bookShelfDataItem)
                pageInputBottomSheetDialog.show(childFragmentManager, DIALOG_TAG_PAGE_INPUT)
            }
        }
        readingBookShelfHeaderAdapter = ReadingBookShelfHeaderAdapter(bookShelfViewModel)
        readingBookShelfDataAdapter = ReadingBookShelfDataAdapter(bookShelfViewModel)
        readingBookShelfDataAdapter.setItemClickListener(bookItemClickListener)
        readingBookShelfDataAdapter.setPageBtnClickListener(pageBtnClickListener)
        observeReadingBookCount()
    }

    private fun observeReadingBookCount() = lifecycleScope.launch{
        bookShelfViewModel.readingBookTotalCount.collect{
            readingBookShelfHeaderAdapter.notifyItemChanged(0)
        }
    }

    private fun initRefreshEvent() {
        binding.swipeRefreshLayoutReading.setOnRefreshListener {
            readingBookShelfDataAdapter.refresh()
            binding.swipeRefreshLayoutReading.isRefreshing = false
        }
    }

    companion object {
        private const val DIALOG_TAG_READING = "ReadingTapBookDialog"
        private const val DIALOG_TAG_PAGE_INPUT = "PageInputBottomSheetDialog"
    }
}