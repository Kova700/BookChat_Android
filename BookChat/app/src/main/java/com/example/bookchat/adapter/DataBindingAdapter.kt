package com.example.bookchat.adapter

import android.graphics.Color
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.utils.NameCheckStatus

object DataBindingAdapter {
    
    /**이미지뷰 이미지 설정(URL)*/
    @JvmStatic
    @BindingAdapter("loadUrl")
    fun loadUrl(imageView: ImageView, url: String?){
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.default_img)
            .error(R.drawable.default_img)
            .into(imageView)
    }

    /**독서취향 : 제출 버튼 색상 설정*/
    @JvmStatic
    @BindingAdapter("setButtonColor")
    fun setButtonColor(button : Button, isTastesEmpty :Boolean){
        if (isTastesEmpty) {
            button.setBackgroundColor(Color.parseColor("#D9D9D9"))
            button.isEnabled = false
            return
        }
        button.setBackgroundColor(Color.parseColor("#5648FF"))
        button.isEnabled = true
        return
    }

    /**회원가입 :닉네임 입력창 상황별 UI설정*/
    //텍스트 (색 , 글자)
    @JvmStatic
    @BindingAdapter("setTextViewFromCheckResult")
    fun setTextViewFromCheckResult(textView :TextView, nameCheckStatus: NameCheckStatus){
        when(nameCheckStatus){
            NameCheckStatus.Default -> {
                textView.text = ""
            }
            NameCheckStatus.IsShort -> {
                textView.setTextColor(Color.parseColor("#FF004D"))
                textView.text = "최소 2자리 이상 입력해 주세요."
            }
            NameCheckStatus.IsDuplicate ->{
                textView.setTextColor(Color.parseColor("#FF004D"))
                textView.text = "이미 사용 중인 닉네임 입니다."
            }
            NameCheckStatus.IsSpecialCharInText -> {
                textView.setTextColor(Color.parseColor("#FF004D"))
                textView.text = "특수문자는 사용 불가능합니다."
            }
            NameCheckStatus.IsPerfect -> {
                textView.setTextColor(Color.parseColor("#5648FF"))
                textView.text = "사용 가능한 닉네임입니다."
            }
        }
    }
    //레이아웃 (테두리)
    @JvmStatic
    @BindingAdapter("setLayoutFromCheckResult")
    fun setLayoutFromCheckResult(layout :LinearLayout, nameCheckStatus: NameCheckStatus){
        when(nameCheckStatus){
            NameCheckStatus.Default ->{
                layout.background = ResourcesCompat.getDrawable(App.instance.resources,R.drawable.nickname_input_back_white,null)
            }
            NameCheckStatus.IsShort,
            NameCheckStatus.IsDuplicate,
            NameCheckStatus.IsSpecialCharInText -> {
                layout.background = ResourcesCompat.getDrawable(App.instance.resources,R.drawable.nickname_input_back_red,null)
            }
            NameCheckStatus.IsPerfect -> {
                layout.background = ResourcesCompat.getDrawable(App.instance.resources,R.drawable.nickname_input_back_blue,null)
            }
        }
    }

//    /**갤러리 사용자 권한 요청 설정*/
//    @JvmStatic
//    @BindingAdapter("requestPermission")
//    fun requestPermission() {
//
//    }


    // 현재 상황별 Enum 클래스 만들고
    // ViewModel의  _nameCheckResult객체를 내가 바꾸고 싶은 위젯의 바인딩어댑터 함수에 집어넣고
    // 바인딩어댑터 함수는 전달 받은 객체의 타입이 뭐냐에 따라 보여주고 싶은 UI 형태를 보여주는거지
    // 상황이 바뀔때마다 라이브데이터로 정의된 _nameCheckResult객체를 수정하면 UI도 자동으로 바뀌는거지
}