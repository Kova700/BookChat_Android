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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.WishBookTabAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.FragmentWishBookTabBinding
import com.example.bookchat.ui.dialog.WishTapBookDialog
import com.example.bookchat.viewmodel.BookShelfViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishBookTabFragment : Fragment() {
    private lateinit var binding : FragmentWishBookTabBinding
    private lateinit var wishBookAdapter : WishBookTabAdapter
    private val bookShelfViewModel: BookShelfViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wish_book_tab,container,false)

        with(binding){
            lifecycleOwner = this@WishBookTabFragment
            viewmodel = bookShelfViewModel
        }
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingWishBookData()
        observeAdapterLoadState()

        return binding.root
    }

    private fun observePagingWishBookData() {
        bookShelfViewModel.wishBookCombined.observe(viewLifecycleOwner){ PagingBookShelfItem ->
            wishBookAdapter.submitData(viewLifecycleOwner.lifecycle,PagingBookShelfItem)
        }
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch{
        wishBookAdapter.loadStateFlow.collect{ combinedLoadStates ->
            if(combinedLoadStates.refresh is LoadState.NotLoading) initializeModificationEvents()
        }
    }

    private fun initializeModificationEvents(){
        bookShelfViewModel.wishBookModificationEvents.value = emptyList()
    }

    private fun initAdapter(){
        val bookItemClickListener = object: WishBookTabAdapter.OnItemClickListener{
            override fun onItemClick(bookShelfDataItem : BookShelfDataItem) {
                val dialog = WishTapBookDialog(bookShelfDataItem)
                dialog.show(this@WishBookTabFragment.childFragmentManager,DIALOG_TAG_WISH)
            }
        }
        wishBookAdapter = WishBookTabAdapter()
        wishBookAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initRecyclerView(){
        with(binding){
            wishBookRcv.adapter = wishBookAdapter
            wishBookRcv.setHasFixedSize(true)
            wishBookRcv.layoutManager = GridLayoutManager(requireContext(),3) //중앙정렬 해야함 Or 개수 화면에 따라 늘어나게 설정
        }
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutWish.setOnRefreshListener {
            wishBookAdapter.refresh()
            binding.swipeRefreshLayoutWish.isRefreshing = false
        }
    }

    companion object {
        private const val DIALOG_TAG_WISH = "WishTapBookDialog"
    }

}