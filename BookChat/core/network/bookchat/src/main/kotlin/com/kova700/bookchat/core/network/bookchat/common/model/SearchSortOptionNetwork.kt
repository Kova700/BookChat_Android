package com.kova700.bookchat.core.network.bookchat.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SearchSortOptionNetwork {
	@SerialName("id,DESC")
	ID_DESC,            //ID기준 내림차순 (=최신순)

	@SerialName("id,ASC")
	ID_ASC,             //ID기준 오른차순 (=등록순)

	@SerialName("updatedAt,DESC")
	UPDATED_AT_DESC,     //변경일기준 내림차순 (=최근 변경순)

	@SerialName("updatedAt,ASC")
	UPDATED_AT_ASC,      //변경일기준 오름차순 (=오래된 변경순)
}