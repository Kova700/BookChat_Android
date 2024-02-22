package com.example.bookchat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.MainBookItemDecoration
import com.example.bookchat.R
import com.example.bookchat.ui.adapter.home.MainBookAdapter
import com.example.bookchat.ui.adapter.home.MainUserChatRoomListAdapter
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.data.database.model.ChatRoomEntity
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.ui.activity.ChatRoomActivity
import com.example.bookchat.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var mainReadingBookAdapter: MainBookAdapter
    private lateinit var mainUserChatRoomListAdapter: MainUserChatRoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        with(binding) {
            lifecycleOwner = this@HomeFragment
            viewmodel = homeViewModel
        }
        initAdapter()
        initRecyclerView()
        observePagingReadingBookData()
        observePagingChatRoomData()
        observeReadingBookLoadStateFlow()

        //독서중인 책 서버로부터 가져오기
        //그리고 로컬 DB에 저장하기,
        //BookShelfFragment에서도 각 페이지 별로 서버로부터 도서 가져오기, (독서예정, 독서중, 독서완료)
        //그리고 각 도서 로컬 DB에 저장하기
        //로컬 DB에서 저장된 도서 가져오기
        //이 작업을 페이징으로 구현( 페이징으로 서버에서 가져옴 -> 로컬 DB에 저장함 -> 처음부터 지금 가져온거까지 로컬DB에서 가져옴 )

        //서재에 있는 도서 값 수정의 경우에는 서버 API 성공시 로컬 DB 수정하는 방향으로 구현
        //서버만 성공하고 LocalDB는 실패하면? (강제종료시에 가능성이 있긴하잖아)
        //어차피 매번 서버로부터 가져오니까 불일치가 생겨도 덮어씌워지니까 상관없을듯

        //채팅방 목록의 경우에도, HomeFragment가 켜지면, 서버로부터 채팅방 목록을 받아와서 로컬 DB에 저장
        //로컬 DB에 저장된 채팅방 목록을 가져와서 List에 뿌림

        //ChatRoomListFragment에서도 서버로부터 채팅방 목록을 가져옴
        //그리고 로컬 DB에 저장함
        //저장된 채팅방 목록을 가져와서 List에 뿌림,

        ///암튼 위에까지는 확실함
        //FCM을 받으면, 로컬 DB에 채팅방 목록에 마지막 채팅 또한 갱신하거나 채팅또한 DB에 저장
        //채팅을 받으면 채팅방 별 마지막 채팅을 채팅방 DB에 저장해야하나, 혹은
        //외래키로 채팅을 채팅 테이블에서 가져오는 형식으로 짜야하나 고민이긴함..
        //이건 stream chat app확인해보자 어떻게 저장하고 어떻게 가져오는지

        return binding.root
    }

    private fun initAdapter() {
        initBookAdapter()
        initChatRoomAdapter()
    }

    private fun initRecyclerView() {
        initBookRcv()
        initChatRoomRcv()
    }

    private fun initBookAdapter() {
        val bookItemClickListener = object : MainBookAdapter.OnItemClickListener {
            override fun onItemClick(bookShelfDataItem: BookShelfDataItem) {
                //도서 누르면 독서중 탭으로 이동
            }
        }
        mainReadingBookAdapter = MainBookAdapter()
        mainReadingBookAdapter.setItemClickListener(bookItemClickListener)
    }

    private fun initChatRoomAdapter() {
        val chatRoomItemClickListener = object : MainUserChatRoomListAdapter.OnItemClickListener {
            override fun onItemClick(chatRoomEntity: ChatRoomEntity) {
                val intent = Intent(requireContext(), ChatRoomActivity::class.java)
                intent.putExtra(ChatRoomListFragment.EXTRA_CHAT_ROOM_LIST_ITEM, chatRoomEntity)
                startActivity(intent)
            }
        }
        mainUserChatRoomListAdapter = MainUserChatRoomListAdapter()
        mainUserChatRoomListAdapter.setItemClickListener(chatRoomItemClickListener)
    }

    private fun initBookRcv() {
        with(binding) {
            readingBookRcvMain.adapter = mainReadingBookAdapter
            readingBookRcvMain.addItemDecoration(MainBookItemDecoration())
            readingBookRcvMain.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
    }

    private fun initChatRoomRcv() {
        with(binding) {
            chatRoomUserInRcv.adapter = mainUserChatRoomListAdapter
            chatRoomUserInRcv.layoutManager =
                LinearLayoutManager(requireContext())
        }
    }

    //도서도 LocalDB에 저장할 수 있게 BookID가 추가되면 좋을 듯함
    private fun observePagingReadingBookData() = lifecycleScope.launch {
        val firstPage = homeViewModel.readingBookResult.first()
        mainReadingBookAdapter.submitData(firstPage)
    }

    private fun observePagingChatRoomData() = lifecycleScope.launch {
        homeViewModel.chatRoomFlow.collect { list ->
            mainUserChatRoomListAdapter.submitList(list)
            val isListEmpty = mainUserChatRoomListAdapter.itemCount == 0
            binding.emptyChatRoomLayout.isVisible = isListEmpty
        }
    }

    //메인에서 독서중 도서, 채팅방 목록 load시에 Paging이 필요하지 않음으로 그냥 ListAdapter로 수정
    //매번 서버로부터 데이터 로드하고, 로컬에서는 캐시데이터를 보여주는 느낌으로
    private fun observeReadingBookLoadStateFlow() = lifecycleScope.launch {
        mainReadingBookAdapter.loadStateFlow.collect { loadState ->
            val isListEmpty = loadState.refresh is LoadState.NotLoading &&
                    mainReadingBookAdapter.itemCount == 0
            binding.emptyReadingBookLayout.isVisible = isListEmpty
        }
    }
}