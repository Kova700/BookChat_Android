package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.repository.AgonyRecordRepository
import com.example.bookchat.domain.repository.AgonyRepository
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.domain.repository.ChannelTempMessageRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.kova700.core.data.notificationinfo.external.repository.ChattingNotificationInfoRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.kova700.bookchat.core.data.deviceinfo.external.DeviceIDRepository
import com.example.bookchat.oauth.repository.external.OAuthIdTokenRepository
import com.kova700.core.data.searchhistory.external.SearchHistoryRepository
import com.example.bookchat.domain.repository.UserRepository
import com.example.bookchat.notification.chat.ChatNotificationHandler
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