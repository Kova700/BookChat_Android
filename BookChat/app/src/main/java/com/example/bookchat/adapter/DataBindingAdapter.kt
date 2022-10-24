package com.example.bookchat.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

    /**이미지뷰 이미지 설정(ByteArray)*/
    @JvmStatic
    @BindingAdapter("loadByteArray")
    fun loadByteArray(imageView: ImageView, byteArray: ByteArray){
        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        Glide.with(imageView.context)
            .load(bitmap)
            .placeholder(R.drawable.ic_default_profile_img1)
            .error(R.drawable.ic_default_profile_img1)
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
    }

    /**회원가입 :닉네임 검사 상황별 안내문구*/
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
                textView.text = App.instance.resources.getString(R.string.message_name_check_status_short)
            }
            NameCheckStatus.IsDuplicate ->{
                textView.setTextColor(Color.parseColor("#FF004D"))
                textView.text = App.instance.resources.getString(R.string.message_name_check_status_duplicate)
            }
            NameCheckStatus.IsSpecialCharInText -> {
                textView.setTextColor(Color.parseColor("#FF004D"))
                textView.text = App.instance.resources.getString(R.string.message_name_check_status_special_char)
            }
            NameCheckStatus.IsPerfect -> {
                textView.setTextColor(Color.parseColor("#5648FF"))
                textView.text = App.instance.resources.getString(R.string.message_name_check_status_perfect)
            }
        }
    }

    /**회원가입 :닉네임 검사 상황별 테두리 색상*/
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
}