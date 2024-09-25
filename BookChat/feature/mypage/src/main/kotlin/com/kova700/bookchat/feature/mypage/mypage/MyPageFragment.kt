package com.kova700.bookchat.feature.mypage.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.feature.mypage.databinding.FragmentMyPageBinding
import com.kova700.bookchat.feature.mypage.notice.NoticeActivity
import com.kova700.bookchat.feature.mypage.policy.PolicyActivity
import com.kova700.bookchat.feature.mypage.setting.setting.SettingActivity
import com.kova700.bookchat.feature.mypage.terms.TermsActivity
import com.kova700.bookchat.feature.mypage.useredit.UserEditActivity
import com.kova700.bookchat.util.image.image.loadUserProfile
import com.kova700.bookchat.util.version.versionName
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
		_binding = FragmentMyPageBinding.inflate(inflater, container, false)
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
			termsOfServiceBtn.setOnClickListener { myPageViewModel.onClickTermsBtn() }
			privacyPolicyBtn.setOnClickListener { myPageViewModel.onClickPolicyBtn() }
			openSourceLicenseBtn.setOnClickListener { myPageViewModel.onClickOpenSourceLicense() }
			userEditBtn.setOnClickListener { myPageViewModel.onClickUserEditBtn() }
			appVersionTv.text = requireContext().versionName
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

	private fun moveToTerms() {
		val intent = Intent(requireContext(), TermsActivity::class.java)
		startActivity(intent)
	}

	private fun moveToPolicy() {
		val intent = Intent(requireContext(), PolicyActivity::class.java)
		startActivity(intent)
	}

	private fun handleEvent(event: MyPageEvent) {
		when (event) {
			MyPageEvent.MoveToUserEditPage -> moveToUserEditActivity()
			MyPageEvent.MoveToSetting -> moveToSetting()
			MyPageEvent.MoveToNotice -> moveToNotice()
			MyPageEvent.MoveToTerms -> moveToTerms()
			MyPageEvent.MoveToPrivacyPolicy -> moveToPolicy()
			MyPageEvent.MoveToOpenSourceLicense -> moveToOpenSourceLicensesMenu()
		}
	}

}