package com.kova700.bookchat.core.network.bookchat.agony.model.mapper

import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.network.bookchat.agony.model.both.AgonyFolderHexColorNetwork

fun AgonyFolderHexColor.toNetWork(): AgonyFolderHexColorNetwork {
	return AgonyFolderHexColorNetwork.valueOf(name)
}

fun AgonyFolderHexColorNetwork.toAgony(): AgonyFolderHexColor {
	return AgonyFolderHexColor.valueOf(name)
}