package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.DialogWishBookTapClickedBinding
import com.example.bookchat.viewmodel.ViewModelFactory
import com.example.bookchat.viewmodel.WishBookTapDialogViewModel

class WishTapBookDialog(private val book: BookShelfItem) : DialogFragment() {
    private lateinit var binding :DialogWishBookTapClickedBinding
    private lateinit var wishBookTapDialogViewModel: WishBookTapDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_wish_book_tap_clicked, container, false)
        wishBookTapDialogViewModel = ViewModelProvider(this, ViewModelFactory()).get(
            WishBookTapDialogViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewmodel = wishBookTapDialogViewModel
        wishBookTapDialogViewModel.book = book
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //하트 처음부터 차있고, 누르면 사라져야함(Wish 삭제 API 호출)
    //독서중 버튼 누르면 상태이동 API 호출
}