package com.example.bookchat.adapter.search.chatroomsearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.SearchChatRoomListItem
import com.example.bookchat.databinding.ItemChatRoomSearchBinding
import kotlin.math.min

class SearchChatRoomSimpleAdapter :
    RecyclerView.Adapter<SearchChatRoomSimpleAdapter.SearchChatRoomItemViewHolder>() {

    private lateinit var binding :ItemChatRoomSearchBinding
    private lateinit var itemClickListener: OnItemClickListener
    var chatRooms: List<SearchChatRoomListItem> = listOf()

    inner class SearchChatRoomItemViewHolder(val binding: ItemChatRoomSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(searchChatRoomListItem: SearchChatRoomListItem) {
            binding.searchChatRoomListItem = searchChatRoomListItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchChatRoomItemViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_chat_room_search,
            parent,
            false
        )
        return SearchChatRoomItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchChatRoomItemViewHolder, position: Int) {
        if (chatRooms.isNotEmpty()) holder.bind(chatRooms[position])
    }

    override fun getItemCount(): Int {
        return minOf(chatRooms.size, SHOW_SIZE)
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_search

    interface OnItemClickListener {
        fun onItemClick(searchChatRoomListItem: SearchChatRoomListItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object{
        private const val SHOW_SIZE = 3
    }
}