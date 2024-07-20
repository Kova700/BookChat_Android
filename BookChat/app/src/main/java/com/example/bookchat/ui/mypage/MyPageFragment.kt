package com.example.bookchat.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentMyPageBinding
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.mypage.MyPageViewModel.MyPageEvent
import com.example.bookchat.ui.mypage.accountsetting.AccountSettingActivity
import com.example.bookchat.ui.mypage.appsetting.AppSettingActivity
import com.example.bookchat.ui.mypage.useredit.UserEditActivity
import com.example.bookchat.utils.image.loadUserProfile
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
		binding.viewmodel = myPageViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		observeUiState()
		observeUiEvent()
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

	private fun setViewState(uiState: User) {
		binding.userProfileIv.loadUserProfile(
			imageUrl = uiState.profileImageUrl,
			userDefaultProfileType = uiState.defaultProfileImageType
		)
	}

	private fun moveToUserEditActivity() {
		val intent = Intent(requireContext(), UserEditActivity::class.java)
		startActivity(intent)
	}

	private fun moveToWishActivity() {
		val intent = Intent(requireContext(), WishActivity::class.java)
		startActivity(intent)
	}

	private fun moveToNoticeActivity() {
		val intent = Intent(requireContext(), NoticeActivity::class.java)
		startActivity(intent)
	}

	private fun moveToAccountSettingActivity() {
		val intent = Intent(requireContext(), AccountSettingActivity::class.java)
		startActivity(intent)
	}

	private fun moveToAppSettingActivity() {
		val intent = Intent(requireContext(), AppSettingActivity::class.java)
		startActivity(intent)
	}

	private fun handleEvent(event: MyPageEvent) {
		when (event) {
			is MyPageEvent.MoveToUserEditPage -> moveToUserEditActivity()
			is MyPageEvent.MoveToWish -> moveToWishActivity()
			is MyPageEvent.MoveToNotice -> moveToNoticeActivity()
			is MyPageEvent.MoveToAccountSetting -> moveToAccountSettingActivity()
			is MyPageEvent.MoveToAppSetting -> moveToAppSettingActivity()
		}
	}

}