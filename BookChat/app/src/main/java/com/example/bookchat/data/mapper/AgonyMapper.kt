package com.example.bookchat.data.mapper

import com.example.bookchat.data.model.AgonyFolderHexColorNetwork
import com.example.bookchat.data.response.AgonyResponse
import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.model.AgonyFolderHexColor

fun AgonyResponse.toAgony(): Agony {
	return Agony(
		agonyId = agonyId,
		title = title,
		hexColorCode = hexColorCode.toAgony()
	)
}

fun List<AgonyResponse>.toAgony() = this.map { it.toAgony() }

fun AgonyFolderHexColor.toNetWork(): AgonyFolderHexColorNetwork {
	return when (this) {
		AgonyFolderHexColor.WHITE -> AgonyFolderHexColorNetwork.WHITE
		AgonyFolderHexColor.BLACK -> AgonyFolderHexColorNetwork.BLACK
		AgonyFolderHexColor.PURPLE -> AgonyFolderHexColorNetwork.PURPLE
		AgonyFolderHexColor.MINT -> AgonyFolderHexColorNetwork.MINT
		AgonyFolderHexColor.GREEN -> AgonyFolderHexColorNetwork.GREEN
		AgonyFolderHexColor.YELLOW -> AgonyFolderHexColorNetwork.YELLOW
		AgonyFolderHexColor.ORANGE -> AgonyFolderHexColorNetwork.ORANGE
	}
}

fun AgonyFolderHexColorNetwork.toAgony(): AgonyFolderHexColor {
	return when (this) {
		AgonyFolderHexColorNetwork.WHITE -> AgonyFolderHexColor.WHITE
		AgonyFolderHexColorNetwork.BLACK -> AgonyFolderHexColor.BLACK
		AgonyFolderHexColorNetwork.PURPLE -> AgonyFolderHexColor.PURPLE
		AgonyFolderHexColorNetwork.MINT -> AgonyFolderHexColor.MINT
		AgonyFolderHexColorNetwork.GREEN -> AgonyFolderHexColor.GREEN
		AgonyFolderHexColorNetwork.YELLOW -> AgonyFolderHexColor.YELLOW
		AgonyFolderHexColorNetwork.ORANGE -> AgonyFolderHexColor.ORANGE
	}
}