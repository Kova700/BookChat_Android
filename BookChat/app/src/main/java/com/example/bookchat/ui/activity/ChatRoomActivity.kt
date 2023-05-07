package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        ChatRoomViewModel.provideFactory(
            chatRoomViewModelFactory,
            getChatRoomListItem(),
            getFirstEnterFlag()
        )
    }

    //TODO : 채팅방에 들어왔을때, 채팅스크롤의 위치는 내가 마지막으로 받았던 채팅
    //TODO : 날짜 구분선, 여기까지 읽었습니다 구현해야함
    //TODO : 전송 중 상태로 어플 종료 시 전송실패로 변경된 UI로 보이게 수정해야함
    //TODO : 또한 전송 중 상태에서 다시 인터넷이 연결되면 자동으로 전송이 되어야함
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

    //TODO : 새 채팅 페이지 가져올때 상단에 로딩 UI 표시 하는게 좋을 듯
    private fun initAdapter() {
        chatDataItemAdapter = ChatDataItemAdapter()
            .apply { registerAdapterDataObserver(adapterDataObserver) }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onNewChatInsertedCallBack()
        }
    }

    private fun onNewChatInsertedCallBack() {
        if (chatRoomViewModel.scrollForcedFlag) {
            scrollNewChatItem()
            chatRoomViewModel.scrollForcedFlag = false
            return
        }

        if (chatRoomViewModel.isFirstItemOnScreen) {
            scrollNewChatItem()
        }
    }

    private fun scrollNewChatItem() {
        binding.chattingRcv.scrollToPosition(0)
    }

    private fun initRcv() {
        val linearLayoutManager = LinearLayoutManager(this@ChatRoomActivity)
            .apply { reverseLayout = true }

        binding.chattingRcv.apply {
            adapter = chatDataItemAdapter
            setHasFixedSize(true)
            addItemDecoration(ChatItemDecoration())
            layoutManager = linearLayoutManager
            addOnScrollListener(rcvScrollListener)
        }
    }

    // TODO : 스크롤 위로 올리면 아래로 스크롤 내릴 수 있는 버튼 보이기
    private val rcvScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val isFirstItemOnScreen = isFistItemOnScreen(recyclerView)
            chatRoomViewModel.isFirstItemOnScreen = isFirstItemOnScreen
            if (isFirstItemOnScreen) {
                chatRoomViewModel.newOtherChatNoticeFlow.value = null
            }
        }
    }

    private fun isFistItemOnScreen(recyclerView: RecyclerView): Boolean {
        val lm = recyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition: Int = lm.findFirstVisibleItemPosition()
        val lastVisiblePosition: Int = lm.findLastVisibleItemPosition()
        return 0 in firstVisiblePosition..lastVisiblePosition
    }

    private fun setBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback {
            chatRoomViewModel.finishActivity()
        }
    }

    private fun observeChat() = lifecycleScope.launch {
        chatRoomViewModel.chatDataFlow.collect { pagingData ->
            chatDataItemAdapter.submitData(pagingData)
        }
    }

    private fun observeChatEvent() = lifecycleScope.launch {
        chatRoomViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun handleEvent(event: ChatEvent) {
        when (event) {
            ChatEvent.MoveBack -> finish()
            ChatEvent.CaptureChat -> {}
            ChatEvent.OpenMenu -> {}
            ChatEvent.ScrollNewChatItem -> scrollNewChatItem()
        }
    }

    override fun onPause() {
        //TODO : EditText에 입력해놓은 내용이 있다면 자동저장 기능 추가
        super.onPause()
    }

    private fun getChatRoomListItem(): UserChatRoomListItem {
        return intent.getSerializableExtra(EXTRA_CHAT_ROOM_LIST_ITEM) as UserChatRoomListItem
    }

    private fun getFirstEnterFlag(): Boolean {
        return intent.getBooleanExtra(EXTRA_FIRST_ENTER_FLAG, false)
    }
}