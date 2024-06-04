package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork
import com.example.bookchat.data.network.model.response.AgonyResponse
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
	return AgonyFolderHexColorNetwork.valueOf(name)
}

fun AgonyFolderHexColorNetwork.toAgony(): AgonyFolderHexColor {
	return AgonyFolderHexColor.valueOf(name)
}