package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.ReadingBookTabAdapter
import com.example.bookchat.adapter.WishBookTabAdapter
import com.example.bookchat.databinding.FragmentWishBookTabBinding

class WishBookTabFragment : Fragment() {
    private lateinit var binding : FragmentWishBookTabBinding
    private lateinit var wishBookAdapter : WishBookTabAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wish_book_tab,container,false)
        with(binding){
            lifecycleOwner = this@WishBookTabFragment
        }
        initRecyclerView()
        return binding.root
    }

    private fun initRecyclerView(){
        with(binding){
            wishBookAdapter = WishBookTabAdapter()
            recyclerView.adapter = wishBookAdapter
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = GridLayoutManager(requireContext(),3) //중앙정렬 해야함 Or 개수 화면에 따라 늘어나게 설정
        }
    }

}