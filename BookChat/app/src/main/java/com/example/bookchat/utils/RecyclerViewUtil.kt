package com.example.bookchat.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.isScrolling(): Boolean {
	return scrollState == RecyclerView.SCROLL_STATE_DRAGGING ||
					scrollState == RecyclerView.SCROLL_STATE_SETTLING
}

fun LinearLayoutManager.isVisibleItem(itemPosition: Int): Boolean {
	return (itemCount != 0) &&
					itemPosition in findFirstVisibleItemPosition()..findLastVisibleItemPosition()
}

fun LinearLayoutManager.isOnBottom(): Boolean {
	return (itemCount != 0) && isVisibleItem(0)
}

fun LinearLayoutManager.isOnTop(): Boolean {
	return (itemCount != 0) && isVisibleItem(itemCount - 1)
}