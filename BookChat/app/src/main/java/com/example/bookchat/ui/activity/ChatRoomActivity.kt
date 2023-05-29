package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.bookchat.App
import com.example.bookchat.ChatItemDecoration
import com.example.bookchat.R
import com.example.bookchat.adapter.chatting.ChatDataItemAdapter
import com.example.bookchat.adapter.chatting.chatdrawer.ChatRoomDrawerDataAdapter
import com.example.bookchat.adapter.chatting.chatdrawer.ChatRoomDrawerHeaderAdapter
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.data.local.entity.UserEntity
import com.example.bookchat.databinding.ActivityChatRoomBinding
import com.example.bookchat.ui.fragment.ChatRoomListFragment.Companion.EXTRA_CHAT_ROOM_LIST_ITEM
import com.example.bookchat.viewmodel.ChatRoomViewModel
import com.example.bookchat.viewmodel.ChatRoomViewModel.ChatEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatRoomActivity : AppCompatActivity() {

    //TODO : 장문의 긴 채팅 길이 접기 구현해야함, 누르면 전체보기 가능하게
    // 채팅 꾹 누르면 복사 가능하게도 구현

    //TODO : 뒤로가기 누를 때, 혹시 채팅방 메뉴 켜져 있으면 닫고, 화면 안꺼지게 수정

    @Inject
    lateinit var chatRoomViewModelFactory: ChatRoomViewModel.AssistedFactory
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatDataItemAdapter: ChatDataItemAdapter
    private lateinit var chatRoomDrawerHeaderAdapter: ChatRoomDrawerHeaderAdapter
    private lateinit var chatRoomDrawerDataAdapter: ChatRoomDrawerDataAdapter
    private val chatRoomViewModel: ChatRoomViewModel by viewModels {
        ChatRoomViewModel.provideFactory(chatRoomViewModelFactory, getChatRoomEntity())
    }

    //TODO : 채팅방에 들어왔을때, 채팅스크롤의 위치는 내가 마지막으로 받았던 채팅 (여기까지 읽었습니다) [보류]
    //   ==> 아마 마지막으로 읽었던 채팅까지 페이징해서 데이터를 가져오기 전까지 RecyclerView는 Invisible 상태로 두고,
    //   해당 위치까지 채팅을 다 가져왔다면 그 위치를 제일 상단에 보이게 스크롤을 이동하고,
    //   RecyclerView를 Visible상태로 변경하는 걸로 보임 (결론은 데이터는 DESC로 가져오지만, 채팅을 보이는 형식은 ASC형식)

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
        observeChatRoomInfoFlow()
        observeChatRoomUserListFlow()
        observeChatRoomUserMapFlow()
    }

    private fun initAdapter() {
        chatDataItemAdapter =
            ChatDataItemAdapter(chatRoomViewModel.chatRoomUserMapFlow.value)
                .apply { registerAdapterDataObserver(adapterDataObserver) }
        chatRoomDrawerHeaderAdapter =
            ChatRoomDrawerHeaderAdapter(chatRoomViewModel.initChatRoomEntity)
        chatRoomDrawerDataAdapter = ChatRoomDrawerDataAdapter()
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
        val linearLayoutManager =
            LinearLayoutManager(this@ChatRoomActivity).apply { reverseLayout = true }

        binding.chattingRcv.apply {
            adapter = chatDataItemAdapter
            setHasFixedSize(true)
            addItemDecoration(ChatItemDecoration())
            layoutManager = linearLayoutManager
            addOnScrollListener(rcvScrollListener)
        }

        binding.chatDrawerLayout.chatroomDrawerRcv.apply {
            val concatAdapterConfig =
                ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(
                concatAdapterConfig, chatRoomDrawerHeaderAdapter, chatRoomDrawerDataAdapter
            )
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(this@ChatRoomActivity)
            setHasFixedSize(true)
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

    private fun observeChatRoomInfoFlow() = lifecycleScope.launch {
        chatRoomViewModel.chatRoomInfoFlow.collect { chatRoomEntity ->
            updateDrawerHeader(chatRoomEntity)
        }
    }

    private fun observeChatRoomUserListFlow() = lifecycleScope.launch {
        chatRoomViewModel.chatRoomUserListFlow.collect { userList ->
            updateDrawerUserList(userList)
        }
    }

    private fun observeChatRoomUserMapFlow() = lifecycleScope.launch {
        chatRoomViewModel.chatRoomUserMapFlow.collect{ userMap ->
            chatDataItemAdapter.userMap = userMap
            chatDataItemAdapter.notifyDataSetChanged()
        }
    }

    private fun updateDrawerHeader(chatRoomEntity: ChatRoomEntity) {
        chatRoomDrawerHeaderAdapter.chatRoomEntity = chatRoomEntity
        chatRoomDrawerHeaderAdapter.notifyItemChanged(0)
    }

    private fun updateDrawerUserList(userList: List<UserEntity>) {
        chatRoomDrawerDataAdapter.submitList(userList)
    }

    private fun observeChatEvent() = lifecycleScope.launch {
        chatRoomViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun handleEvent(event: ChatEvent) {
        when (event) {
            ChatEvent.MoveBack -> finish()
            ChatEvent.CaptureChat -> {}
            ChatEvent.ScrollNewChatItem -> scrollNewChatItem()
            ChatEvent.OpenOrCloseDrawer -> openOrCloseDrawer()
        }
    }

    private fun openOrCloseDrawer() {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
            return
        }
        drawerLayout.openDrawer(GravityCompat.END)
    }

    override fun onStop() {
        saveTempSavedMessage(chatRoomViewModel.inputtedMessage.value)
        super.onStop()
    }

    private fun saveTempSavedMessage(message: String) = lifecycleScope.launch {
        if (message.isBlank()) return@launch

        with(App.instance.database) {
            withTransaction {
                chatRoomDAO().setTempSavedMessage(
                    roomId = chatRoomViewModel.initChatRoomEntity.roomId,
                    message = message
                )
            }
        }
    }

    private fun getChatRoomEntity(): ChatRoomEntity {
        return intent.getSerializableExtra(EXTRA_CHAT_ROOM_LIST_ITEM) as ChatRoomEntity
    }
}