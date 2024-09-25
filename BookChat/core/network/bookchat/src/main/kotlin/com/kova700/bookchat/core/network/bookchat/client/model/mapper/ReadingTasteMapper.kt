package com.kova700.bookchat.core.network.bookchat.client.model.mapper

import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.network.bookchat.client.model.both.ReadingTasteNetwork

fun ReadingTaste.toNetWork(): ReadingTasteNetwork {
	return ReadingTasteNetwork.valueOf(name)
}

fun ReadingTasteNetwork.toDomain(): ReadingTaste {
	return ReadingTaste.valueOf(name)
}