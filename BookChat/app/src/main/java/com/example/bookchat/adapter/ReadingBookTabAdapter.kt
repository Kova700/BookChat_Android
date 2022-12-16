package com.example.bookchat.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.adapter.WishBookTabAdapter.Companion.BOOK_SHELF_ITEM_COMPARATOR
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ItemReadingBookTabBinding
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReadingBookTabAdapter(private val bookShelfViewModel: BookShelfViewModel)
    : PagingDataAdapter<BookShelfItem, ReadingBookTabAdapter.ReadingBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemReadingBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener
    private lateinit var pageBtnClickListener :OnItemClickListener

    inner class ReadingBookItemViewHolder(val binding: ItemReadingBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(book : BookShelfItem){
            with(this.binding){
                bookShelfItem = book
                setViewHolderState(swipeView,book.isSwiped)

                swipeView.setOnClickListener {
                    itemClickListener.onItemClick(book)
                }

                swipeView.setOnLongClickListener {
                    startSwipeAnimation(swipeView,book.isSwiped)
                    book.isSwiped = !book.isSwiped
                    true //true = clickEvent 종료 (ClickEvnet가 작동하지 않음)
                }

                swipeBackground.setOnClickListener {
                    setSwiped(false)

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        bookShelfViewModel.deleteBookShelfBookWithSwipe(book)
                        bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.RemoveWaiting(book), ReadingStatus.READING)
                        bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.Remove(book), ReadingStatus.READING)
                    }, SNACK_BAR_DURATION.toLong())

                    val snackCancelClickListener = View.OnClickListener {
                        bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.RemoveWaiting(book), ReadingStatus.READING)
                        handler.removeCallbacksAndMessages(null)
                    }

                    bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.RemoveWaiting(book), ReadingStatus.READING)
                    Snackbar.make(binding.root,"도서 삭제가 완료되었습니다.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("실행취소",snackCancelClickListener)
                        .setDuration(SNACK_BAR_DURATION)
                        .show()

                    swipeView.translationX = 0f
                }

                pageBtn.setOnClickListener{
                    pageBtnClickListener.onItemClick(book)
                }

            }
        }

        fun setSwiped(isClamped: Boolean){
            val currentItem = getItem(absoluteAdapterPosition)
            currentItem?.let { currentItem.isSwiped = isClamped }
        }

        fun getSwiped(): Boolean{
            return getItem(absoluteAdapterPosition)?.isSwiped ?: false
        }
    }

    private fun startSwipeAnimation(view: View, isSwiped :Boolean) =
        CoroutineScope(Dispatchers.Main).launch {
            val swipedX = view.width.toFloat() * SWIPE_VIEW_PERCENT
            if(isSwiped) {
                while (view.translationX > 0F){
                    view.translationX -= swipedX /20
                    delay(5L)
                }
                view.translationX = 0F
                return@launch
            }

            while (view.translationX < swipedX){
                view.translationX += swipedX /20
                delay(5L)
            }
            view.translationX = swipedX
        }

    private fun setViewHolderState(view: View, isSwiped :Boolean){
        if(!isSwiped) { view.translationX = 0f; return }
        view.translationX = view.width.toFloat() * SWIPE_VIEW_PERCENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingBookTabAdapter.ReadingBookItemViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_reading_book_tab,parent,false)

        return ReadingBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReadingBookTabAdapter.ReadingBookItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(book :BookShelfItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun setPageBtnClickListener(onItemClickListener: OnItemClickListener){
        this.pageBtnClickListener = onItemClickListener
    }

    companion object {
        private const val SWIPE_VIEW_PERCENT = 0.3F
        private const val SNACK_BAR_DURATION = 5000
    }

}