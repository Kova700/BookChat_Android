package com.example.bookchat.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.isScrolling(): Boolean {
	return scrollState == RecyclerView.SCROLL_STATE_DRAGGING
					|| scrollState == RecyclerView.SCROLL_STATE_SETTLING
}

fun LinearLayoutManager.isVisiblePosition(itemPosition: Int): Boolean {
	val fvip = findFirstVisibleItemPosition()
	if (fvip == RecyclerView.NO_POSITION) return false
	val lvip = findLastVisibleItemPosition()
	return (itemCount != 0) && itemPosition in fvip..lvip
}

fun LinearLayoutManager.isOnHigherPosition(itemPosition: Int): Boolean {
	val fvip = findFirstVisibleItemPosition()
	if (fvip == RecyclerView.NO_POSITION) return false
	return (itemCount != 0) && (fvip > itemPosition)
}

fun LinearLayoutManager.isOnListBottom(): Boolean {
	return (itemCount != 0) && isVisiblePosition(0)
}

fun LinearLayoutManager.isOnListTop(): Boolean {
	return (itemCount != 0) && isVisiblePosition(itemCount - 1)
}