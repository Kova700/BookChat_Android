package com.example.bookchat.data.mapper

import com.example.bookchat.data.model.SearchSortOptionNetwork
import com.example.bookchat.domain.model.SearchSortOption

fun SearchSortOption.toNetwork(): SearchSortOptionNetwork {
	return when (this) {
		SearchSortOption.ID_DESC -> SearchSortOptionNetwork.ID_DESC
		SearchSortOption.ID_ASC -> SearchSortOptionNetwork.ID_ASC
		SearchSortOption.UPDATED_AT_DESC -> SearchSortOptionNetwork.UPDATED_AT_DESC
		SearchSortOption.UPDATED_AT_ASC -> SearchSortOptionNetwork.UPDATED_AT_ASC
	}
}