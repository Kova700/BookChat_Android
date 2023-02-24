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
import com.example.bookchat.adapter.complete_bookshelf.CompleteBookShelfDataAdapter
import com.example.bookchat.adapter.complete_bookshelf.CompleteBookShelfHeaderAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.FragmentCompleteBookshelfBinding
import com.example.bookchat.ui.dialog.CompleteTapBookDialog
import com.example.bookchat.utils.RefreshManager
import com.example.bookchat.utils.RefreshManager.popRefreshCompleteFlag
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.BookShelfViewModel.PagingViewEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompleteBookShelfFragment : Fragment() {
    lateinit var binding : FragmentCompleteBookshelfBinding
    private lateinit var completeBookShelfHeaderAdapter : CompleteBookShelfHeaderAdapter
    lateinit var completeBookShelfDataAdapter : CompleteBookShelfDataAdapter
    val bookShelfViewModel: BookShelfViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_complete_bookshelf,container,false)
        with(binding){
            lifecycleOwner = this@CompleteBookShelfFragment
            viewmodel = bookShelfViewModel
        }
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingCompleteBookData()
        observeAdapterLoadState()

        return binding.root
    }

    private fun observePagingCompleteBookData(){
        bookShelfViewModel.completeBookCombined.observe(viewLifecycleOwner){ pagingData ->
            completeBookShelfDataAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
        }
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch{
        completeBookShelfDataAdapter.loadStateFlow.collect{ combinedLoadStates ->
            if(combinedLoadStates.refresh is LoadState.NotLoading) {
                if(completeBookShelfDataAdapter.itemCount == 0){
                    with(bookShelfViewModel){
                        completeBookTotalCountCache = 0 + getRemoveWaitingCount(completeBookModificationEvents).toLong()
                        initializeModificationEvents()
                        if (completeBookModificationEvents.value.isEmpty()){
                            binding.bookshelfEmptyLayout.visibilty = View.VISIBLE
                            binding.swipeRefreshLayoutComplete.visibility = View.GONE
                        }
                        return@collect
                    }
                }
                bookShelfViewModel.isCompleteBookLoaded = true
                binding.bookshelfEmptyLayout.visibilty = View.GONE
                binding.swipeRefreshLayoutComplete.visibility = View.VISIBLE
                initializeModificationEvents()
            }
        }
    }
    private fun initializeModificationEvents(){
        bookShelfViewModel.completeBookModificationEvents.value =
            bookShelfViewModel.completeBookModificationEvents.value.filterIsInstance<PagingViewEvent.RemoveWaiting>()
        bookShelfViewModel.renewTotalItemCount(BookShelfViewModel.MODIFICATION_EVENT_FLAG_COMPLETE)
        popRefreshCompleteFlag()
    }

    private fun initAdapter(){
        val bookItemClickListener = object: CompleteBookShelfDataAdapter.OnItemClickListener {
            override fun onItemClick(bookShelfDataItem : BookShelfDataItem) {
                val dialog = CompleteTapBookDialog(bookShelfDataItem)
                dialog.show(this@CompleteBookShelfFragment.childFragmentManager, DIALOG_TAG_COMPLETE)
            }
        }
        completeBookShelfHeaderAdapter = CompleteBookShelfHeaderAdapter(bookShelfViewModel)
        completeBookShelfDataAdapter = CompleteBookShelfDataAdapter(bookShelfViewModel)
        completeBookShelfDataAdapter.setItemClickListener(bookItemClickListener)
        observeCompleteBookCount()
    }

    private fun observeCompleteBookCount() = lifecycleScope.launch{
        bookShelfViewModel.completeBookTotalCount.collect{
            completeBookShelfHeaderAdapter.notifyItemChanged(0)
        }
    }

    private fun initRecyclerView(){
        with(binding){
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                completeBookShelfHeaderAdapter,
                completeBookShelfDataAdapter
            )
            completeBookRcv.adapter = concatAdapter
            completeBookRcv.setHasFixedSize(true)
            completeBookRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutComplete.setOnRefreshListener {
            completeBookShelfDataAdapter.refresh()
            binding.swipeRefreshLayoutComplete.isRefreshing = false
        }
    }
    override fun onResume() {
        super.onResume()
        if(RefreshManager.hasCompleteBookShelfNewData()){
            completeBookShelfDataAdapter.refresh()
            popRefreshCompleteFlag()
        }
    }

    companion object{
        private const val DIALOG_TAG_COMPLETE = "CompleteTapBookDialog"
    }

}