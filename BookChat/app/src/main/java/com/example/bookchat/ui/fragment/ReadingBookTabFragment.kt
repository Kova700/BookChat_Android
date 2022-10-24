package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.adapter.ReadingBookTabAdapter
import com.example.bookchat.databinding.FragmentReadingBookTabBinding

class ReadingBookTabFragment :Fragment() {
    private lateinit var binding : FragmentReadingBookTabBinding
    private lateinit var readingBookAdapter :ReadingBookTabAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_reading_book_tab,container,false)
        with(binding){
            lifecycleOwner = this@ReadingBookTabFragment
        }
        initRecyclerView()

        return binding.root
    }

    private fun initRecyclerView(){
        with(binding){
            readingBookAdapter = ReadingBookTabAdapter()
            recyclerView.adapter = readingBookAdapter
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

}