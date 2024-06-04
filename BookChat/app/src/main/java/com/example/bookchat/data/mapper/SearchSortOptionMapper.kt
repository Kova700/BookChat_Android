package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.SearchSortOptionNetwork
import com.example.bookchat.domain.model.SearchSortOption

fun SearchSortOption.toNetwork(): SearchSortOptionNetwork {
	return SearchSortOptionNetwork.valueOf(name)
}

fun SearchSortOptionNetwork.toDomain(): SearchSortOption {
	return SearchSortOption.valueOf(name)
}