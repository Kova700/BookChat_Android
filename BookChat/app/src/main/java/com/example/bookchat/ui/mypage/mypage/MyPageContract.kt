package com.example.bookchat.ui.mypage.mypage

sealed class MyPageEvent {
	data object MoveToUserEditPage : MyPageEvent()
	data object MoveToSetting : MyPageEvent()
	data object MoveToNotice : MyPageEvent()
}