package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork
import com.example.bookchat.data.network.model.response.AgonyResponse
import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.model.AgonyFolderHexColor

fun com.example.bookchat.data.network.model.response.AgonyResponse.toAgony(): Agony {
	return Agony(
		agonyId = agonyId,
		title = title,
		hexColorCode = hexColorCode.toAgony()
	)
}

fun List<com.example.bookchat.data.network.model.response.AgonyResponse>.toAgony() = this.map { it.toAgony() }

fun AgonyFolderHexColor.toNetWork(): com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork {
	return when (this) {
		AgonyFolderHexColor.WHITE -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.WHITE
		AgonyFolderHexColor.BLACK -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.BLACK
		AgonyFolderHexColor.PURPLE -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.PURPLE
		AgonyFolderHexColor.MINT -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.MINT
		AgonyFolderHexColor.GREEN -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.GREEN
		AgonyFolderHexColor.YELLOW -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.YELLOW
		AgonyFolderHexColor.ORANGE -> com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.ORANGE
	}
}

fun com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.toAgony(): AgonyFolderHexColor {
	return when (this) {
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.WHITE -> AgonyFolderHexColor.WHITE
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.BLACK -> AgonyFolderHexColor.BLACK
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.PURPLE -> AgonyFolderHexColor.PURPLE
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.MINT -> AgonyFolderHexColor.MINT
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.GREEN -> AgonyFolderHexColor.GREEN
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.YELLOW -> AgonyFolderHexColor.YELLOW
		com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork.ORANGE -> AgonyFolderHexColor.ORANGE
	}
}