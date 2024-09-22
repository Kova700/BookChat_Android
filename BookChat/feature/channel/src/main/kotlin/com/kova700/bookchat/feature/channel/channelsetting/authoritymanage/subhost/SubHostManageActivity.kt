package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.adapter.SubHostManageViewPagerAdapter
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.subhostadd.SubHostAddFragment
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.subhostdelete.SubHostDeleteFragment
import com.kova700.bookchat.feature.channel.databinding.ActivitySubHostManageBinding
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubHostManageActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySubHostManageBinding

	private val subHostManageViewModel: SubHostManageViewModel by viewModels()

	private val fragments: List<Fragment> =
		listOf(SubHostDeleteFragment(), SubHostAddFragment())

	private lateinit var viewPagerAdapter: SubHostManageViewPagerAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySubHostManageBinding.inflate(layoutInflater)
		setContentView(binding.root)
		viewPagerAdapter = SubHostManageViewPagerAdapter(fragments, this)
		setBackPressedDispatcher()
		initViewState()
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		subHostManageViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		with(binding) {
			subHostManageVp.adapter = viewPagerAdapter
			subHostManageVp.isUserInputEnabled = false
		}
	}

	private fun changeTab(tabIndex: Int) {
		binding.subHostManageVp.currentItem = tabIndex
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			if (binding.subHostManageVp.currentItem != SUB_HOST_DELETE_INDEX) {
				changeTab(binding.subHostManageVp.currentItem - 1)
				return@addCallback
			}
			finish()
		}
	}

	private fun handleEvent(event: SubHostManageUiEvent) {
		when (event) {
			is SubHostManageUiEvent.ShowSnackBar -> binding.root.showSnackBar(textId = event.stringId)
			SubHostManageUiEvent.MoveAddSubHost -> changeTab(SUB_HOST_ADD_INDEX)
			SubHostManageUiEvent.MoveBack -> finish()
			SubHostManageUiEvent.MoveDeleteSubHost -> changeTab(SUB_HOST_DELETE_INDEX)
		}
	}

	companion object {
		private const val SUB_HOST_DELETE_INDEX = 0
		private const val SUB_HOST_ADD_INDEX = 1
	}
}