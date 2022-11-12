package com.example.bookchat.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.LoadState
import com.example.bookchat.utils.NameCheckStatus
import com.example.bookchat.utils.SearchTapStatus

object DataBindingAdapter {

    /**텍스트뷰 select 설정*/
    @JvmStatic
    @BindingAdapter("setSelected")
    fun setSelected(view: View, boolean: Boolean){
        view.isSelected = boolean
    }

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
    /** EditText 엔터이벤트 등록*/
    @JvmStatic
    @BindingAdapter("setEnterListener")
    fun setEnterListener(editText : EditText, listener : TextView.OnEditorActionListener){
        editText.setOnEditorActionListener(listener)
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

    /**검색창 : 검색어 삭제버튼 Visible 설정*/
    @JvmStatic
    @BindingAdapter("setSearchDeleteBtnVisibility")
    fun setSearchDeleteBtnVisibility(view: View, searchTapStatus : SearchTapStatus){
        if (searchTapStatus !is SearchTapStatus.Searching) {
            view.visibility = View.INVISIBLE
            return
        }
        view.visibility = View.VISIBLE
    }

    /**검색창 애니메이션 터치뷰 : 검색창 애니메이션 터치뷰 Visible 설정*/
    @JvmStatic
    @BindingAdapter("setAnimationTouchViewVisibility")
    fun setAnimationTouchViewVisibility(view: View, searchTapStatus : SearchTapStatus){
        if (searchTapStatus !is SearchTapStatus.Default) {
            view.visibility = View.INVISIBLE
            return
        }
        view.visibility = View.VISIBLE
    }

    /**검색창 뒤로가기 버튼 : 검색창 뒤로가기 버튼 Visible 설정*/
    @JvmStatic
    @BindingAdapter("setBackBtnViewVisibility")
    fun setBackBtnViewVisibility(view: View, searchTapStatus : SearchTapStatus){
        if (searchTapStatus !is SearchTapStatus.Default) {
            view.visibility = View.VISIBLE
            return
        }
        view.visibility = View.INVISIBLE
    }

    /**리사이클러뷰 아이템 연결*/
    @JvmStatic
    @BindingAdapter("setItem")
    fun setItem(recyclerView: RecyclerView, data :List<Any>){
        if(data.isEmpty()) return

        when(recyclerView.id){

            R.id.search_result_book_simple_Rcv -> {
                if(data.first() is Book) {
                    val books = data.map { it as Book }
                    val searchResultBookSimpleAdapter = recyclerView.adapter as SearchResultBookSimpleAdapter
                    searchResultBookSimpleAdapter.books = books
                }
            }

            /*서재 전부 페이징으로 수정 예정*/
            R.id.wishBookRcv -> {
                Log.d(TAG, "DataBindingAdapter: setItem() - called")
                if(data.first() is BookShelfItem) {
                    val bookShelfItemList = data.map { it as BookShelfItem }
                    val wishBookTabAdapter = recyclerView.adapter as WishBookTabAdapter
                    wishBookTabAdapter.books = bookShelfItemList
                    wishBookTabAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    /**ProgressBar Viisbilty 설정*/
    @JvmStatic
    @BindingAdapter("setProgressBarVisibility")
    fun setProgressBarVisibility(view: View, loadState: LoadState){
        when(loadState){
            LoadState.Default,
            LoadState.Result -> view.visibility = View.INVISIBLE
            LoadState.Loading -> view.visibility = View.VISIBLE
        }
    }

    /**RecyclerView Viisbilty 설정*/
    @JvmStatic
    @BindingAdapter("setRcvVisibility")
    fun setRcvVisibility(view: View, loadState: LoadState){
        when(loadState){
            LoadState.Default,
            LoadState.Result -> view.visibility = View.VISIBLE
            LoadState.Loading -> view.visibility = View.INVISIBLE
        }
    }

}