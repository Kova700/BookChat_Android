package com.example.bookchat.ui.mypage.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.BuildConfig
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentMyPageBinding
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.mypage.notice.NoticeActivity
import com.example.bookchat.ui.mypage.setting.setting.SettingActivity
import com.example.bookchat.ui.mypage.useredit.UserEditActivity
import com.example.bookchat.utils.image.loadUserProfile
import com.mikepenz.aboutlibraries.LibsBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment() {
	private var _binding: FragmentMyPageBinding? = null
	private val binding get() = _binding!!
	private val myPageViewModel by activityViewModels<MyPageViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.fragment_my_page, container, false
		)
		binding.lifecycleOwner = this
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		myPageViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		myPageViewModel.eventFlow.collect { handleEvent(it) }
	}

	private fun initViewState() {
		with(binding) {
			settingBtn.setOnClickListener { myPageViewModel.onClickSettingBtn() }
			noticeBtn.setOnClickListener { myPageViewModel.onClickNoticeBtn() }
			inviteBtn.setOnClickListener { }
			openSourceLicenseBtn.setOnClickListener { moveToOpenSourceLicensesMenu() }
			userEditBtn.setOnClickListener { myPageViewModel.onClickUserEditBtn() }
			appVersionTv.text = BuildConfig.VERSION_NAME
		}
	}

	private fun setViewState(uiState: User) {
		binding.userProfileIv.loadUserProfile(
			imageUrl = uiState.profileImageUrl,
			userDefaultProfileType = uiState.defaultProfileImageType
		)
		binding.nicknameTv.text = uiState.nickname
	}

	private fun moveToOpenSourceLicensesMenu() {
		LibsBuilder()
			.withAboutIconShown(true)
			.withAboutVersionShown(true)
			.start(requireContext())
	}

	private fun moveToUserEditActivity() {
		val intent = Intent(requireContext(), UserEditActivity::class.java)
		startActivity(intent)
	}

	private fun moveToSetting() {
		val intent = Intent(requireContext(), SettingActivity::class.java)
		startActivity(intent)
	}

	private fun moveToNotice() {
		val intent = Intent(requireContext(), NoticeActivity::class.java)
		startActivity(intent)
	}

	private fun handleEvent(event: MyPageEvent) {
		when (event) {
			is MyPageEvent.MoveToUserEditPage -> moveToUserEditActivity()
			is MyPageEvent.MoveToSetting -> moveToSetting()
			is MyPageEvent.MoveToNotice -> moveToNotice()
		}
	}

}