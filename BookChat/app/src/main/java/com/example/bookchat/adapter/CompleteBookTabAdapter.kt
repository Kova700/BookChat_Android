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
import com.example.bookchat.databinding.ItemCompleteBookTabBinding
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CompleteBookTabAdapter(private val bookShelfViewModel: BookShelfViewModel)
    : PagingDataAdapter<BookShelfItem, CompleteBookTabAdapter.CompleteBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemCompleteBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener

    inner class CompleteBookItemViewHolder(val binding: ItemCompleteBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(book :BookShelfItem){
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
                        bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.RemoveWaiting(book), ReadingStatus.COMPLETE)
                        bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.Remove(book), ReadingStatus.COMPLETE)
                    }, SNACK_BAR_DURATION.toLong())

                    val snackCancelClickListener = View.OnClickListener {
                        bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.RemoveWaiting(book), ReadingStatus.COMPLETE)
                        handler.removeCallbacksAndMessages(null)
                    }

                    bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.RemoveWaiting(book), ReadingStatus.COMPLETE)
                    Snackbar.make(binding.root,"도서 삭제가 완료되었습니다.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("실행취소",snackCancelClickListener)
                        .setDuration(SNACK_BAR_DURATION)
                        .show()
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

    private fun setViewHolderState(view: View, isSwiped :Boolean){
        if(!isSwiped) { view.translationX = 0f; return }
        view.translationX = view.width.toFloat() * SWIPE_VIEW_PERCENT
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompleteBookItemViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_complete_book_tab,parent,false)

        return CompleteBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompleteBookItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(book :BookShelfItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object {
        private const val SWIPE_VIEW_PERCENT = 0.3F
        private const val SNACK_BAR_DURATION = 5000
    }

}