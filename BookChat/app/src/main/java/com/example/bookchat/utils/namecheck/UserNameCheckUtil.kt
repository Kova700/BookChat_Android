package com.example.bookchat.utils.namecheck

import android.content.Context
import android.graphics.Color
import com.example.bookchat.R
import com.example.bookchat.domain.model.NicknameCheckState

fun NicknameCheckState.getNameCheckResultText(context: Context): String {
	return when (this) {
		NicknameCheckState.Default -> ""

		NicknameCheckState.IsShort ->
			context.resources.getString(R.string.name_check_status_short)

		NicknameCheckState.IsDuplicate ->
			context.resources.getString(R.string.name_check_status_duplicate)

		NicknameCheckState.IsSpecialCharInText ->
			context.resources.getString(R.string.name_check_status_special_char)

		NicknameCheckState.IsPerfect ->
			context.resources.getString(R.string.name_check_status_perfect)
	}
}

fun NicknameCheckState.getNameCheckResultHexInt(context: Context): Int {
	return when (this) {
		NicknameCheckState.Default -> Color.TRANSPARENT

		NicknameCheckState.IsShort,
		NicknameCheckState.IsDuplicate,
		NicknameCheckState.IsSpecialCharInText,
		-> Color.parseColor("#FF004D")

		NicknameCheckState.IsPerfect -> Color.parseColor("#5648FF")
	}
}

fun NicknameCheckState.getNameCheckResultBackgroundResId(): Int {
	return when (this) {
		NicknameCheckState.Default -> R.drawable.nickname_input_back_white

		NicknameCheckState.IsShort,
		NicknameCheckState.IsDuplicate,
		NicknameCheckState.IsSpecialCharInText,
		-> R.drawable.nickname_input_back_red

		NicknameCheckState.IsPerfect -> R.drawable.nickname_input_back_blue
	}
}