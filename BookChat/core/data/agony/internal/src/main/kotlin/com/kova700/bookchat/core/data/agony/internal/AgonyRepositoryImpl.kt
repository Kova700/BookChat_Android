package com.kova700.bookchat.core.data.agony.internal

import com.kova700.bookchat.core.data.agony.external.AgonyRepository
import com.kova700.bookchat.core.data.agony.external.model.Agony
import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.data.agony.internal.mapper.toAgony
import com.kova700.bookchat.core.data.agony.internal.mapper.toNetWork
import com.kova700.bookchat.core.data.util.mapper.toNetwork
import com.kova700.bookchat.core.data.util.model.SearchSortOption
import com.kova700.bookchat.core.network.bookchat.BookChatApi
import com.kova700.bookchat.core.network.bookchat.model.request.RequestMakeAgony
import com.kova700.bookchat.core.network.bookchat.model.request.RequestReviseAgony
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AgonyRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : AgonyRepository {

	private val mapAgonies = MutableStateFlow<Map<Long, Agony>>(emptyMap()) //(agonyId, Agony)
	private val agonies = mapAgonies.map {
		it.values.toList().sortedByDescending { agony -> agony.agonyId }
	}

	private var cachedBookShelfItemId: Long = -1
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getAgoniesFlow(initFlag: Boolean): Flow<List<Agony>> {
		if (initFlag) clear()
		return agonies
	}

	override fun getAgonyFlow(agonyId: Long): Flow<Agony> {
		return agonies.map { agonyList -> agonyList.first { it.agonyId == agonyId } }
	}

	private fun setAgonies(newAgonies: Map<Long, Agony>) {
		mapAgonies.update { newAgonies }
	}

	override suspend fun getAgonies(
		bookShelfId: Long,
		sort: SearchSortOption,
		size: Int,
	) {
		if (cachedBookShelfItemId != bookShelfId) clear()
		if (isEndPage) return

		val response = bookChatApi.getAgonies(
			bookShelfId = bookShelfId,
			size = size,
			sort = sort.toNetwork(),
			postCursorId = currentPage
		)
		cachedBookShelfItemId = bookShelfId
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		val newAgonies = response.agonyResponseList.toAgony()
		setAgonies(mapAgonies.value + newAgonies.associateBy { it.agonyId })
	}

	override suspend fun getAgony(
		bookShelfId: Long,
		agonyId: Long,
	): Agony {
		val agony = mapAgonies.value[agonyId]
			?: getOnlineAgony(
				bookShelfId = bookShelfId,
				agonyId = agonyId
			)
		setAgonies(mapAgonies.value + (agony.agonyId to agony))
		return agony
	}

	private suspend fun getOnlineAgony(
		bookShelfId: Long,
		agonyId: Long,
	): Agony {
		return bookChatApi.getAgony(
			bookShelfId = bookShelfId,
			agonyId = agonyId
		).toAgony()
	}

	override suspend fun makeAgony(
		bookShelfId: Long,
		title: String,
		hexColorCode: AgonyFolderHexColor,
	): Agony {
		val requestMakeAgony = RequestMakeAgony(
			title = title,
			hexColorCode = hexColorCode.toNetWork()
		)
		val response = bookChatApi.makeAgony(
			bookId = bookShelfId,
			requestMakeAgony = requestMakeAgony
		)

		val createdAgonyId = response.headers()["Location"]
			?.split("/")?.last()?.toLong()
			?: throw Exception("AgonyId does not exist in Http header.")

		return getAgony(
			bookShelfId = bookShelfId,
			agonyId = createdAgonyId
		)
	}

	//TODO : 색상도 변경 가능하게 UI 추가
	override suspend fun reviseAgony(
		bookShelfId: Long,
		agonyId: Long,
		newTitle: String,
	) {
		val agony = getAgony(
			bookShelfId = bookShelfId,
			agonyId = agonyId
		)
		val requestReviseAgony = RequestReviseAgony(
			title = newTitle,
			hexColorCode = agony.hexColorCode.toNetWork()
		)
		bookChatApi.reviseAgony(bookShelfId, agony.agonyId, requestReviseAgony)
		setAgonies(mapAgonies.value + (agony.agonyId to agony.copy(title = newTitle)))
	}

	override suspend fun deleteAgony(
		bookShelfId: Long,
		agonyIds: List<Long>,
	) {
		val agonyIdsString = agonyIds.joinToString(",")
		bookChatApi.deleteAgony(bookShelfId, agonyIdsString)
		setAgonies(mapAgonies.value - agonyIds.toSet())
	}

	override fun clear() {
		setAgonies(emptyMap())
		cachedBookShelfItemId = -1
		currentPage = null
		isEndPage = false
	}

}