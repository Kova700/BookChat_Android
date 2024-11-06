package com.kova700.bookchat.util.user.namecheck

//TODO : [Version 2] 추후 가능하다면 IsDuplicate,IsPerfect를 제외한 나머지는 커스텀 뷰 만들어서 해결
sealed class NicknameCheckState {
	data object Default : NicknameCheckState()
	data object IsShort : NicknameCheckState()
	data object IsSpecialCharInText : NicknameCheckState()
	data object IsDuplicate : NicknameCheckState()
	data object IsPerfect : NicknameCheckState()
}