package com.example.bookchat.ui.fragment

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.animation.doOnStart
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchBinding
import com.example.bookchat.ui.activity.SearchTapResultDetailActivity
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.SearchTapStatus
import com.example.bookchat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val searchViewModel: SearchViewModel by viewModels()
    private val imm by lazy { requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val defaultTapFragment by lazy { SearchTapDefaultFragment() }
    private val historyTapFragment by lazy { SearchTapHistoryFragment() }
    private val searchingTapFragment by lazy { SearchTapSearchingFragment() }
    private val resultTapFragment by lazy { SearchTapResultFragment() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        with(binding) {
            lifecycleOwner = this@SearchFragment
            viewModel = searchViewModel
        }
        initFragmentBackStackChangedListener()
        collectSearchTapStatus()
        return binding.root
    }

    private fun initFragmentBackStackChangedListener(){
        childFragmentManager.addOnBackStackChangedListener {
            getInflatedSearchTapFragment()?.let { handleBackStackFragment(it) }
        }
    }

    private fun collectSearchTapStatus() = viewLifecycleOwner.lifecycleScope.launch {
        searchViewModel._searchTapStatus.collect { searchTapStatus ->
            Log.d(TAG, "SearchFragment: _searchTapStatus.collect - searchTapStatus : $searchTapStatus")
            handleSearchTapStatus(searchTapStatus)
        }
    }

    private fun handleSearchTapStatus(searchTapStatus: SearchTapStatus) =
        when (searchTapStatus) {
            SearchTapStatus.Default -> {
                closeSearchWindowAnimation()
                replaceFragment(defaultTapFragment, FRAGMENT_TAG_DEFAULT,false)
            }
            SearchTapStatus.History -> {
                openSearchWindowAnimation()
                replaceFragment(historyTapFragment, FRAGMENT_TAG_HISTORY,true)
            }
            SearchTapStatus.Searching -> {
                replaceFragment(searchingTapFragment, FRAGMENT_TAG_SEARCHING,true)
            }
            SearchTapStatus.Result -> {
                closeKeyboard(binding.searchEditText)
                replaceFragment(resultTapFragment, FRAGMENT_TAG_RESULT,true)
            }
            SearchTapStatus.Detail -> {
                moveToDetailActivity()
            }
        }

    private fun handleBackStackFragment(inflatedFragment :Fragment){
        when(inflatedFragment.tag){
            FRAGMENT_TAG_DEFAULT -> {
                searchViewModel._searchKeyWord.value = ""
                searchViewModel._searchTapStatus.value = SearchTapStatus.Default
            }
            FRAGMENT_TAG_HISTORY -> { searchViewModel._searchTapStatus.value = SearchTapStatus.History }
            FRAGMENT_TAG_SEARCHING -> { searchViewModel._searchTapStatus.value = SearchTapStatus.Searching }
            FRAGMENT_TAG_RESULT -> {  searchViewModel._searchTapStatus.value = SearchTapStatus.Result }
        }
    }

    private fun moveToDetailActivity() {
        val intent = Intent(requireContext(), SearchTapResultDetailActivity::class.java)
        intent.putExtra(EXTRA_SEARCH_KEYWORD, searchViewModel._searchKeyWord.value)
        startActivity(intent)
    }

    private fun replaceFragment(newFragment: Fragment, tag: String, backStackFlag :Boolean) {
        childFragmentManager.popBackStack(SEARCH_TAP_FRAGMENT_FLAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val childFragmentTransaction = childFragmentManager.beginTransaction()
        with(childFragmentTransaction) {
            setReorderingAllowed(true)
            replace(R.id.searchPage_layout, newFragment, tag)
            if (backStackFlag){
                addToBackStack(SEARCH_TAP_FRAGMENT_FLAG)
            }
            commit()
        }

    }

    /* 애니메이션 처리 전부 MotionLayout으로 마이그레이션 예정 */
    private fun openSearchWindowAnimation() {
        Log.d(TAG, "SearchFragment: openSearchWindowAnimation() - called")

        val windowAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.clicked_searchwindow_animator
        )
        windowAnimator.apply {
            setTarget(binding.searchWindow)
            binding.searchWindow.pivotX = 0.0f
            binding.searchWindow.pivotY = 0.0f
            doOnStart {
                binding.searchEditText.isEnabled = true
                binding.searchEditText.requestFocus()
                openKeyboard(binding.searchEditText)
            }
            start()
        }
        val btnAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.clicked_searchwindow_btn_animator
        )
        btnAnimator.apply {
            setTarget(binding.searchBtn)
            binding.searchBtn.pivotX = 0.0f
            binding.searchBtn.pivotY = 0.0f
            start()
        }
        val etAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.clicked_searchwindow_et_animator
        )
        etAnimator.apply {
            setTarget(binding.searchEditText)
            binding.searchEditText.pivotX = 0.0f
            binding.searchEditText.pivotY = 0.0f
            start()
        }
    }

    /* 애니메이션 처리 전부 MotionLayout으로 마이그레이션 예정 */
    private fun closeSearchWindowAnimation() {
        Log.d(TAG, "SearchFragment: closeSearchWindowAnimation() - called")

        val windowAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.unclicked_searchwindow_animator
        )
        windowAnimator.apply {
            setTarget(binding.searchWindow)
            binding.searchWindow.pivotX = 0.0f
            binding.searchWindow.pivotY = 0.0f
            doOnStart {
                binding.searchEditText.isEnabled = false
            }
            start()
        }
        val btnAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.unclicked_searchwindow_btn_animator
        )
        btnAnimator.apply {
            setTarget(binding.searchBtn)
            binding.searchBtn.pivotX = 0.0f
            binding.searchBtn.pivotY = 0.0f
            start()
        }
        val etAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.unclicked_searchwindow_et_animator
        )
        etAnimator.apply {
            setTarget(binding.searchEditText)
            binding.searchEditText.pivotX = 0.0f
            binding.searchEditText.pivotY = 0.0f
            start()
        }
    }

    private fun openKeyboard(view: View) {
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeKeyboard(editText: EditText) {
        editText.clearFocus()
        imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onResume() {
        if (searchViewModel._searchTapStatus.value == SearchTapStatus.Detail) {
            searchViewModel._searchTapStatus.value = SearchTapStatus.Result
        }
        super.onResume()
    }

    private fun getInflatedSearchTapFragment() :Fragment?{
        return childFragmentManager.fragments.firstOrNull { it.isVisible }
    }

    companion object {
        const val FRAGMENT_TAG_DEFAULT = "Default"
        const val FRAGMENT_TAG_HISTORY = "History"
        const val FRAGMENT_TAG_SEARCHING = "Searching"
        const val FRAGMENT_TAG_RESULT = "Result"
        const val EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD"
        const val SEARCH_TAP_FRAGMENT_FLAG = "SEARCH_TAP_FRAGMENT_FLAG"
    }

}