package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.SearchSortOptionNetwork
import com.example.bookchat.domain.model.SearchSortOption

fun SearchSortOption.toNetwork(): com.example.bookchat.data.network.model.SearchSortOptionNetwork {
	return when (this) {
		SearchSortOption.ID_DESC -> com.example.bookchat.data.network.model.SearchSortOptionNetwork.ID_DESC
		SearchSortOption.ID_ASC -> com.example.bookchat.data.network.model.SearchSortOptionNetwork.ID_ASC
		SearchSortOption.UPDATED_AT_DESC -> com.example.bookchat.data.network.model.SearchSortOptionNetwork.UPDATED_AT_DESC
		SearchSortOption.UPDATED_AT_ASC -> com.example.bookchat.data.network.model.SearchSortOptionNetwork.UPDATED_AT_ASC
	}
}