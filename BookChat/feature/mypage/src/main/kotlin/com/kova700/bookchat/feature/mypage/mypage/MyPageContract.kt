package com.kova700.bookchat.feature.mypage.mypage

sealed class MyPageEvent {
	data object MoveToUserEditPage : MyPageEvent()
	data object MoveToSetting : MyPageEvent()
	data object MoveToNotice : MyPageEvent()
	data object MoveToOpenSourceLicense : MyPageEvent()
	data object MoveToTerms : MyPageEvent()
	data object MoveToPrivacyPolicy : MyPageEvent()
}