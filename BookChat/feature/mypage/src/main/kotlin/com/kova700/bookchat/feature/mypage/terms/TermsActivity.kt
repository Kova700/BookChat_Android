package com.kova700.bookchat.feature.mypage.terms

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kova700.bookchat.feature.mypage.databinding.ActivityTermsBinding

class TermsActivity : AppCompatActivity() {
	private lateinit var binding: ActivityTermsBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTermsBinding.inflate(layoutInflater)
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
			"https://sleepy-loganberry-b5f.notion.site/f77ea90170a34e97825caee1db3c9d84?pvs=4"
	}
}