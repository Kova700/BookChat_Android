package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.data.SearchChatRoomListItem
import com.example.bookchat.databinding.ActivityChatRoomInfoBinding
import com.example.bookchat.ui.fragment.SearchTapResultFragment.Companion.EXTRA_CLICKED_CHAT_ROOM_ITEM

class ChatRoomInfoActivity : AppCompatActivity() {
    private lateinit var binding :ActivityChatRoomInfoBinding

    //채팅방 입장 로직 추가해야함
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room_info)
        binding.chatRoomItem = getChatRoomItem()
    }

    private fun getChatRoomItem(): SearchChatRoomListItem {
        return intent.getSerializableExtra(EXTRA_CLICKED_CHAT_ROOM_ITEM) as SearchChatRoomListItem
    }
}