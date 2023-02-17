package com.example.bookchat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentMyPageBinding
import com.example.bookchat.ui.activity.LoginActivity
import com.example.bookchat.ui.activity.UserEditActivity
import com.example.bookchat.viewmodel.MyPageViewModel
import com.example.bookchat.viewmodel.MyPageViewModel.MyPageEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment()  {

    private lateinit var binding : FragmentMyPageBinding
    private val myPageViewModel: MyPageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_page,container,false)
        binding.lifecycleOwner = this
        binding.viewmodel = myPageViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeEvent()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun observeEvent(){
        lifecycleScope.launch {
            myPageViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun moveToLoginActivity(){
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK //새로운 태스크 생성
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // 실행 액티비티 외 모두 제거
        startActivity(intent)
    }

    private fun moveToUserEditActivity(){
        val intent = Intent(requireContext(), UserEditActivity::class.java)
        startActivity(intent)
    }

    private fun handleEvent(event : MyPageEvent){
        when(event){
            is MyPageEvent.MoveToLoginPage -> moveToLoginActivity()
            is MyPageEvent.MoveToUserEditPage -> moveToUserEditActivity()
        }
    }

}