package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySubHostManageBinding
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.adapter.SubHostManageViewPagerAdapter
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostadd.SubHostAddFragment
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostdelete.SubHostDeleteFragment
import com.example.bookchat.utils.makeToast
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
		binding = DataBindingUtil.setContentView(this, R.layout.activity_sub_host_manage)
		viewPagerAdapter = SubHostManageViewPagerAdapter(fragments, this)
		binding.viewmodel = subHostManageViewModel
		binding.lifecycleOwner = this
		binding.subHostManageVp.adapter = viewPagerAdapter
		setBackPressedDispatcher()
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		subHostManageViewModel.eventFlow.collect { event -> handleEvent(event) }
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
			is SubHostManageUiEvent.MakeToast -> makeToast(event.stringId)
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