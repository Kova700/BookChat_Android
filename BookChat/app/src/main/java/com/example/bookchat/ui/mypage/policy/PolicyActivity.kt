package com.example.bookchat.ui.mypage.policy

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.bookchat.databinding.ActivityPolicyBinding

class PolicyActivity : AppCompatActivity() {
	private lateinit var binding: ActivityPolicyBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityPolicyBinding.inflate(layoutInflater)
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
			"https://sleepy-loganberry-b5f.notion.site/c175a25634f64ef890fb13af3b6ceba6?pvs=4"
	}
}