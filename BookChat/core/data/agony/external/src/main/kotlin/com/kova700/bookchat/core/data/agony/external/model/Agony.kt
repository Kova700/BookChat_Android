package com.kova700.bookchat.core.data.agony.external.model

data class Agony(
	val agonyId: Long,
	val title: String,
	val hexColorCode: AgonyFolderHexColor,
) {
	companion object {
		val DEFAULT = Agony(
			agonyId = 0L,
			title = "",
			hexColorCode = AgonyFolderHexColor.WHITE,
		)
	}
}