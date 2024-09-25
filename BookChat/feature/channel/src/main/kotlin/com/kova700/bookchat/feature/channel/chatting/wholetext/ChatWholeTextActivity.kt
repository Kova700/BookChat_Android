package com.kova700.bookchat.feature.channel.chatting.wholetext

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.feature.channel.databinding.ActivityChatWholeTextBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatWholeTextActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChatWholeTextBinding
	private val chatWholeTextViewmodel by viewModels<ChatWholeTextViewmodel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityChatWholeTextBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		chatWholeTextViewmodel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		chatWholeTextViewmodel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(uiState: ChatWholeTextUiState) {
		with(binding) {
			chatMessageTv.text = uiState.chatMessage
		}
	}

	private fun initViewState() {
		with(binding) {
			backBtn.setOnClickListener { chatWholeTextViewmodel.onClickBackBtn() }
		}
	}

	private fun handleEvent(state: ChatWholeTextEvent) {
		when (state) {
			ChatWholeTextEvent.MoveBack -> finish()
		}
	}
}