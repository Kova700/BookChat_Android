package com.example.bookchat.ui.search.mapper

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.search.model.SearchResultItem

fun Book.toBookItem(): SearchResultItem.BookItem {
	return SearchResultItem.BookItem(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun SearchResultItem.BookItem.toBook(): Book {
	return Book(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun Channel.toChannelItem(): SearchResultItem.ChannelItem {
	return SearchResultItem.ChannelItem(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		host = host,
		participants = participants,
		participantAuthorities = participantAuthorities,
		tagsString = tagsString,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}