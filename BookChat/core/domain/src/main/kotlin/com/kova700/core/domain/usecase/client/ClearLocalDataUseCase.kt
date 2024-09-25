package com.kova700.core.domain.usecase.client

import com.kova700.bookchat.core.data.agony.external.AgonyRepository
import com.kova700.bookchat.core.data.agonyrecord.external.AgonyRecordRepository
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.channel.external.repository.ChannelTempMessageRepository
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.deviceinfo.external.DeviceIDRepository
import com.kova700.bookchat.core.data.oauth.external.OAuthIdTokenRepository
import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.ChannelSearchRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.notification.chat.external.ChatNotificationHandler
import com.kova700.core.data.notificationinfo.external.repository.ChattingNotificationInfoRepository
import com.kova700.core.data.searchhistory.external.SearchHistoryRepository
import javax.inject.Inject

class ClearLocalDataUseCase @Inject constructor(
	private val agonyRecordRepository: AgonyRecordRepository,
	private val agonyRepository: AgonyRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val bookSearchRepository: BookSearchRepository,
	private val bookShelfRepository: BookShelfRepository,
	private val channelRepository: ChannelRepository,
	private val channelSearchRepository: ChannelSearchRepository,
	private val chatRepository: ChatRepository,
	private val chattingNotificationInfoRepository: ChattingNotificationInfoRepository,
	private val clientRepository: ClientRepository,
	private val deviceIDRepository: DeviceIDRepository,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
	private val searchHistoryRepository: SearchHistoryRepository,
	private val userRepository: UserRepository,
	private val channelTempMessageRepository: ChannelTempMessageRepository,
	private val chatNotificationHandler: ChatNotificationHandler,
) {
	suspend operator fun invoke() {
		agonyRecordRepository.clear()
		agonyRepository.clear()
		bookChatTokenRepository.clear()
		bookSearchRepository.clear()
		bookShelfRepository.clear()
		channelRepository.clear()
		channelSearchRepository.clear()
		chatRepository.clear()
		chattingNotificationInfoRepository.clear()
		clientRepository.clear()
		deviceIDRepository.clear()
		oAuthIdTokenRepository.clear()
		searchHistoryRepository.clear()
		userRepository.clear()
		channelTempMessageRepository.clear()
		chatNotificationHandler.dismissAllNotifications()
	}
}