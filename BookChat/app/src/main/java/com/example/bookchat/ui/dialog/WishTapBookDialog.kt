package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.DialogWishBookTapClickedBinding
import com.example.bookchat.ui.fragment.BookShelfFragment
import com.example.bookchat.ui.fragment.ReadingBookTabFragment
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.BookShelfViewModel.BookShelfEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import com.example.bookchat.viewmodel.WishBookTapDialogViewModel
import com.example.bookchat.viewmodel.WishBookTapDialogViewModel.WishBookEvent
import kotlinx.coroutines.launch

class WishTapBookDialog(private val book: BookShelfItem) : DialogFragment() {
    private lateinit var binding :DialogWishBookTapClickedBinding
    private lateinit var wishBookTapDialogViewModel: WishBookTapDialogViewModel
    private lateinit var bookShelfViewModel: BookShelfViewModel
    private var returnEvent :WishBookEvent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_wish_book_tap_clicked, container, false)
        wishBookTapDialogViewModel = ViewModelProvider(this, ViewModelFactory()).get(
            WishBookTapDialogViewModel::class.java)
        bookShelfViewModel = ViewModelProvider(getBookShelfFragment(), ViewModelFactory()).get(
            BookShelfViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewmodel = wishBookTapDialogViewModel
        wishBookTapDialogViewModel.book = book
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        observeEventFlow()

        //버튼 중복클릭 방지 (+네트워크가 연결되어있지 않을때는 클릭이 안되게) 수정이 필요해보임
        //혹은 API응답이 오기 전까지 클릭이 안되거나

        return binding.root
    }

    private fun getBookShelfFragment() :Fragment{
        return requireParentFragment().requireParentFragment()
    }

    private fun getReadingBookTabFragment() : ReadingBookTabFragment {
        return (getBookShelfFragment() as BookShelfFragment).pagerAdapter.readingBookTabFragment
    }

    private fun observeEventFlow() {
        lifecycleScope.launch {
            wishBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun handleEvent(event: WishBookEvent) = when(event){
        is WishBookEvent.RemoveItem -> {
            returnEvent = WishBookEvent.RemoveItem
        }
        is WishBookEvent.AddItem -> {
            returnEvent = WishBookEvent.AddItem
        }
        is WishBookEvent.MoveToReadingBook -> {
            returnEvent = WishBookEvent.MoveToReadingBook
            this.dismiss()
        }
    }

    override fun onDetach() {
        when(returnEvent){
            WishBookEvent.RemoveItem ->{
                val removeEvent = BookShelfViewModel.PagingViewEvent.Remove(book)
                bookShelfViewModel.onPagingViewEvent(removeEvent,ReadingStatus.WISH)
            }
            WishBookEvent.MoveToReadingBook -> {
                val removeEvent = BookShelfViewModel.PagingViewEvent.Remove(book)
                bookShelfViewModel.onPagingViewEvent(removeEvent,ReadingStatus.WISH)
                bookShelfViewModel.startEvent(BookShelfEvent.ChangeBookShelfTab(1))
                getReadingBookTabFragment().readingBookAdapter.refresh()
            }
            else -> { }
        }
        super.onDetach()
    }
}