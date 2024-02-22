package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.request.RequestMakeAgony
import com.example.bookchat.data.request.RequestReviseAgony
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetAgony
import com.example.bookchat.domain.repository.AgonyRepository
import com.example.bookchat.utils.AgonyFolderHexColor
import com.example.bookchat.utils.SearchSortOption
import retrofit2.Response
import javax.inject.Inject

class AgonyRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : AgonyRepository {

	override suspend fun makeAgony(
		bookShelfId: Long,
		title: String,
		hexColorCode: AgonyFolderHexColor
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestMakeAgony = RequestMakeAgony(title, hexColorCode)
		val response = bookChatApi.makeAgony(bookShelfId, requestMakeAgony)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun getAgony(
		bookShelfId: Long,
		size: Int,
		sort: SearchSortOption,
		postCursorId: Long?
	): ResponseGetAgony {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		return bookChatApi.getAgony(
			bookShelfId = bookShelfId,
			size = size,
			sort = sort,
			postCursorId = postCursorId
		)

	}

	//고민폴더 색상 변경 UI 추가되면 색상도 변경 가능하게 수정
	override suspend fun reviseAgony(
		bookShelfId: Long,
		agony: Agony,
		newTitle: String
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestReviseAgony = RequestReviseAgony(newTitle, agony.hexColorCode)
		val response =
			bookChatApi.reviseAgony(bookShelfId, agony.agonyId, requestReviseAgony)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun deleteAgony(
		bookShelfId: Long,
		agonyDataList: List<AgonyDataItem>
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val deleteIdListString = agonyDataList.map { it.agony.agonyId }.joinToString(",")
		val response = bookChatApi.deleteAgony(bookShelfId, deleteIdListString)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}
}