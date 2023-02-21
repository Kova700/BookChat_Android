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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.wish_bookshelf.WishBookShelfDataAdapter
import com.example.bookchat.adapter.wish_bookshelf.WishBookShelfHeaderAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.FragmentWishBookshelfBinding
import com.example.bookchat.ui.dialog.WishTapBookDialog
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishBookBookShelfFragment : Fragment() {
    private lateinit var binding : FragmentWishBookshelfBinding
    private lateinit var wishBookShelfHeaderAdapter :WishBookShelfHeaderAdapter
    private lateinit var wishBookShelfDataAdapter : WishBookShelfDataAdapter
    private val bookShelfViewModel: BookShelfViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wish_bookshelf,container,false)

        with(binding){
            lifecycleOwner = this@WishBookBookShelfFragment
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
            wishBookShelfDataAdapter.submitData(viewLifecycleOwner.lifecycle,PagingBookShelfItem)
        }
    }

    private fun observeAdapterLoadState() = lifecycleScope.launch{
        wishBookShelfDataAdapter.loadStateFlow.collect{ combinedLoadStates ->
            if(combinedLoadStates.refresh is LoadState.NotLoading) initializeModificationEvents()
        }
    }

    private fun initializeModificationEvents(){
        bookShelfViewModel.wishBookModificationEvents.value = emptyList()
    }

    private fun initAdapter(){
        val bookItemClickListener = object: WishBookShelfDataAdapter.OnItemClickListener{
            override fun onItemClick(bookShelfDataItem : BookShelfDataItem) {
                val dialog = WishTapBookDialog(bookShelfDataItem)
                dialog.show(this@WishBookBookShelfFragment.childFragmentManager,DIALOG_TAG_WISH)
            }
        }
        wishBookShelfHeaderAdapter = WishBookShelfHeaderAdapter(bookShelfViewModel)
        wishBookShelfDataAdapter = WishBookShelfDataAdapter()
        wishBookShelfDataAdapter.setItemClickListener(bookItemClickListener)
        observeWishBookCount()
    }

    private fun observeWishBookCount() = lifecycleScope.launch{
        bookShelfViewModel.wishBookTotalCount.collect{
            wishBookShelfHeaderAdapter.notifyItemChanged(0)
        }
    }

    private fun initRecyclerView(){
        with(binding){
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig,
                wishBookShelfHeaderAdapter,
                wishBookShelfDataAdapter
            )
            wishBookRcv.adapter = concatAdapter

            val gridLayoutManager = GridLayoutManager(requireContext(),3)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {
                    return when(concatAdapter.getItemViewType(position)){
                        R.layout.item_wish_bookshelf_header -> 3
                        R.layout.item_wish_bookshelf_data -> 1
                        else -> throw Exception("Unknown ViewType")
                    }
                }
            }
            wishBookRcv.layoutManager = gridLayoutManager
        }
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutWish.setOnRefreshListener {
            wishBookShelfDataAdapter.refresh()
            binding.swipeRefreshLayoutWish.isRefreshing = false
        }
    }

    companion object {
        private const val DIALOG_TAG_WISH = "WishTapBookDialog"
    }

}