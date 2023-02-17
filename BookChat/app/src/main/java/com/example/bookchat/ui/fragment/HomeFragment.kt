package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding :FragmentHomeBinding
    private lateinit var chatRoomAdapter: MainChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        with(binding){
            lifecycleOwner = this@HomeFragment
            user = App.instance.getCachedUser()
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