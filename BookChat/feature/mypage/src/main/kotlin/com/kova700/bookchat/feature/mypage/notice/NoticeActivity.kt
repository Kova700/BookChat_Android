package com.kova700.bookchat.feature.mypage.notice

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kova700.bookchat.feature.mypage.databinding.ActivityNoticeBinding

class NoticeActivity : AppCompatActivity() {
	private lateinit var binding: ActivityNoticeBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityNoticeBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initViewState()
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { finish() }
		binding.webview.apply {
			webViewClient = WebViewClient()
			settings.builtInZoomControls = true
			settings.domStorageEnabled = true
			settings.javaScriptEnabled = true
			scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_INSET
			loadUrl(NOTICE_URL)
		}
	}

	companion object {
		private const val NOTICE_URL =
			"https://sleepy-loganberry-b5f.notion.site/f63139c7f1924ad39b365a9c08ffc0ea"
	}
}