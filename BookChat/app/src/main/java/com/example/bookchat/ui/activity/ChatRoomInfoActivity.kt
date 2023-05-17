package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.databinding.ActivityChatRoomInfoBinding
import com.example.bookchat.repository.ChatRoomManagementRepository
import com.example.bookchat.ui.fragment.ChatRoomListFragment.Companion.EXTRA_CHAT_ROOM_LIST_ITEM
import com.example.bookchat.ui.fragment.SearchTapResultFragment.Companion.EXTRA_CLICKED_CHAT_ROOM_ITEM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatRoomInfoActivity @Inject constructor(
    private val chatRoomManagementRepository: ChatRoomManagementRepository
) : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomInfoBinding
    private val chatRoomItem = getExtraChatRoomItem()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room_info)
        with(binding) {
            activity = this@ChatRoomInfoActivity
            chatRoomItem = this@ChatRoomInfoActivity.chatRoomItem
        }
    }

    fun clickBackBtn() {
        finish()
    }

    fun clickEnterBtn() = lifecycleScope.launch {
        runCatching { chatRoomManagementRepository.enterChatRoom(chatRoomItem.roomId) }
            .onSuccess { startChatRoomActivity() }
            .onFailure { makeToast(R.string.enter_chat_room_fail) }
    }

    private fun startChatRoomActivity() {
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra(EXTRA_CHAT_ROOM_LIST_ITEM, chatRoomItem.toChatRoomEntity())
        startActivity(intent)
    }

    private fun makeToast(stringId: Int) {
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    private fun getExtraChatRoomItem(): WholeChatRoomListItem {
        return intent.getSerializableExtra(EXTRA_CLICKED_CHAT_ROOM_ITEM) as WholeChatRoomListItem
    }
}