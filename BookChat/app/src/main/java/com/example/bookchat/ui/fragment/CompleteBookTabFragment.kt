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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.SwipeHelperCallback
import com.example.bookchat.SwipeHelperCallback.SwipeViewType
import com.example.bookchat.adapter.CompleteBookTabAdapter
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.FragmentCompleteBookTabBinding
import com.example.bookchat.ui.dialog.CompleteTapBookDialog
import com.example.bookchat.viewmodel.BookShelfViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompleteBookTabFragment : Fragment() {
    lateinit var binding : FragmentCompleteBookTabBinding
    lateinit var completeBookAdapter :CompleteBookTabAdapter
    val bookShelfViewModel: BookShelfViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_complete_book_tab,container,false)
        with(binding){
            lifecycleOwner = this@CompleteBookTabFragment
            viewmodel = bookShelfViewModel
        }
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingCompleteBookData()
        observeAdapterLoadState()

        //다이얼로그 별점 설정 정책 결정하고 설정해야함

        return binding.root
    }

    private fun observePagingCompleteBookData(){
        bookShelfViewModel.completeBookCombined.observe(viewLifecycleOwner){ PagingBookShelfItem ->
            completeBookAdapter.submitData(viewLifecycleOwner.lifecycle, PagingBookShelfItem)
        }
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch{
        completeBookAdapter.loadStateFlow.collect{ combinedLoadStates ->
            if(combinedLoadStates.refresh is LoadState.NotLoading) initializeModificationEvents()
        }
    }
    private fun initializeModificationEvents(){
        bookShelfViewModel.completeBookModificationEvents.value =
            bookShelfViewModel.completeBookModificationEvents.value.filter { pagingViewEvent ->
                pagingViewEvent !is BookShelfViewModel.PagingViewEvent.Remove
            }
        bookShelfViewModel.renewTotalItemCount(BookShelfViewModel.MODIFICATION_EVENT_FLAG_COMPLETE)
    }

    private fun initAdapter(){
        val bookItemClickListener = object: CompleteBookTabAdapter.OnItemClickListener {
            override fun onItemClick(book : BookShelfItem) {
                val dialog = CompleteTapBookDialog(book)
                dialog.show(this@CompleteBookTabFragment.childFragmentManager, DIALOG_TAG_COMPLETE)
            }
        }
        completeBookAdapter = CompleteBookTabAdapter(bookShelfViewModel)
        completeBookAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initRecyclerView(){
        with(binding){
            completeBookRcv.adapter = completeBookAdapter
            completeBookRcv.setHasFixedSize(true)
            completeBookRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutComplete.setOnRefreshListener {
            completeBookAdapter.refresh()
            binding.swipeRefreshLayoutComplete.isRefreshing = false
        }
    }

    companion object{
        private const val DIALOG_TAG_COMPLETE = "CompleteTapBookDialog"
    }

}