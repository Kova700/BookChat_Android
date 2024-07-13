package com.example.bookchat.ui.agony.makeagony.util

import android.graphics.Color
import com.example.bookchat.domain.model.AgonyFolderHexColor

fun AgonyFolderHexColor.getTextColorHexInt(): Int {
	return when (this) {
		AgonyFolderHexColor.WHITE,
		AgonyFolderHexColor.YELLOW,
		AgonyFolderHexColor.ORANGE,
		-> Color.parseColor("#595959")

		AgonyFolderHexColor.BLACK,
		AgonyFolderHexColor.GREEN,
		AgonyFolderHexColor.PURPLE,
		AgonyFolderHexColor.MINT,
		-> Color.parseColor("#FFFFFF")
	}
}