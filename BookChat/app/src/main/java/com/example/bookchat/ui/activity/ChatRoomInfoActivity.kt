package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.databinding.ActivityChatRoomInfoBinding
import com.example.bookchat.ui.fragment.ChatRoomListFragment.Companion.EXTRA_CHAT_ROOM_LIST_ITEM
import com.example.bookchat.ui.fragment.SearchTapResultFragment.Companion.EXTRA_CLICKED_CHAT_ROOM_ITEM

class ChatRoomInfoActivity : AppCompatActivity() {
    private lateinit var binding :ActivityChatRoomInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room_info)
        with(binding){
            activity = this@ChatRoomInfoActivity
            chatRoomItem = getExtraChatRoomItem()
        }
    }

    fun clickBackBtn(){
        finish()
    }

    fun clickEnterBtn(){
        startChatRoomActivity()
    }

    private fun startChatRoomActivity(){
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra(EXTRA_FIRST_ENTER_FLAG, true)
        intent.putExtra(EXTRA_CHAT_ROOM_LIST_ITEM, getExtraChatRoomItem().getUserChatRoomListItem())
        startActivity(intent)
    }

    private fun getExtraChatRoomItem(): WholeChatRoomListItem {
        return intent.getSerializableExtra(EXTRA_CLICKED_CHAT_ROOM_ITEM) as WholeChatRoomListItem
    }

    companion object{
        const val EXTRA_FIRST_ENTER_FLAG = "EXTRA_FIRST_ENTER_FLAG"
    }
}