package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.databinding.ActivityChatRoomInfoBinding
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.ui.fragment.ChannelListFragment.Companion.EXTRA_CHAT_ROOM_ID
import com.example.bookchat.ui.fragment.SearchTapResultFragment.Companion.EXTRA_CLICKED_CHAT_ROOM_ITEM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatRoomInfoActivity : AppCompatActivity() {
	@Inject
	lateinit var channelRepository: ChannelRepository
	val database = App.instance.database

	private lateinit var binding: ActivityChatRoomInfoBinding
	private val chatRoomItem: WholeChatRoomListItem by lazy { getExtraChatRoomItem() }

	// TODO : 채팅방 Size API에 추가 예정 (현재 채팅방 참여인원만 있음)
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

	private fun isAlreadyEntered() {
		//DB에 채팅방 저장되어있는지 확인
		//이미 채팅방이 DB에 저장되어있다면 API보내지 않고, 그냥 채팅방 페이지로 이동,
		//저장되어있지 않다면 서버로 API 보냄,
		//서버 응답코드에 따라 분기처리
	}

	// TODO : DB에 채팅방 있는거 보고 있으면 요청 안보내고 바로 채팅방 페이지 이동하는 걸로 수정
	//  + DB에 채팅방이 없더라도, 서버로부터 Status Code 넘겨 받아서, 차단된 사용자인지,
	//  채팅방 인원이 다 차서 못들어가는지, 이미 들어와있는 유저인지 ,
	//  성공적으로 입장했는지, 분기가 필요함
	fun clickEnterBtn() = lifecycleScope.launch {
		runCatching { channelRepository.enter(chatRoomItem.toChannel()) }
			.onSuccess { enterSuccessCallback() }
			.onFailure {
				failHandler(it)
				makeToast(R.string.enter_chat_room_fail)
			}
	}

	//TODO : saveChatRoomInLocalDB 안했을 때 , 화면 어떻게 보이나 확인
	// 일단 채팅방 퇴장 부터 구현
	private suspend fun enterSuccessCallback() {
		saveChatRoomInLocalDB()
		startChatRoomActivity()
	}

	private suspend fun saveChatRoomInLocalDB() {
		database.withTransaction {
			database.channelDAO().upsertChannel(
				chatRoomItem.toChannelEntity()
					.copy(lastChatId = Long.MAX_VALUE) //수정 필요
			)
		}
	}

	private fun startChatRoomActivity() {
		val intent = Intent(this, ChannelActivity::class.java)
		intent.putExtra(EXTRA_CHAT_ROOM_ID, chatRoomItem.roomId)
		startActivity(intent)
	}

	private fun makeToast(stringId: Int) {
		Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
	}

	private fun getExtraChatRoomItem(): WholeChatRoomListItem {
		return intent.getSerializableExtra(EXTRA_CLICKED_CHAT_ROOM_ITEM) as WholeChatRoomListItem
	}

	private fun failHandler(throwable: Throwable) {
		// TODO : 이미 채팅방에 입장한 유저라면 채팅방 페이지로 이동
		//  + 이미 DB에 해당 채팅방 정보가 있을거임 (초기 로그인시에 다 가져오니까)

	}
}