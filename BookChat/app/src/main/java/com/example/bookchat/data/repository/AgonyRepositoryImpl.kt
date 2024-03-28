package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.mapper.toAgony
import com.example.bookchat.data.mapper.toNetWork
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.request.RequestMakeAgony
import com.example.bookchat.data.request.RequestReviseAgony
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.SearchSortOption
import com.example.bookchat.domain.repository.AgonyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AgonyRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : AgonyRepository {

	private val mapAgonies = MutableStateFlow<Map<Long, Agony>>(emptyMap()) //(agonyId, Agony)
	private val agonies = mapAgonies.map {
		it.values.toList().sortedByDescending { agony -> agony.agonyId }
	}

	private var cachedBookShelfItemId: Long = -1
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getAgoniesFlow(): Flow<List<Agony>> {
		return agonies
	}

	override suspend fun getAgonies(
		bookShelfId: Long,
		sort: SearchSortOption,
		size: Int,
	) {
		if (cachedBookShelfItemId != bookShelfId) {
			clearCachedData()
		}

		if (isEndPage) return
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.getAgony(
			bookShelfId = bookShelfId,
			size = size,
			sort = sort.toNetwork(),
			postCursorId = currentPage
		)
		cachedBookShelfItemId = bookShelfId
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		val newAgonies = response.agonyResponseList.toAgony()
		mapAgonies.update { mapAgonies.value + newAgonies.associateBy { it.agonyId } }
	}

	private fun clearCachedData() {
		mapAgonies.update { emptyMap() }
		cachedBookShelfItemId = -1
		currentPage = null
		isEndPage = false
	}

	//TODO : 여기도 만들어진 Agony 객체 필요함
	override suspend fun makeAgony(
		bookShelfId: Long,
		title: String,
		hexColorCode: AgonyFolderHexColor
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestMakeAgony = RequestMakeAgony(title, hexColorCode.toNetWork())
		bookChatApi.makeAgony(bookShelfId, requestMakeAgony)
	}

	//TODO : 색상도 변경 가능하게 UI 추가
	override suspend fun reviseAgony(
		bookShelfId: Long,
		agony: Agony,
		newTitle: String
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestReviseAgony = RequestReviseAgony(newTitle, agony.hexColorCode.toNetWork())
		bookChatApi.reviseAgony(bookShelfId, agony.agonyId, requestReviseAgony)
		mapAgonies.update { mapAgonies.value + (agony.agonyId to agony.copy(title = newTitle)) }
	}

	override suspend fun deleteAgony(
		bookShelfId: Long,
		agonyIds: List<Long>
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val agonyIdsString = agonyIds.joinToString(",")
		bookChatApi.deleteAgony(bookShelfId, agonyIdsString)
		mapAgonies.update { mapAgonies.value - agonyIds.toSet() }
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

}