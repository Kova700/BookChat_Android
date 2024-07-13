package com.example.bookchat.ui

import android.view.View
import android.widget.*
import androidx.databinding.BindingAdapter
import com.example.bookchat.data.*
import com.example.bookchat.utils.*
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
}