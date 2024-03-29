package com.example.bookchat.ui

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.UserDefaultProfileImageType
import com.example.bookchat.ui.agony.AgonyUiState
import com.example.bookchat.ui.bookreport.BookReportViewModel.BookReportStatus
import com.example.bookchat.ui.login.LoginViewModel.LoginUiState
import com.example.bookchat.utils.*
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.*

object DataBindingAdapter {

	/**텍스트뷰 select 설정*/
	@JvmStatic
	@BindingAdapter("setSelected")
	fun setSelected(view: View, boolean: Boolean) {
		view.isSelected = boolean
	}

	/**이미지뷰 이미지 설정(URL)*/
	@JvmStatic
	@BindingAdapter("loadUrl")
	fun loadUrl(imageView: ImageView, url: String?) {
		if (url == null || url.isEmpty()) return

		Glide.with(imageView.context)
			.load(url)
			.placeholder(R.drawable.loading_img)
			.error(R.drawable.error_img)
			.into(imageView)
	}

	/**이미지뷰 이미지 설정(ByteArray)*/
	@JvmStatic
	@BindingAdapter("loadByteArray")
	fun loadByteArray(imageView: ImageView, byteArray: ByteArray?) {
		if (byteArray == null || byteArray.isEmpty()) return

		val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
		Glide.with(imageView.context)
			.load(bitmap)
			.placeholder(R.drawable.loading_img)
			.error(R.drawable.error_img)
			.into(imageView)
	}

	/**유저 프로필 이미지 출력*/
	@JvmStatic
	@BindingAdapter("userProfileUrl", "userDefaultProfileImageType", requireAll = false)
	fun loadUserProfile(
		imageView: ImageView,
		userProfileUrl: String?,
		userDefaultProfileImageType: UserDefaultProfileImageType?
	) {
		if (!userProfileUrl?.trim().isNullOrBlank()) {
			loadUrl(imageView, userProfileUrl)
			return
		}
		inflateUserDefaultProfileImage(imageView, userDefaultProfileImageType)
	}

	private fun inflateUserDefaultProfileImage(
		imageView: ImageView,
		imageType: UserDefaultProfileImageType?
	) {
		Glide.with(imageView.context)
			.load(getUserDefaultProfileImage(imageType))
			.fitCenter()
			.placeholder(R.drawable.loading_img)
			.error(R.drawable.error_img)
			.into(imageView)
	}

	private fun getUserDefaultProfileImage(imageType: UserDefaultProfileImageType?) =
		when (imageType) {
			null,
			UserDefaultProfileImageType.ONE -> R.drawable.default_profile_img1

			UserDefaultProfileImageType.TWO -> R.drawable.default_profile_img2
			UserDefaultProfileImageType.THREE -> R.drawable.default_profile_img3
			UserDefaultProfileImageType.FOUR -> R.drawable.default_profile_img4
			UserDefaultProfileImageType.FIVE -> R.drawable.default_profile_img5
		}

	@JvmStatic
	@BindingAdapter("setUserNickname")
	fun setUserNickname(textview: TextView, nickname: String?) {
		if (nickname.isNullOrBlank()) {
			textview.text = "(알 수 없음)"
			return
		}
		textview.text = nickname
	}

	/** EditText 엔터이벤트 등록*/
	@JvmStatic
	@BindingAdapter("setEnterListener")
	fun setEnterListener(editText: EditText, listener: TextView.OnEditorActionListener) {
		editText.setOnEditorActionListener(listener)
	}

	/**독서취향 : 제출 버튼 색상 설정*/
	@JvmStatic
	@BindingAdapter("setButtonColor")
	fun setButtonColor(button: Button, booleanFlag: Boolean) {
		if (booleanFlag) {
			button.setBackgroundColor(Color.parseColor("#D9D9D9"))
			button.isEnabled = false
			return
		}
		button.setBackgroundColor(Color.parseColor("#5648FF"))
		button.isEnabled = true
	}

	/**회원가입 :닉네임 검사 상황별 안내문구*/
	//텍스트 (색 , 글자)
	@JvmStatic
	@BindingAdapter("setTextViewFromCheckResult")
	fun setTextViewFromCheckResult(textView: TextView, nameCheckStatus: NameCheckStatus) {
		when (nameCheckStatus) {
			NameCheckStatus.Default -> {
				textView.text = ""
			}

			NameCheckStatus.IsShort -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = App.instance.resources.getString(R.string.name_check_status_short)
			}

			NameCheckStatus.IsDuplicate -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = App.instance.resources.getString(R.string.name_check_status_duplicate)
			}

			NameCheckStatus.IsSpecialCharInText -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = App.instance.resources.getString(R.string.name_check_status_special_char)
			}

			NameCheckStatus.IsPerfect -> {
				textView.setTextColor(Color.parseColor("#5648FF"))
				textView.text = App.instance.resources.getString(R.string.name_check_status_perfect)
			}
		}
	}

	/**회원가입 :닉네임 검사 상황별 테두리 색상*/
	//레이아웃 (테두리)
	@JvmStatic
	@BindingAdapter("setLayoutFromCheckResult")
	fun setLayoutFromCheckResult(view: View, nameCheckStatus: NameCheckStatus) {
		when (nameCheckStatus) {
			NameCheckStatus.Default -> {
				view.background = ResourcesCompat.getDrawable(
					App.instance.resources,
					R.drawable.nickname_input_back_white,
					null
				)
			}

			NameCheckStatus.IsShort,
			NameCheckStatus.IsDuplicate,
			NameCheckStatus.IsSpecialCharInText -> {
				view.background = ResourcesCompat.getDrawable(
					App.instance.resources,
					R.drawable.nickname_input_back_red,
					null
				)
			}

			NameCheckStatus.IsPerfect -> {
				view.background = ResourcesCompat.getDrawable(
					App.instance.resources,
					R.drawable.nickname_input_back_blue,
					null
				)
			}
		}
	}

	@JvmStatic
	@BindingAdapter("setLoadingVisibilityInBookReport")
	fun setLoadingVisibilityInBookReport(view: View, status: BookReportStatus) {
		view.visibility = if (status == BookReportStatus.Loading) View.VISIBLE else View.INVISIBLE
	}

	@JvmStatic
	@BindingAdapter("setInputLayoutVisibilityInBookReport")
	fun setInputLayoutVisibilityInBookReport(view: View, status: BookReportStatus) {
		when (status) {
			BookReportStatus.InputData,
			BookReportStatus.ReviseData -> {
				view.visibility = View.VISIBLE
			}

			else -> {
				view.visibility = View.INVISIBLE
			}
		}
	}

	@JvmStatic
	@BindingAdapter("setShowDataLayoutVisibilityInBookReport")
	fun setShowDataLayoutVisibilityInBookReport(view: View, status: BookReportStatus) {
		view.visibility = if (status == BookReportStatus.ShowData) View.VISIBLE else View.INVISIBLE
	}

	/**고민 폴더 색상 설정(HexColor)*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderBackgroundTint")
	fun setAgonyFolderBackgroundTint(
		view: View,
		agonyFolderHexColor: AgonyFolderHexColor
	) {
		view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(agonyFolderHexColor.hexcolor))
	}

	/**고민 폴더 text 색상 설정(HexColor, ItemStatus)*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderTextColor")
	fun setAgonyFolderTextColor(
		textView: TextView,
		agonyFolderHexColor: AgonyFolderHexColor,
	) {
		when (agonyFolderHexColor) {
			AgonyFolderHexColor.WHITE,
			AgonyFolderHexColor.YELLOW,
			AgonyFolderHexColor.ORANGE -> {
				textView.setTextColor(Color.parseColor("#595959"))
			}

			AgonyFolderHexColor.BLACK,
			AgonyFolderHexColor.GREEN,
			AgonyFolderHexColor.PURPLE,
			AgonyFolderHexColor.MINT -> {
				textView.setTextColor(Color.parseColor("#FFFFFF"))
			}
		}
	}

	/**고민 폴더 Editting Mode 색상 설정(HexColor, ItemStatus)*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderEdittingBackgroundTint")
	fun setAgonyFolderEdittingBackgroundTint(
		view: View,
		isSelected: Boolean,
	) {
		when (isSelected) {
			false -> {
				view.backgroundTintList =
					ColorStateList.valueOf(ContextCompat.getColor(App.instance, R.color.agony_color_white))
			}

			true -> {
				view.backgroundTintList =
					ColorStateList.valueOf(ContextCompat.getColor(App.instance, R.color.agony_color_selected))
			}
		}
	}

	/**고민 폴더 체크 여부 Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderEditingCheckedVisibility")
	fun setAgonyFolderEditingCheckedVisibility(
		view: View,
		isSelected: Boolean
	) {
		when (isSelected) {
			false -> view.visibility = View.INVISIBLE
			true -> view.visibility = View.VISIBLE
		}
	}

	/** 고민탭 EditingState일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderEditingComponentVisibility")
	fun setAgonyFolderEditingComponentVisibility(
		view: View,
		agonyUiState: AgonyUiState.UiState
	) {
		if (agonyUiState == AgonyUiState.UiState.EDITING) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/** 고민탭 DefaultState일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderDefaultComponentVisibility")
	fun setAgonyFolderDefaultComponentVisibility(
		view: View,
		agonyUiState: AgonyUiState.UiState
	) {
		if (agonyUiState == AgonyUiState.UiState.SUCCESS) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 생성 Dialog text 색상 설정*/
	@JvmStatic
	@BindingAdapter("setMakeAgonyTextColorWithFolderHexColor")
	fun setMakeAgonyTextColorWithFolderHexColor(
		textView: TextView,
		agonyFolderHexColor: AgonyFolderHexColor
	) {
		when (agonyFolderHexColor) {
			AgonyFolderHexColor.WHITE,
			AgonyFolderHexColor.YELLOW,
			AgonyFolderHexColor.ORANGE -> {
				textView.setTextColor(Color.parseColor("#595959"))
				textView.setHintTextColor(Color.parseColor("#595959"))
			}

			AgonyFolderHexColor.BLACK,
			AgonyFolderHexColor.GREEN,
			AgonyFolderHexColor.PURPLE,
			AgonyFolderHexColor.MINT -> {
				textView.setTextColor(Color.parseColor("#FFFFFF"))
				textView.setHintTextColor(Color.parseColor("#FFFFFF"))
			}
		}
	}

	/**고민 Color Circle Click 가능 여부 설정*/
	@JvmStatic
	@BindingAdapter("setClickableWithAgonyFolderHexColor")
	fun setClickableWithAgonyFolderHexColor(toggleButton: ToggleButton, checkedFlag: Boolean) {
		toggleButton.isClickable = !checkedFlag
	}

	/**고민 기록 FirstItem Default State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibiltyFirstItemInDefaultState")
	fun setVisibiltyInFirstItemView(
		view: View,
		nowFirstItemStatus: AgonyRecordFirstItemStatus
	) {
		if (nowFirstItemStatus == AgonyRecordFirstItemStatus.Default) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 FirstItem Editing State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibiltyFirstItemInEditingState")
	fun setVisibiltyInEditingItemView(
		view: View,
		nowFirstItemStatus: AgonyRecordFirstItemStatus
	) {
		if (nowFirstItemStatus == AgonyRecordFirstItemStatus.Editing) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 FirstItem Loading State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibiltyFirstItemInLoadingState")
	fun setVisibiltyInLoadingItemView(
		view: View,
		nowFirstItemStatus: AgonyRecordFirstItemStatus
	) {
		if (nowFirstItemStatus == AgonyRecordFirstItemStatus.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Default State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibiltyDataItemInDefaultState")
	fun setVisibiltyDataItemInDefaultState(
		view: View,
		nowFirstItemStatus: AgonyRecordDataItemStatus
	) {
		if (nowFirstItemStatus == AgonyRecordDataItemStatus.Default) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Editing State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibiltyDataItemInEditingState")
	fun setVisibiltyDataItemInEditingState(
		view: View,
		nowFirstItemStatus: AgonyRecordDataItemStatus
	) {
		if (nowFirstItemStatus == AgonyRecordDataItemStatus.Editing) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Loading State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibiltyDataItemInLoadingState")
	fun setVisibiltyDataItemInLoadingState(
		view: View,
		nowFirstItemStatus: AgonyRecordDataItemStatus
	) {
		if (nowFirstItemStatus == AgonyRecordDataItemStatus.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**Login페이지 Loading UI Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityLoadingUIInLogin")
	fun setVisibilityLoadingUIInLogin(view: View, uiState: LoginUiState) {
		if (uiState == LoginUiState.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**UserChatRoomListItem 시간 Text 세팅*/
	@JvmStatic
	@BindingAdapter("getFormattedDetailDateTimeText")
	fun getFormattedDetailDateTimeText(view: TextView, dateAndTimeString: String?) {
		if (dateAndTimeString.isNullOrBlank()) return
		view.text = DateManager.getFormattedDetailDateTimeText(dateAndTimeString)
	}

	/**UserChatRoomListItem 채팅방 이미지 세팅*/
	@JvmStatic
	@BindingAdapter("defaultImgNum", "imgUrl", requireAll = false)
	fun setChatListItemImg(
		view: ImageView,
		defaultImgNum: Int,
		imgUrl: String?
	) {
		if (imgUrl.isNullOrBlank()) {
			setRandomChatRoomImg(view, defaultImgNum)
			return
		}
		loadUrl(view, imgUrl)
	}

	/**SearchChatRoomItem 시간 Text 세팅*/
	@JvmStatic
	@BindingAdapter("getFormattedAbstractDateTimeText")
	fun getFormattedAbstractDateTimeText(view: TextView, dateAndTimeString: String?) {
		if (dateAndTimeString.isNullOrBlank()) return
		view.text = DateManager.getFormattedAbstractDateTimeText(dateAndTimeString)
	}

	/**SearchChatRoomItem 태그 파싱*/
	@JvmStatic
	@BindingAdapter("parseHashTagText")
	fun parseHashTagText(view: TextView, hashTagString: String) {
		view.text = hashTagString.split(",").joinToString("  ") { "#$it" }
	}

	/**화면 크기에 맞는 도서 이미지 크기 세팅*/
	@JvmStatic
	@BindingAdapter("setBookImgSize")
	fun setBookImgSize(view: View, bool: Boolean) {
		with(view) {
			layoutParams.width = BookImgSizeManager.bookImgWidthPx
			layoutParams.height = BookImgSizeManager.bookImgHeightPx
		}
	}

	/**화면 크기에 맞는 Dialog 크기 세팅*/
	@JvmStatic
	@BindingAdapter("setDialogSize")
	fun setDialogSize(view: View, bool: Boolean) {
		view.layoutParams.width = DialogSizeManager.dialogWidthPx
	}

	/** MakeChatRoom 채팅방 생성 기본 이미지 세팅*/
	@JvmStatic
	@BindingAdapter("defaultImgNum", "imgByteArray", requireAll = false)
	fun setMakeChatRoomImg(
		view: ImageView,
		defaultImgNum: Int,
		imgByteArray: ByteArray
	) {
		if (imgByteArray.isEmpty()) {
			setRandomChatRoomImg(view, defaultImgNum)
			return
		}
		loadByteArray(view, imgByteArray)
	}

	@JvmStatic
	fun setRandomChatRoomImg(view: ImageView, defaultImgNum: Int) {
		when (defaultImgNum) {
			1 -> view.setImageResource(R.drawable.default_chat_room_img1)
			2 -> view.setImageResource(R.drawable.default_chat_room_img2)
			3 -> view.setImageResource(R.drawable.default_chat_room_img3)
			4 -> view.setImageResource(R.drawable.default_chat_room_img4)
			5 -> view.setImageResource(R.drawable.default_chat_room_img5)
			6 -> view.setImageResource(R.drawable.default_chat_room_img6)
			7 -> view.setImageResource(R.drawable.default_chat_room_img7)
		}
	}

	/** MakeChatRoom 화면 크기에 맞는 채팅방 이미지 크기 세팅*/
	@JvmStatic
	@BindingAdapter("setMakeChatRoomImgSize")
	fun setMakeChatRoomImgSize(view: ImageView, bool: Boolean) {
		val sizeManager = MakeChatRoomImgSizeManager()
		with(view) {
			layoutParams.width = sizeManager.chatRoomImgWidthPx
			layoutParams.height = sizeManager.chatRoomImgHeightPx
		}
	}

	/**독서취향 : 제출 버튼 색상 설정*/
	@JvmStatic
	@BindingAdapter("setSubmitTextColor")
	fun setSubmitTextColor(textView: TextView, flag: Boolean) {
		if (flag) {
			textView.setTextColor(Color.parseColor("#000000"))
			textView.isClickable = true
			return
		}
		textView.setTextColor(Color.parseColor("#B5B7BB"))
		textView.isClickable = false
	}

	/**Shimmer내부 GirdLayout 설정*/
	@JvmStatic
	@BindingAdapter("setShimmerGridLayout")
	fun setShimmerGridLayout(gridLayout: GridLayout, bool: Boolean) {
		gridLayout.columnCount = BookImgSizeManager.flexBoxBookSpanSize
		gridLayout.rowCount = 2
	}

	/**Shimmer Animation Start/Stop 설정*/
	@JvmStatic
	@BindingAdapter("setShimmerAnimation")
	fun setShimmerAnimation(
		shimmerFrameLayout: ShimmerFrameLayout,
		isVisible: Boolean
	) {
		if (isVisible) return
		shimmerFrameLayout.stopShimmer()
	}

	/**Shimmer Animation Start/Stop 설정*/
	@JvmStatic
	@BindingAdapter("setChatInputEtFocusChangeListener")
	fun setChatInputEtFocusChangeListener(editText: EditText, bool: Boolean) {
		editText.setOnFocusChangeListener { view, hasFocus ->
			if (view !is EditText) return@setOnFocusChangeListener

			if (hasFocus) {
				view.maxLines = 4
				return@setOnFocusChangeListener
			}
			view.maxLines = 1
		}
	}

	/**ChatItem 시간 Text 세팅*/
	@JvmStatic
	@BindingAdapter("getFormattedTimeText")
	fun getFormattedTimeText(view: TextView, dateAndTimeString: String?) {
		if (dateAndTimeString.isNullOrBlank()) return
		view.text = DateManager.getFormattedTimeText(dateAndTimeString)
	}

	/**ChatItem 전송 이미지 Visibility 세팅*/
	@JvmStatic
	@BindingAdapter("setChatSendImgVisibility")
	fun setChatSendImgVisibility(view: View, chatStatus: ChatStatus?) {
		view.visibility = when (chatStatus) {
			ChatStatus.LOADING -> View.VISIBLE
			else -> View.GONE
		}
	}

	/**ChatItem 전송 실패 버튼 Visibility 세팅*/
	@JvmStatic
	@BindingAdapter("setChatFailBtnVisibility")
	fun setChatFailBtnVisibility(view: View, chatStatus: ChatStatus?) {
		view.visibility = when (chatStatus) {
			ChatStatus.FAILURE -> View.VISIBLE
			else -> View.GONE
		}
	}

	/**다른 사람의 새로운 채팅 바텀 공지 Visibility 세팅*/
	@JvmStatic
	@BindingAdapter("setNewOtherChatNoticeVisibility")
	fun setNewOtherChatNoticeVisibility(view: View, chat: Chat?) {
		view.visibility = if (chat == null) View.GONE else View.VISIBLE
	}

	/**채팅 날짜 표시 텍스트 세팅*/
	@JvmStatic
	@BindingAdapter("getDateKoreanString")
	fun getDateKoreanString(textView: TextView, chat: Chat) {
		val dispatchTime = chat.dispatchTime
		if (dispatchTime.isBlank()) return
		textView.text = DateManager.getDateKoreanString(dispatchTime)
	}

	/**채팅 날짜 Visibility 세팅*/
	@JvmStatic
	@BindingAdapter("setDateChatVisibility")
	fun setDateChatVisibility(view: View, bool: Boolean?) {
		view.visibility = if (bool == false) View.VISIBLE else View.GONE
	}

	/**DrawerLayout 스와이프 off 세팅*/
	@JvmStatic
	@BindingAdapter("setDrawerLayoutSwipeOff")
	fun setDrawerLayoutSwipeOff(view: DrawerLayout, bool: Boolean?) {
		view.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
	}
}