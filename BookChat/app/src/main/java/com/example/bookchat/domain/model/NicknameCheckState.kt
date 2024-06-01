package com.example.bookchat.domain.model

//TODO : 추후 가능하다면 IsDuplicate,IsPerfect를 제외한 나머지는 커스텀 뷰 만들어서 해결
sealed class NicknameCheckState {
	object Default : NicknameCheckState()
	object IsShort : NicknameCheckState()
	object IsSpecialCharInText : NicknameCheckState()
	object IsDuplicate : NicknameCheckState()
	object IsPerfect : NicknameCheckState()
}