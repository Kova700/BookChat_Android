package com.example.bookchat.data.mapper

import com.example.bookchat.data.model.ReadingTasteNetwork
import com.example.bookchat.domain.model.ReadingTaste

fun ReadingTaste.toNetWork(): ReadingTasteNetwork {
	return when (this) {
		ReadingTaste.ECONOMY -> ReadingTasteNetwork.ECONOMY
		ReadingTaste.PHILOSOPHY -> ReadingTasteNetwork.PHILOSOPHY
		ReadingTaste.HISTORY -> ReadingTasteNetwork.HISTORY
		ReadingTaste.TRAVEL -> ReadingTasteNetwork.TRAVEL
		ReadingTaste.HEALTH -> ReadingTasteNetwork.HEALTH
		ReadingTaste.HOBBY -> ReadingTasteNetwork.HOBBY
		ReadingTaste.HUMANITIES -> ReadingTasteNetwork.HUMANITIES
		ReadingTaste.NOVEL -> ReadingTasteNetwork.NOVEL
		ReadingTaste.ART -> ReadingTasteNetwork.ART
		ReadingTaste.DESIGN -> ReadingTasteNetwork.DESIGN
		ReadingTaste.DEVELOPMENT -> ReadingTasteNetwork.DEVELOPMENT
		ReadingTaste.SCIENCE -> ReadingTasteNetwork.SCIENCE
		ReadingTaste.MAGAZINE -> ReadingTasteNetwork.MAGAZINE
		ReadingTaste.RELIGION -> ReadingTasteNetwork.RELIGION
		ReadingTaste.CHARACTER -> ReadingTasteNetwork.CHARACTER
	}
}