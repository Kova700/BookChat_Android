package com.example.bookchat.ui.fragment

import android.animation.AnimatorInflater
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchBinding
import com.example.bookchat.utils.Constants.TAG

class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    var isclickedSearchWindow = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.fragment = this@SearchFragment
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        //검색창 클릭되면 애니메이션 작동
        //뒤로가기버튼 Visible
    }

    fun clickSearchWindow(){
        Log.d(TAG, "SearchFragment: clickedSearchWindow() - called")
        if (isclickedSearchWindow) return
        isclickedSearchWindow = true

        val windowAnimator = AnimatorInflater.loadAnimator(requireContext(),R.animator.clicked_searchwindow_animator)
        windowAnimator.apply {
            setTarget(binding.searhWindow)
            binding.searhWindow.pivotX = 0.0f
            binding.searhWindow.pivotY = 0.0f
            binding.backBtn.visibility = VISIBLE
            start()
        }
        val btnAnimator = AnimatorInflater.loadAnimator(requireContext(),R.animator.clicked_searchwindow_btn_animator)
        btnAnimator.apply {
            setTarget(binding.searchBtn)
            binding.searchBtn.pivotX = 0.0f
            binding.searchBtn.pivotY = 0.0f
            start()
        }
        val etAnimator = AnimatorInflater.loadAnimator(requireContext(),R.animator.clicked_searchwindow_et_animator)
        etAnimator.apply {
            setTarget(binding.searchEditText)
            binding.searchEditText.pivotX = 0.0f
            binding.searchEditText.pivotY = 0.0f
            doOnEnd { binding.searchEditText.isEnabled = true }
            start()
        }
    }

    fun clickBackBtn(){
        Log.d(TAG, "SearchFragment: clickBackBtn() - called")
        if (!isclickedSearchWindow) return
        isclickedSearchWindow = false

        val windowAnimator = AnimatorInflater.loadAnimator(requireContext(),R.animator.unclicked_searchwindow_animator)
        windowAnimator.apply {
            setTarget(binding.searhWindow)
            binding.searhWindow.pivotX = 0.0f
            binding.searhWindow.pivotY = 0.0f
            doOnStart {
                binding.backBtn.visibility = INVISIBLE
                binding.searchEditText.setText("")
                binding.searchEditText.isEnabled = false
            }
            start()
        }
        val btnAnimator = AnimatorInflater.loadAnimator(requireContext(),R.animator.unclicked_searchwindow_btn_animator)
        btnAnimator.apply {
            setTarget(binding.searchBtn)
            binding.searchBtn.pivotX = 0.0f
            binding.searchBtn.pivotY = 0.0f
            start()
        }
        val etAnimator = AnimatorInflater.loadAnimator(requireContext(),R.animator.unclicked_searchwindow_et_animator)
        etAnimator.apply {
            setTarget(binding.searchEditText)
            binding.searchEditText.pivotX = 0.0f
            binding.searchEditText.pivotY = 0.0f
            start()
        }

    }
}