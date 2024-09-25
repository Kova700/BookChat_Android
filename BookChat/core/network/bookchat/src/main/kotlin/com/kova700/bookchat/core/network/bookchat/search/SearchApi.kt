package com.kova700.bookchat.core.network.bookchat.search

import com.kova700.bookchat.core.network.bookchat.search.model.book.both.BookSearchSortOptionNetWork
import com.kova700.bookchat.core.network.bookchat.search.model.book.response.ResponseGetBookSearch
import com.kova700.bookchat.core.network.bookchat.search.model.channel.response.ResponseGetChannelSearch
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

	@GET("/v1/api/chatrooms")
	suspend fun getSearchedChannels(
		@Query("postCursorId") postCursorId: Long?,
		@Query("size") size: Int,
		@Query("roomName") roomName: String?,
		@Query("title") title: String?,
		@Query("isbn") isbn: String?,
		@Query("tags") tags: String?,
	): ResponseGetChannelSearch

	@GET("/v1/api/books")
	suspend fun getBookSearchResult(
		@Query("query") query: String,
		@Query("size") size: Int,
		@Query("page") page: Int,
		@Query("sort") sort: BookSearchSortOptionNetWork = BookSearchSortOptionNetWork.ACCURACY,
	): ResponseGetBookSearch

}