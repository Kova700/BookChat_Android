package com.kova700.bookchat.core.data.agony.internal.mapper

import com.kova700.bookchat.core.data.agony.external.model.Agony
import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.network.bookchat.model.both.AgonyFolderHexColorNetwork
import com.kova700.bookchat.core.network.bookchat.model.response.AgonyResponse

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