package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding :FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var chatRoomAdapter: MainChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        with(binding){
            lifecycleOwner = this@HomeFragment
            viewModel = homeViewModel
        }
        initChatRoomRcv()

        return binding.root
    }

    private fun initChatRoomRcv(){
        with(binding){
            chatRoomAdapter = MainChatRoomAdapter()
            todayChatRoomListView.adapter = chatRoomAdapter
            todayChatRoomListView.setHasFixedSize(true)
            todayChatRoomListView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

}