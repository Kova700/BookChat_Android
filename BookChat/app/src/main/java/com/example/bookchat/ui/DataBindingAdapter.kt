package com.example.bookchat.ui

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.NameCheckStatus
import com.example.bookchat.domain.model.UserDefaultProfileType
import com.example.bookchat.ui.agony.AgonyUiState
import com.example.bookchat.ui.agonyrecord.model.AgonyRecordListItem
import com.example.bookchat.ui.login.LoginUiState
import com.example.bookchat.utils.*
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
		if (url.isNullOrEmpty()) return

		Glide.with(imageView.context)
			.load(url)
			.placeholder(R.drawable.loading_img)
			.error(R.drawable.error_img)
			.into(imageView)
	}

	/**이미지뷰 이미지 설정(Bitmap)*/
	@JvmStatic
	@BindingAdapter("loadBitmap")
	fun loadBitmap(imageView: ImageView, bitmap: Bitmap?) {
		if (bitmap == null) return

		Glide.with(imageView.context)
			.load(bitmap)
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
		userDefaultProfileType: UserDefaultProfileType?
	) {
		if (!userProfileUrl?.trim().isNullOrBlank()) {
			loadUrl(imageView, userProfileUrl)
			return
		}
		inflateUserDefaultProfileImage(imageView, userDefaultProfileType)
	}

	private fun inflateUserDefaultProfileImage(
		imageView: ImageView,
		imageType: UserDefaultProfileType?
	) {
		Glide.with(imageView.context)
			.load(getUserDefaultProfileImage(imageType))
			.fitCenter()
			.placeholder(R.drawable.loading_img)
			.error(R.drawable.error_img)
			.into(imageView)
	}

	private fun getUserDefaultProfileImage(imageType: UserDefaultProfileType?) =
		when (imageType) {
			null,
			UserDefaultProfileType.ONE -> R.drawable.default_profile_img1

			UserDefaultProfileType.TWO -> R.drawable.default_profile_img2
			UserDefaultProfileType.THREE -> R.drawable.default_profile_img3
			UserDefaultProfileType.FOUR -> R.drawable.default_profile_img4
			UserDefaultProfileType.FIVE -> R.drawable.default_profile_img5
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
				textView.text = textView.context.resources.getString(R.string.name_check_status_short)
			}

			NameCheckStatus.IsDuplicate -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_duplicate)
			}

			NameCheckStatus.IsSpecialCharInText -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text =
					textView.context.resources.getString(R.string.name_check_status_special_char)
			}

			NameCheckStatus.IsPerfect -> {
				textView.setTextColor(Color.parseColor("#5648FF"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_perfect)
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
					view.resources,
					R.drawable.nickname_input_back_white,
					null
				)
			}

			NameCheckStatus.IsShort,
			NameCheckStatus.IsDuplicate,
			NameCheckStatus.IsSpecialCharInText -> {
				view.background = ResourcesCompat.getDrawable(
					view.resources,
					R.drawable.nickname_input_back_red,
					null
				)
			}

			NameCheckStatus.IsPerfect -> {
				view.background = ResourcesCompat.getDrawable(
					view.context.resources,
					R.drawable.nickname_input_back_blue,
					null
				)
			}
		}
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
					ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.agony_color_white))
			}

			true -> {
				view.backgroundTintList =
					ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.agony_color_selected))
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
	@BindingAdapter("setVisibilityInFirstItemView")
	fun setVisibilityInFirstItemView(
		view: View,
		itemState: AgonyRecordListItem.ItemState
	) {
		if (itemState is AgonyRecordListItem.ItemState.Success) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 FirstItem Editing State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityInEditingItemView")
	fun setVisibilityInEditingItemView(
		view: View,
		itemState: AgonyRecordListItem.ItemState
	) {
		if (itemState is AgonyRecordListItem.ItemState.Editing) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 FirstItem Loading State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityInLoadingItemView")
	fun setVisibilityInLoadingItemView(
		view: View,
		itemState: AgonyRecordListItem.ItemState
	) {
		if (itemState == AgonyRecordListItem.ItemState.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Default State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityDataItemInDefaultState")
	fun setVisibilityDataItemInDefaultState(
		view: View,
		itemState: AgonyRecordListItem.ItemState
	) {
		if (itemState is AgonyRecordListItem.ItemState.Success) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Editing State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityDataItemInEditingState")
	fun setVisibilityDataItemInEditingState(
		view: View,
		itemState: AgonyRecordListItem.ItemState
	) {
		if (itemState is AgonyRecordListItem.ItemState.Editing) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Loading State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityDataItemInLoadingState")
	fun setVisibilityDataItemInLoadingState(
		view: View,
		itemState: AgonyRecordListItem.ItemState
	) {
		if (itemState == AgonyRecordListItem.ItemState.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**Login 페이지 Loading UI Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityLoadingUIInLogin")
	fun setVisibilityLoadingUIInLogin(view: View, uiState: LoginUiState.UiState) {
		if (uiState == LoginUiState.UiState.LOADING) {
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
	@BindingAdapter("channelDefaultImageType", "imgUrl", requireAll = false)
	fun setChannelImg(
		view: ImageView,
		channelDefaultImageType: ChannelDefaultImageType,
		imgUrl: String?
	) {
		if (imgUrl.isNullOrBlank()) {
			setRandomChannelImg(view, channelDefaultImageType)
			return
		}
		loadUrl(view, imgUrl)
	}

	/** MakeChatRoom 채팅방 생성 기본 이미지 세팅*/
	@JvmStatic
	@BindingAdapter("channelDefaultImageType", "loadByteArray", requireAll = false)
	fun setMakeChannelImg(
		view: ImageView,
		channelDefaultImageType: ChannelDefaultImageType,
		imgByteArray: ByteArray?
	) {
		if (imgByteArray == null) {
			setRandomChannelImg(view, channelDefaultImageType)
			return
		}
		loadByteArray(view, imgByteArray)
	}

	@JvmStatic
	fun setRandomChannelImg(view: ImageView, channelDefaultImageType: ChannelDefaultImageType) {
		when (channelDefaultImageType) {
			ChannelDefaultImageType.ONE -> view.setImageResource(R.drawable.default_chat_room_img1)
			ChannelDefaultImageType.TWO -> view.setImageResource(R.drawable.default_chat_room_img2)
			ChannelDefaultImageType.THREE -> view.setImageResource(R.drawable.default_chat_room_img3)
			ChannelDefaultImageType.FOUR -> view.setImageResource(R.drawable.default_chat_room_img4)
			ChannelDefaultImageType.FIVE -> view.setImageResource(R.drawable.default_chat_room_img5)
			ChannelDefaultImageType.SIX -> view.setImageResource(R.drawable.default_chat_room_img6)
			ChannelDefaultImageType.SEVEN -> view.setImageResource(R.drawable.default_chat_room_img7)
		}
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

	// TODO : 여기부터
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
			ChatStatus.LOADING,
			ChatStatus.RETRY_REQUIRED -> View.VISIBLE

			else -> View.GONE
		}
	}

	/**ChatItem 전송 실패 버튼 Visibility 세팅*/
	@JvmStatic
	@BindingAdapter("setChatFailBtnVisibility")
	fun setChatFailBtnVisibility(view: View, chatStatus: ChatStatus?) {
		view.visibility = when (chatStatus) {
			ChatStatus.FAILURE -> View.VISIBLE
			//TODO : 이거만 보일게 아니라 전송시간 가려야함
			//TODO : 취소, 재전송 버튼 OnClickListener연결하려면 View 분리해서 사용해야함 (지금 덩어리 하나로 묶였음)
			else -> View.GONE
		}
	}

	//TODO : 여기까지 전부 MyChatViewholder가서 설정하자

	/**DrawerLayout 스와이프 off 세팅*/
	@JvmStatic
	@BindingAdapter("setDrawerLayoutSwipeOff")
	fun setDrawerLayoutSwipeOff(view: DrawerLayout, bool: Boolean?) {
		view.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
	}
}