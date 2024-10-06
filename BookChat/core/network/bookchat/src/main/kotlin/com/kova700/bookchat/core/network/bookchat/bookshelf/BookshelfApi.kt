package com.kova700.bookchat.core.network.bookchat.bookshelf

import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.both.BookShelfStateNetwork
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.request.RequestChangeBookStatus
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.request.RequestRegisterBookShelfBook
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.response.BookShelfItemResponse
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.response.BookStateInBookShelfResponse
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.response.ResponseGetBookShelfBooks
import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookshelfApi {

	@POST("/v1/api/bookshelves")
	suspend fun registerBookShelfBook(
		@Body requestRegisterBookShelfBook: RequestRegisterBookShelfBook,
	): BookChatApiResult<Unit>

	@GET("/v1/api/bookshelves")
	suspend fun getBookShelfItems(
		@Query("size") size: Int,
		@Query("page") page: Long,
		@Query("readingStatus") bookShelfState: BookShelfStateNetwork,
		@Query("sort") sort: SearchSortOptionNetwork = SearchSortOptionNetwork.UPDATED_AT_DESC,
	): ResponseGetBookShelfBooks

	@GET("/v1/api/bookshelves/{bookShelfId}")
	suspend fun getBookShelfItem(
		@Path("bookShelfId") bookShelfId: Long,
	): BookShelfItemResponse

	@DELETE("/v1/api/bookshelves/{bookId}")
	suspend fun deleteBookShelfBook(
		@Path("bookId") bookId: Long,
	)

	@PUT("/v1/api/bookshelves/{bookId}")
	suspend fun changeBookShelfBookStatus(
		@Path("bookId") bookId: Long,
		@Body requestChangeBookStatus: RequestChangeBookStatus,
	)

	@GET("/v1/api/bookshelves/book")
	suspend fun checkAlreadyInBookShelf(
		@Query("isbn") isbn: String,
		@Query("publishAt") publishAt: String,
	): BookChatApiResult<BookStateInBookShelfResponse>

}