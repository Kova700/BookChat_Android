package com.example.bookchat.adapter.search.chatroomsearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.SearchChatRoomListItem
import com.example.bookchat.databinding.ItemChatRoomSearchBinding

class SearchChatRoomDetailAdapter :
    PagingDataAdapter<SearchChatRoomListItem, SearchChatRoomDetailAdapter.SearchChatRoomItemViewHolder>(
        CHAT_ROOM_COMPARATOR
    ) {
    private lateinit var binding: ItemChatRoomSearchBinding
    private lateinit var itemClickListener: OnItemClickListener

    inner class SearchChatRoomItemViewHolder(val binding: ItemChatRoomSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(searchChatRoomListItem: SearchChatRoomListItem) {
            binding.searchChatRoomListItem = searchChatRoomListItem
            binding.root.setOnClickListener{
                itemClickListener.onItemClick(searchChatRoomListItem)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchChatRoomItemViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_chat_room_search, parent, false
        )
        return SearchChatRoomItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchChatRoomItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_search

    interface OnItemClickListener {
        fun onItemClick(searchChatRoomListItem: SearchChatRoomListItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object {
        private val CHAT_ROOM_COMPARATOR =
            object : DiffUtil.ItemCallback<SearchChatRoomListItem>() {
                override fun areItemsTheSame(
                    oldItem: SearchChatRoomListItem,
                    newItem: SearchChatRoomListItem
                ) = oldItem.roomId == newItem.roomId

                override fun areContentsTheSame(
                    oldItem: SearchChatRoomListItem,
                    newItem: SearchChatRoomListItem
                ) = oldItem == newItem
            }
    }
}