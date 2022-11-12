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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.WishBookTabAdapter
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.FragmentWishBookTabBinding
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.ViewModelFactory
import com.example.bookchat.viewmodel.WishBookTapViewModel
import kotlinx.coroutines.launch

class WishBookTabFragment : Fragment() {
    private lateinit var binding : FragmentWishBookTabBinding
    private lateinit var wishBookAdapter : WishBookTabAdapter
    private lateinit var wishBookTapViewModel: WishBookTapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wish_book_tab,container,false)
        wishBookTapViewModel = ViewModelProvider(this, ViewModelFactory()).get(WishBookTapViewModel::class.java)
        with(binding){
            lifecycleOwner = this@WishBookTabFragment
            viewmodel = wishBookTapViewModel
        }
        initAdapter()
        initRecyclerView()
        initRefreshEvent()
        observePagingWishBookData()

        return binding.root
    }

    private fun observePagingWishBookData()= lifecycleScope.launch {
        wishBookTapViewModel.wishBookResult.collect{ PagingBookShelfItem ->
            Log.d(TAG, "WishBookTabFragment: observePagingWishBookData() - PagingBookShelfItem :$PagingBookShelfItem")
            wishBookAdapter.submitData(PagingBookShelfItem)
        }
    }

    private fun initAdapter(){
        val bookItemClickListener = object: WishBookTabAdapter.OnItemClickListener{
            override fun onItemClick(book : BookShelfItem) {
                //다이얼로그 출력
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
            wishBookRcv.viewTreeObserver.addOnScrollChangedListener {
                binding.swipeRefreshLayoutWish.isEnabled = (wishBookRcv.scrollY == 0)
            }
        }
    }

    private fun initRefreshEvent(){
        binding.swipeRefreshLayoutWish.setOnRefreshListener {
            wishBookAdapter.refresh()
            binding.swipeRefreshLayoutWish.isRefreshing = false
        }
    }

}