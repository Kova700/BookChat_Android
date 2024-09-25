package com.kova700.bookchat.core.data.common.model

enum class SearchSortOption {
	ID_DESC,            //ID기준 내림차순 (=최신순)
	ID_ASC,             //ID기준 오른차순 (=등록순)
	UPDATED_AT_DESC,     //변경일기준 내림차순 (=최근 변경순)
	UPDATED_AT_ASC,      //변경일기준 오름차순 (=오래된 변경순)
}
