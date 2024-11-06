package com.kova700.bookchat.feature.signup.selecttaste

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.navigation.MainNavigator
import com.kova700.bookchat.feature.signup.databinding.ActivitySelectTasteBinding
import com.kova700.bookchat.util.image.bitmap.compressToByteArray
import com.kova700.bookchat.util.image.bitmap.getImageBitmap
import com.kova700.bookchat.util.image.image.deleteImageCache
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectTasteActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySelectTasteBinding
	private val selectTasteViewModel: SelectTasteViewModel by viewModels()

	@Inject
	lateinit var mainNavigator: MainNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySelectTasteBinding.inflate(layoutInflater)
		setContentView(binding.root)
		getCroppedImageBitmap()
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		selectTasteViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		selectTasteViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun getCroppedImageBitmap() = lifecycleScope.launch {
		val uri = intent.getStringExtra(EXTRA_USER_PROFILE_URI) ?: return@launch
		val userProfileByteArray = uri.getImageBitmap(context = this@SelectTasteActivity)
			?.compressToByteArray()
		selectTasteViewModel.onUpdatedUserProfileImage(userProfileByteArray)
		deleteImageCache(uri)
	}

	private fun initViewState() {
		with(binding) {
			startBookchatBtn.setOnClickListener { selectTasteViewModel.onClickSignUpBtn() }
			backBtn.setOnClickListener { selectTasteViewModel.onClickBackBtn() }
			economyTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.ECONOMY) }
			philosophyTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.PHILOSOPHY) }
			historyTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.HISTORY) }
			travelTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.TRAVEL) }
			healthTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.HEALTH) }
			hobbyTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.HOBBY) }
			humanitiesTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.HUMANITIES) }
			novelTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.NOVEL) }
			artTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.ART) }
			designTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.DESIGN) }
			developmentTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.DEVELOPMENT) }
			scienceTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.SCIENCE) }
			magazineTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.MAGAZINE) }
			religionTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.RELIGION) }
			characterTasteBtn.setOnClickListener { selectTasteViewModel.onClickTasteBtn(ReadingTaste.CHARACTER) }
		}
	}

	private fun setViewState(uiState: SelectTasteState) {
		setStartButtonState(uiState)
		binding.progressBar.visibility = if (uiState.isLoading) VISIBLE else GONE
	}

	private fun setStartButtonState(uiState: SelectTasteState) {
		with(binding.startBookchatBtn) {
			if (uiState.readingTastes.isEmpty()) {
				setBackgroundColor(Color.parseColor("#D9D9D9"))
				isEnabled = false
				return
			}
			setBackgroundColor(Color.parseColor("#5648FF"))
			isEnabled = true
		}
	}

	private fun moveToMain() {
		mainNavigator.navigate(
			currentActivity = this,
			intentAction = {
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			},
			shouldFinish = true
		)
	}

	private fun handleEvent(event: SelectTasteEvent) = when (event) {
		is SelectTasteEvent.MoveToBack -> finish()
		is SelectTasteEvent.MoveToMain -> moveToMain()

		is SelectTasteEvent.ErrorEvent -> binding.selectTasteLayout.showSnackBar(event.stringId)
		is SelectTasteEvent.UnknownErrorEvent -> binding.selectTasteLayout.showSnackBar(event.message)
	}

	companion object {
		const val EXTRA_SIGNUP_USER_NICKNAME = "EXTRA_SIGNUP_USER_NICKNAME"
		const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
	}
}