package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.ChatItemDecoration
import com.example.bookchat.R
import com.example.bookchat.adapter.chatting.ChatDataItemAdapter
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.databinding.ActivityChatRoomBinding
import com.example.bookchat.ui.activity.ChatRoomInfoActivity.Companion.EXTRA_FIRST_ENTER_FLAG
import com.example.bookchat.ui.fragment.ChatRoomListFragment.Companion.EXTRA_CHAT_ROOM_LIST_ITEM
import com.example.bookchat.viewmodel.ChatRoomViewModel
import com.example.bookchat.viewmodel.ChatRoomViewModel.ChatEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatRoomActivity : AppCompatActivity() {

    @Inject
    lateinit var chatRoomViewModelFactory: ChatRoomViewModel.AssistedFactory

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatDataItemAdapter: ChatDataItemAdapter
    private val chatRoomViewModel: ChatRoomViewModel by viewModels {
        ChatRoomViewModel.provideFactory(chatRoomViewModelFactory, getChatRoomListItem(), getFirstEnterFlag())
    }

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room)
        with(binding) {
            lifecycleOwner = this@ChatRoomActivity
            viewmodel = chatRoomViewModel
        }
        setBackPressedDispatcher()
        initAdapter()
        initRcv()
        observeChat()
        observeChatEvent()
    }

    private fun initAdapter() {
        chatDataItemAdapter = ChatDataItemAdapter()
    }

    private fun initRcv() {
        with(binding) {
            chattingRcv.adapter = chatDataItemAdapter
            chattingRcv.setHasFixedSize(true)
            chattingRcv.addItemDecoration(ChatItemDecoration())
            chattingRcv.layoutManager = LinearLayoutManager(this@ChatRoomActivity)
        }
    }

    private fun setBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback {
            chatRoomViewModel.finishActivity()
        }
    }

    //PagingSource로 땡겨와서 RemoteConfig로 로컬에 저장하고
    //나는 로컬에서 Room을 통해서 변경되는 데이터들을 가져와야하나
    //임시로 그냥 소켓에서 받는 데이터들 바로 올리고 있음

    private fun observeChat() = lifecycleScope.launch {
        chatRoomViewModel.chatData.collect { chatList ->
            chatDataItemAdapter.submitList(chatList)
            chatDataItemAdapter.notifyDataSetChanged()
        }
    }

    private fun observeChatEvent() = lifecycleScope.launch {
        chatRoomViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun handleEvent(event: ChatEvent) {
        when (event) {
            ChatEvent.MoveBack -> { finish() }
            ChatEvent.CaptureChat -> {}
            ChatEvent.OpenMenu -> {}
        }
    }

    private fun getChatRoomListItem(): UserChatRoomListItem {
        return intent.getSerializableExtra(EXTRA_CHAT_ROOM_LIST_ITEM) as UserChatRoomListItem
    }

    private fun getFirstEnterFlag(): Boolean {
        return intent.getBooleanExtra(EXTRA_FIRST_ENTER_FLAG,false)
    }
}