package com.example.bookchat.ui.adapter.bookshelf.reading

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.ui.adapter.bookshelf.wish.WishBookShelfDataAdapter.Companion.BOOK_SHELF_ITEM_COMPARATOR
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.ItemReadingBookshelfDataBinding
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.ui.viewmodel.BookShelfViewModel
import com.example.bookchat.ui.viewmodel.BookShelfViewModel.PagingViewEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReadingBookShelfDataAdapter(private val bookShelfViewModel: BookShelfViewModel)
    : PagingDataAdapter<BookShelfDataItem, ReadingBookShelfDataAdapter.ReadingBookShelfDataViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemReadingBookshelfDataBinding
    private lateinit var itemClickListener : OnItemClickListener
    private lateinit var pageBtnClickListener : OnItemClickListener

    inner class ReadingBookShelfDataViewHolder(val binding: ItemReadingBookshelfDataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(bookShelfDataItem : BookShelfDataItem){
            with(binding){
                bookShelfItem = bookShelfDataItem.bookShelfItem
                setViewHolderState(swipeView,bookShelfDataItem.isSwiped)

                swipeView.setOnClickListener {
                    itemClickListener.onItemClick(bookShelfDataItem)
                }

                pageBtn.setOnClickListener{
                    pageBtnClickListener.onItemClick(bookShelfDataItem)
                }

                swipeView.setOnLongClickListener {
                    startSwipeAnimation(swipeView, bookShelfDataItem.isSwiped)
                    bookShelfDataItem.isSwiped = !bookShelfDataItem.isSwiped
                    true
                }

                swipeBackground.setOnClickListener {
                    setSwiped(false)
                    val removeWaitingEvent = PagingViewEvent.RemoveWaiting(bookShelfDataItem)
                    bookShelfViewModel.addPagingViewEvent(removeWaitingEvent ,ReadingStatus.READING)

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        val removeEvent = PagingViewEvent.Remove(bookShelfDataItem)
                        bookShelfViewModel.deleteBookShelfBookWithSwipe(bookShelfDataItem,
                            removeEvent, removeWaitingEvent, ReadingStatus.READING)
                    }, SNACK_BAR_DURATION.toLong())

                    val snackCancelClickListener = View.OnClickListener {
                        bookShelfViewModel.removePagingViewEvent(removeWaitingEvent, ReadingStatus.READING)
                        handler.removeCallbacksAndMessages(null)
                    }

                    Snackbar.make(binding.root, R.string.bookshelf_delete_snack_bar, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.bookshelf_delete_snack_bar_cancel, snackCancelClickListener)
                        .setDuration(SNACK_BAR_DURATION)
                        .show()
                }

            }
        }

        private fun setSwiped(flag: Boolean){
            val currentItem = getItem(bindingAdapterPosition)
            currentItem?.let { currentItem.isSwiped = flag }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingBookShelfDataViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context),
                R.layout.item_reading_bookshelf_data,parent,false)
        return ReadingBookShelfDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReadingBookShelfDataViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(bookShelfDataItem : BookShelfDataItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun setPageBtnClickListener(onItemClickListener: OnItemClickListener){
        this.pageBtnClickListener = onItemClickListener
    }

    override fun getItemViewType(position: Int): Int  = R.layout.item_reading_bookshelf_data

    companion object {
        private const val SWIPE_VIEW_PERCENT = 0.3F
        private const val SNACK_BAR_DURATION = 3000
    }
}