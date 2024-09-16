package com.kova700.bookchat.core.network.bookchat.agony.model.mapper

import com.kova700.bookchat.core.data.agony.external.model.Agony
import com.kova700.bookchat.core.network.bookchat.agony.model.response.AgonyResponse

fun AgonyResponse.toAgony(): Agony {
	return Agony(
		agonyId = agonyId,
		title = title,
		hexColorCode = hexColorCode.toAgony()
	)
}

fun List<AgonyResponse>.toAgony() = this.map { it.toAgony() }