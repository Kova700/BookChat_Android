package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.ReadingTasteNetwork
import com.example.bookchat.domain.model.ReadingTaste

fun ReadingTaste.toNetWork(): com.example.bookchat.data.network.model.ReadingTasteNetwork {
	return when (this) {
		ReadingTaste.ECONOMY -> com.example.bookchat.data.network.model.ReadingTasteNetwork.ECONOMY
		ReadingTaste.PHILOSOPHY -> com.example.bookchat.data.network.model.ReadingTasteNetwork.PHILOSOPHY
		ReadingTaste.HISTORY -> com.example.bookchat.data.network.model.ReadingTasteNetwork.HISTORY
		ReadingTaste.TRAVEL -> com.example.bookchat.data.network.model.ReadingTasteNetwork.TRAVEL
		ReadingTaste.HEALTH -> com.example.bookchat.data.network.model.ReadingTasteNetwork.HEALTH
		ReadingTaste.HOBBY -> com.example.bookchat.data.network.model.ReadingTasteNetwork.HOBBY
		ReadingTaste.HUMANITIES -> com.example.bookchat.data.network.model.ReadingTasteNetwork.HUMANITIES
		ReadingTaste.NOVEL -> com.example.bookchat.data.network.model.ReadingTasteNetwork.NOVEL
		ReadingTaste.ART -> com.example.bookchat.data.network.model.ReadingTasteNetwork.ART
		ReadingTaste.DESIGN -> com.example.bookchat.data.network.model.ReadingTasteNetwork.DESIGN
		ReadingTaste.DEVELOPMENT -> com.example.bookchat.data.network.model.ReadingTasteNetwork.DEVELOPMENT
		ReadingTaste.SCIENCE -> com.example.bookchat.data.network.model.ReadingTasteNetwork.SCIENCE
		ReadingTaste.MAGAZINE -> com.example.bookchat.data.network.model.ReadingTasteNetwork.MAGAZINE
		ReadingTaste.RELIGION -> com.example.bookchat.data.network.model.ReadingTasteNetwork.RELIGION
		ReadingTaste.CHARACTER -> com.example.bookchat.data.network.model.ReadingTasteNetwork.CHARACTER
	}
}