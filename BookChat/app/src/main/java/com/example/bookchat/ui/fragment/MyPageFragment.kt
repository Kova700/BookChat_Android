package com.example.bookchat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentMyPageBinding
import com.example.bookchat.ui.activity.LoginActivity
import com.example.bookchat.viewmodel.MyPageViewModel
import com.example.bookchat.viewmodel.MyPageViewModel.MyPageEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MyPageFragment : Fragment()  {

    private lateinit var binding : FragmentMyPageBinding
    private lateinit var myPageViewModel: MyPageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_page,container,false)
        myPageViewModel = ViewModelProvider(this, ViewModelFactory()).get(MyPageViewModel::class.java)
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

    private fun handleEvent(event : MyPageEvent){
        when(event){
            MyPageEvent.MoveToLoginPage -> moveToLoginActivity()
        }
    }

}