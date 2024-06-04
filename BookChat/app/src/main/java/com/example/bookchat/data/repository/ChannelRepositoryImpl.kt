package com.example.bookchat.data.repository

import android.util.Log
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.mapper.toBookRequest
import com.example.bookchat.data.mapper.toChannel
import com.example.bookchat.data.mapper.toChannelEntity
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.ChannelMemberAuthorityNetwork
import com.example.bookchat.data.network.model.request.RequestChangeChannelSetting
import com.example.bookchat.data.network.model.request.RequestMakeChannel
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.toMultiPartBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

//TODO : 채팅방 정보 조회 API 실패 시 재시도 로직 필요함 (채팅 전송 재시도 로직같은)
class ChannelRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val channelDAO: ChannelDAO,
	private val userRepository: UserRepository,
	private val clientRepository: ClientRepository,
	private val chatRepository: ChatRepository,
) : ChannelRepository {

	private val mapChannels = MutableStateFlow<Map<Long, Channel>>(emptyMap())//(channelId, Channel)
	private val sortedChannels = mapChannels.map { it.values }
		.map { channels ->
			//ORDER BY top_pin_num DESC, last_chat_id IS NULL DESC, last_chat_id DESC, room_id DESC
			channels.sortedWith(
				compareBy<Channel> { channel -> -channel.topPinNum }
					.then(nullsFirst(compareBy { channel -> channel.lastChat?.chatId?.unaryMinus() }))
					.thenBy { channel -> -channel.roomId }
			)
		}

	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getChannelsFlow(): Flow<List<Channel>> {
		return sortedChannels
	}

	override fun getChannelFlow(channelId: Long): Flow<Channel> {
		return mapChannels.map { it[channelId] }.filterNotNull().distinctUntilChanged()
	}

	private fun setChannels(newChannels: Map<Long, Channel>) {
		mapChannels.update { newChannels }
	}

	/** 로컬에 있는 채널 우선적으로 쿼리
	 * (API를 통해 받아온 Channel에는 LastChat을 비롯한 detail정보가 없음) */
	override suspend fun getChannel(channelId: Long): Channel {
		val channel = mapChannels.value[channelId]
			?: getOfflineChannel(channelId)
			?: getOnlineChannel(channelId)

		setChannels(mapChannels.value + (channelId to channel))
		return channel
	}

	private suspend fun getOfflineChannel(channelId: Long): Channel? {
		return channelDAO.getChannel(channelId)?.toChannel(
			getChat = { chatId -> chatRepository.getChat(chatId) },
			getUser = { userId -> userRepository.getUser(userId) })
	}

	/** LastChat을 비롯한 Channel detail 정보가 없음 */
	private suspend fun getOnlineChannel(channelId: Long): Channel {
		return bookChatApi.getChannel(channelId).toChannel()
			.also { channelDAO.upsertChannel(it.toChannelEntity()) }
	}

	override suspend fun getChannelInfo(channelId: Long) {
		Log.d(TAG, "ChannelRepositoryImpl: getChannelInfo() - called")
		val channelInfo = bookChatApi.getChannelInfo(channelId)
		userRepository.upsertAllUsers(channelInfo.participants)

		channelDAO.updateDetailInfo(
			roomId = channelId,
			roomName = channelInfo.roomName,
			roomHostId = channelInfo.roomHost.id,
			participantIds = channelInfo.participantIds,
			participantAuthorities = channelInfo.participantAuthorities,
			bookTitle = channelInfo.bookTitle,
			bookAuthors = channelInfo.bookAuthors,
			bookCoverImageUrl = channelInfo.bookCoverImageUrl,
			roomTags = channelInfo.roomTags,
			roomCapacity = channelInfo.roomCapacity,
			isBanned = channelInfo.isBanned,
			isExploded = channelInfo.isExploded
		)
		setChannels(mapChannels.value + (channelId to getChannelWithUpdatedData(channelId)))
	}

	override suspend fun makeChannel(
		channelTitle: String,
		channelSize: Int,
		defaultRoomImageType: ChannelDefaultImageType,
		channelTags: List<String>,
		selectedBook: Book,
		channelImage: ByteArray?,
	): Channel {
		val response = bookChatApi.makeChannel(
			requestMakeChannel = RequestMakeChannel(
				roomName = channelTitle,
				roomSize = channelSize,
				defaultRoomImageType = defaultRoomImageType.toNetwork(),
				hashTags = channelTags,
				bookRequest = selectedBook.toBookRequest()
			),
			chatRoomImage = channelImage?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = IMAGE_MULTIPART_NAME,
				fileName = IMAGE_FILE_NAME,
				fileExtension = IMAGE_FILE_EXTENSION_WEBP
			)
		)

		val createdChannelId = response.headers()["Location"]?.split("/")?.last()?.toLong()
			?: throw Exception("ChannelId does not exist in Http header.")
		return getChannel(createdChannelId)
	}

	override suspend fun changeChannelSetting(
		channelId: Long,
		channelTitle: String,
		channelCapacity: Int,
		channelTags: List<String>,
		channelImage: ByteArray?,
	) {
		bookChatApi.changeChannelSetting(
			channelId = channelId,
			requestChangeChannelSetting = RequestChangeChannelSetting(
				channelId = channelId,
				channelTitle = channelTitle,
				channelCapacity = channelCapacity,
				channelTags = channelTags
			),
			chatRoomImage = channelImage?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = IMAGE_MULTIPART_NAME,
				fileName = IMAGE_FILE_NAME,
				fileExtension = IMAGE_FILE_EXTENSION_WEBP
			)
		)
		getOnlineChannel(channelId)
	}

	/** DB에 갱신된 LastChat, Participants와 기존에 존재하던
	 * 채널 부가 정보들을 반영해서 반환 (ex : topPin, roomTags, ..) */
	private suspend fun getChannelWithUpdatedData(channelId: Long): Channel {
		val channel = getOfflineChannel(channelId) ?: getOnlineChannel(channelId)
		return channel.copy(
			lastChat = channel.lastChat?.chatId?.let { chatRepository.getChat(it) },
			host = channel.host?.id?.let { userRepository.getUser(it) },
			participants = channel.participantIds?.map { userRepository.getUser(it) },
		)
	}

	//TODO : 방장 나갈 경우 받는 메세지에 넘어오는 메세제지 nullable타입 서버 수정 대기 중
	override suspend fun leaveChannel(channelId: Long) {
		Log.d(TAG, "ChannelRepositoryImpl: leaveChannel() - called")
		val response = bookChatApi.leaveChannel(channelId)
		Log.d(TAG, "ChannelRepositoryImpl: leaveChannel() - response :${response.code()}")
		channelDAO.delete(channelId)
		//현재 방장이 채팅방을 나가면

		//여기서 지우고 다시 서버로부터 로드 되고 있음 지금 (안띄워야지)
		//방장이 채널 나가기를 하면 해당 이벤트를 받지 못한 유저를 위해 채널을 soft 삭제를 하기로 했음,
		//하지만 방장만 있는 인원수가 1명인 채팅방은 방장이 나가면 더 이상
		//남은 유저가 없으니까 soft 삭제가 아니라 그냥 삭제가 되어야함 (사용자 채팅방 목록 이랑 전체 채팅방 검색에 나오지 않는)
		//(but 서버로부터 load되고 있음)
		//심지어 소켓도 연결되고 채팅도 보내짐 (혹은 채널이 나가지지 않았거나)
		//채팅방 삭제조건을 soft삭제가 아니라
		//터진 상태로 소켓연결 + 채팅 불가능하게 만들고
		//모든 유저가 채팅방을 나가야지 삭제되는 조건으로 바꾸는게 제일 편할듯 ㅇㅇ
		//Ban은 그냥 유저를 채팅방 목록에서 지우고 쿼리시에 안넘겨 주면 되는데
		//채팅방 폭발은 쿼리시에 넘겨줌 (그 사람이 채팅방을 나갈 때 까지) but 클라이언트 단에서 상하 페이징, 소켓 연결 막음
		//소켓 연결 여부는 서버에서도 막는게 좋을 듯
		chatRepository.deleteChannelAllChat(channelId)
		setChannels(mapChannels.value - channelId)
	}

	override suspend fun leaveChannelMember(channelId: Long, targetUserId: Long) {
		val channel = getChannel(channelId)
		val newParticipants = channel.participants?.filter { it.id != targetUserId }
		val newParticipantAuthorities = channel.participantAuthorities?.minus(targetUserId)

		val newChannel = channel.copy(
			participants = newParticipants,
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		channelDAO.updateChannelMember(
			channelId = channelId,
			participantIds = newParticipants?.map { it.id },
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun leaveChannelHost(channelId: Long) {
		val channel = getChannel(channelId)
		val newChannel = channel.copy(isExploded = true)
		channelDAO.explosion(channelId)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	// TODO : 이미 입장되어있는 채널에 입장 API 호출하면 넘어오는 응답코드 따로 정의 후,
	//  해당 코드 응답시, 예외 던지기 (isEntered반영하면 수정가능)
	override suspend fun enterChannel(channel: Channel) {
		val resultCode = bookChatApi.enterChannel(channel.roomId).code()
		Log.d(TAG, "ChannelRepositoryImpl: enter() - resultCode : $resultCode")
		channelDAO.upsertChannel(channel.toChannelEntity())
		setChannels(mapChannels.value + mapOf(channel.roomId to channel))
	}

	override suspend fun enterChannelMember(channelId: Long, targetUserId: Long) {
		val channel = getChannel(channelId)
		val newUser = userRepository.getUser(targetUserId)
		val newParticipants = channel.participants?.plus(newUser)
		val newParticipantAuthorities = channel.participantAuthorities
			?.plus(targetUserId to ChannelMemberAuthority.GUEST)

		val newChannel = channel.copy(
			participants = newParticipants,
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		channelDAO.updateChannelMember(
			channelId = channelId,
			participantIds = newParticipants?.map { it.id },
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun banChannelMember(
		channelId: Long,
		targetUserId: Long,
		needServer: Boolean,
	) {
		val channel = getChannel(channelId)
		val clientId = clientRepository.getClientProfile().id

		if (needServer) {
			bookChatApi.banChannelMember(
				channelId = channel.roomId,
				userId = targetUserId,
			)
		}

		val newParticipants = channel.participants?.filter { it.id != targetUserId }
		val newParticipantAuthorities = channel.participantAuthorities?.minus(targetUserId)
		val newChannel = channel.copy(
			participants = newParticipants,
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities,
			isBanned = clientId == targetUserId
		)
		channelDAO.banChannelMember(
			channelId = channelId,
			participantIds = newParticipants?.map { it.id },
			roomMemberCount = newParticipants?.size ?: 0,
			participantAuthorities = newParticipantAuthorities,
			isBanned = clientId == targetUserId
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun updateChannelMemberAuthority(
		channelId: Long,
		targetUserId: Long,
		channelMemberAuthority: ChannelMemberAuthority,
		needServer: Boolean,
	) {

		if (needServer) {
			bookChatApi.updateChannelMemberAuthority(
				channelId = channelId,
				targetUserId = targetUserId,
				authority = channelMemberAuthority.toNetwork()
			)
		}

		val channel = getChannel(channelId)
		val newParticipantAuthorities = channel.participantAuthorities
			?.plus(targetUserId to channelMemberAuthority)
		val newChannel = channel.copy(
			participantAuthorities = newParticipantAuthorities
		)

		channelDAO.updateChannelMemberAuthorities(
			channelId = channelId,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun updateChannelHost(
		channelId: Long,
		targetUserId: Long,
		needServer: Boolean,
	) {
		if (needServer) {
			bookChatApi.updateChannelMemberAuthority(
				channelId = channelId,
				targetUserId = targetUserId,
				authority = ChannelMemberAuthorityNetwork.HOST
			)
		}

		val channel = getChannel(channelId)
		val previousHostId = channel.host?.id ?: throw Exception("hostId does not exist")
		val newParticipantAuthorities = channel.participantAuthorities
			?.plus(previousHostId to ChannelMemberAuthority.GUEST)
			?.plus(targetUserId to ChannelMemberAuthority.HOST)

		val newChannel = channel.copy(
			host = userRepository.getUser(targetUserId),
			participantAuthorities = newParticipantAuthorities
		)

		channelDAO.updateChannelHost(
			channelId = channelId,
			targetUserId = targetUserId,
			participantAuthorities = newParticipantAuthorities
		)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	override suspend fun updateLastReadChatIdIfValid(channelId: Long, chatId: Long) {
		val channel = channelDAO.getChannel(channelId) //왜 DB부터 볼까 인메모리 데이터 안봐?
		val existingLastReadChatId = channel?.lastReadChatId
		if (existingLastReadChatId != null && existingLastReadChatId >= chatId) return

		channelDAO.updateLastReadChat(
			channelId = channelId,
			lastReadChatId = chatId
		)
		val newChannel = getChannelWithUpdatedData(channelId)
		setChannels(mapChannels.value + mapOf(channelId to newChannel))
	}

	//TODO : 서버측에서 lastChatID가 가장 높은 순으로 쿼리되게 혹은 쿼리 옵션을 주게 수정 대기
	//TODO : 해당 함수 인터넷 끊겨있다 연결 trigger발생 시 page :0 부터 재호출
	//TODO : 오프라인부터 가장 첫페이지 뿌려주고 리모트 데이터로 덮어쓰기하는 방향으로 해야함 (리모트 실패하면 오프라인 가져오는게 아니라)
	/** 지수백오프 getChannels 요청 */
	/** 서버에 있는 채널 우선적으로 쿼리 */
	/** Channel 세부 정보는 채팅방 들어 가면 getChannelInfo에 의해 갱신될 예정 */
	override suspend fun getChannels(
		loadSize: Int,
		maxAttempts: Int,
	) {
		if (isEndPage) return

		for (attempt in 0 until maxAttempts) {
			Log.d(TAG, "ChannelRepositoryImpl: getChannels() - attempt : $attempt")

			runCatching {
				bookChatApi.getChannels(
					postCursorId = currentPage,
					size = loadSize
				)
			}.onSuccess { response ->
				channelDAO.upsertAllChannels(response.channels.toChannelEntity())
				val chats = response.channels
					.mapNotNull {
						it.getLastChat(
							clientId = clientRepository.getClientProfile().id
						)
					}
				isEndPage = response.cursorMeta.last
				currentPage = response.cursorMeta.nextCursorId
				chatRepository.insertAllChats(chats)
				val newChannels = response.channels.map { getChannelWithUpdatedData(it.roomId) }
				setChannels(mapChannels.value + newChannels.associateBy { it.roomId })
				return
			}
			delay((DEFAULT_RETRY_ATTEMPT_DELAY_TIME * (1.5).pow(attempt)))
		}

		/**조회 실패시 오프라인 모드로 전환*/
		setChannels(
			mapChannels.value + getOfflineChannels(
				loadSize,
				currentPage
			).associateBy { it.roomId })
	}

	private suspend fun getOfflineChannels(
		loadSize: Int,
		baseId: Long?,
	): List<Channel> {
		return channelDAO.getChannels(loadSize, baseId ?: 0)
			.map {
				it.toChannel(
					getChat = { chatId -> chatRepository.getChat(chatId) },
					getUser = { userId -> userRepository.getUser(userId) }
				)
			}
	}

	override suspend fun updateChannelLastChatIfValid(channelId: Long, chatId: Long) {
		val existingLastChatId = channelDAO.getChannel(channelId)?.lastChatId //왜 DB부터 볼까 인메모리 데이터 안봐?
		if (existingLastChatId != null && chatId <= existingLastChatId) return

		channelDAO.updateLastChat(
			channelId = channelId,
			lastChatId = chatId,
		)

		val updatedChannel = getChannelWithUpdatedData(channelId)
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	//TODO : 데이터를 load해놓지 않았다면 크게 유의미한 함수는 아님
	// (API로 수정하거나 Channel객체를 받을 때 참가유무를 가지고 있는상태로 수정되어야함)
	override suspend fun isChannelAlreadyEntered(channelId: Long): Boolean {
		return channelDAO.isExist(channelId)
	}

	private fun clearCachedData() {
		mapChannels.update { emptyMap() }
		currentPage = null
		isEndPage = false
	}

	/** 로그아웃 + 회원탈퇴시에 모든 repository 일괄 호출 */
	override suspend fun clear() {
		clearCachedData()
		channelDAO.deleteAll()
	}

	companion object {
		private const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		private const val IMAGE_FILE_NAME = "profile_img"
		private const val IMAGE_FILE_EXTENSION_WEBP = ".webp"
		private const val IMAGE_MULTIPART_NAME = "chatRoomImage"
		private val DEFAULT_RETRY_ATTEMPT_DELAY_TIME = 1.seconds
	}

}