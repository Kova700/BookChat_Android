package com.kova700.bookchat.feature.channel.chatting.capture

import android.view.View
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.channel.chatting.model.ChatItem

fun setCaptureViewState(
	isCaptureMode: Boolean,
	chatItem: ChatItem,
	captureLayoutView: View,
	rootLayout: View,
) {
	if (isCaptureMode.not()) {
		captureLayoutView.visibility = View.GONE
		rootLayout.setBackgroundResource(R.drawable.chat_default_layout)
		return
	}

	when {
		chatItem.isCaptureHeader && chatItem.isCaptureBottom -> {
			captureLayoutView.visibility = View.GONE
			rootLayout.setBackgroundResource(R.drawable.chat_capture_only_one_layout)
		}

		chatItem.isCaptureHeader -> {
			captureLayoutView.visibility = View.GONE
			rootLayout.setBackgroundResource(R.drawable.chat_capture_header_layout)
		}

		chatItem.isCaptureMiddle -> {
			captureLayoutView.visibility = View.GONE
			rootLayout.setBackgroundResource(R.drawable.chat_capture_middle_layout)
		}

		chatItem.isCaptureBottom -> {
			captureLayoutView.visibility = View.GONE
			rootLayout.setBackgroundResource(R.drawable.chat_capture_bottom_layout)
		}

		else -> {
			captureLayoutView.visibility = View.VISIBLE
			rootLayout.setBackgroundResource(R.drawable.chat_default_layout)
		}
	}
}