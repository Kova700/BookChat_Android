package com.example.bookchat.domain.repository

import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.response.ResponseGetAgony
import com.example.bookchat.utils.AgonyFolderHexColor
import com.example.bookchat.utils.SearchSortOption
import retrofit2.Response

interface AgonyRepository {
	suspend fun makeAgony(
		bookShelfId: Long,
		title: String,
		hexColorCode: AgonyFolderHexColor
	)

	suspend fun getAgony(
		bookShelfId: Long,
		size: Int,
		sort: SearchSortOption,
		postCursorId: Long?,
	): ResponseGetAgony

	suspend fun reviseAgony(
		bookShelfId: Long,
		agony: Agony,
		newTitle: String
	)

	suspend fun deleteAgony(
		bookShelfId: Long,
		agonyDataList: List<AgonyDataItem>
	)

}