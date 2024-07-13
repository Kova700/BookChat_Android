package com.example.bookchat.ui

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.domain.model.NicknameCheckState
import com.example.bookchat.utils.*
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.image.loadUrl
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
		imageView.loadUrl(url)
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

	/**회원가입 :닉네임 검사 상황별 안내문구*/
	//텍스트 (색 , 글자)
	@JvmStatic
	@BindingAdapter("setTextViewFromCheckResult")
	fun setTextViewFromCheckResult(textView: TextView, nicknameCheckState: NicknameCheckState) {
		when (nicknameCheckState) {
			NicknameCheckState.Default -> textView.text = ""

			NicknameCheckState.IsShort -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_short)
			}

			NicknameCheckState.IsDuplicate -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_duplicate)
			}

			NicknameCheckState.IsSpecialCharInText -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text =
					textView.context.resources.getString(R.string.name_check_status_special_char)
			}

			NicknameCheckState.IsPerfect -> {
				textView.setTextColor(Color.parseColor("#5648FF"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_perfect)
			}

		}
	}

	/**회원가입 :닉네임 검사 상황별 테두리 색상*/
	//레이아웃 (테두리)
	@JvmStatic
	@BindingAdapter("setLayoutFromCheckResult")
	fun setLayoutFromCheckResult(view: View, nicknameCheckState: NicknameCheckState) {
		Log.d(
			TAG,
			"DataBindingAdapter: setLayoutFromCheckResult() - nicknameCheckState : $nicknameCheckState"
		)
		when (nicknameCheckState) {
			NicknameCheckState.Default,
			-> {
				view.background = ResourcesCompat.getDrawable(
					view.resources,
					R.drawable.nickname_input_back_white,
					null
				)
			}

			NicknameCheckState.IsShort,
			NicknameCheckState.IsDuplicate,
			NicknameCheckState.IsSpecialCharInText,
			-> {
				view.background = ResourcesCompat.getDrawable(
					view.resources,
					R.drawable.nickname_input_back_red,
					null
				)
			}

			NicknameCheckState.IsPerfect -> {
				view.background = ResourcesCompat.getDrawable(
					view.context.resources,
					R.drawable.nickname_input_back_blue,
					null
				)
			}
		}
	}

}