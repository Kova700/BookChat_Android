package com.kova700.bookchat.core.network.bookchat.common.mapper

import com.kova700.bookchat.core.data.util.model.SearchSortOption
import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork

fun SearchSortOption.toNetwork(): SearchSortOptionNetwork {
	return SearchSortOptionNetwork.valueOf(name)
}

fun SearchSortOptionNetwork.toDomain(): SearchSortOption {
	return SearchSortOption.valueOf(name)
}