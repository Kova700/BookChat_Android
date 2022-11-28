package com.example.bookchat.adapter

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

class ReadingBookTabAdapter(private val bookShelfViewModel: BookShelfViewModel)
    : PagingDataAdapter<BookShelfItem, ReadingBookTabAdapter.ReadingBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemReadingBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener
    private lateinit var pageBtnClickListener :OnItemClickListener

    inner class ReadingBookItemViewHolder(val binding: ItemReadingBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(book : BookShelfItem){
            with(this.binding){
                bookShelfItem = book
                swipeView.setOnClickListener {
                    itemClickListener.onItemClick(book)
                }
                swipeBackground.setOnClickListener {
                    bookShelfViewModel.onPagingViewEvent(BookShelfViewModel.PagingViewEvent.Remove(book), ReadingStatus.READING)
                    bookShelfViewModel.deleteBookShelfBook(book)
                    swipeView.translationX = 0f
                }
                pageBtn.setOnClickListener{
                    pageBtnClickListener.onItemClick(book)
                }
                setViewHolderState(swipeView,book.isSwiped)
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
    }

}