package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.ReadingTasteNetwork
import com.example.bookchat.domain.model.ReadingTaste

fun ReadingTaste.toNetWork(): ReadingTasteNetwork {
	return ReadingTasteNetwork.valueOf(name)
}

fun ReadingTasteNetwork.toDomain(): ReadingTaste {
	return ReadingTaste.valueOf(name)
}