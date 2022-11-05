package com.example.bookchat.ui.fragment

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchBinding
import com.example.bookchat.ui.activity.SearchTapResultDetailActivity
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.SearchTapStatus
import com.example.bookchat.viewmodel.SearchViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchViewModel: SearchViewModel
    private val imm by lazy { requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val defaultTapFragment by lazy { SearchTapDefaultFragment() }
    private val historyTapFragment by lazy { SearchTapHistoryFragment() }
    private val searchingTapFragment by lazy { SearchTapSearchingFragment() }
    private val resultTapFragment by lazy { SearchTapResultFragment() }

    var isClickedSearchWindow = false //이거 뷰 디스트로이 됐다가 다시 나타나면 터시 안됨 수정필요함

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        searchViewModel =
            ViewModelProvider(this, ViewModelFactory()).get(SearchViewModel::class.java)
        with(binding) {
            lifecycleOwner = this@SearchFragment
            fragment = this@SearchFragment //viewModel로 옮길 수 있으면 다 옮기기
            viewModel = searchViewModel
        }

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        collectSearchTapStatus()
        super.onViewCreated(view, savedInstanceState)
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
                replaceFragment(defaultTapFragment, FRAGMENT_TAG_DEFAULT)
            }
            SearchTapStatus.History -> {
                replaceFragment(historyTapFragment, FRAGMENT_TAG_HISTORY)
            }
            SearchTapStatus.Searching -> {
                replaceFragment(searchingTapFragment, FRAGMENT_TAG_SEARCHING)
            }
            SearchTapStatus.Result -> {
                closeKeyboard(binding.searchEditText)
                replaceFragment(resultTapFragment, FRAGMENT_TAG_RESULT)
            }
            SearchTapStatus.Detail -> {
                moveToDetailActivity()
            }
        }

    private fun moveToDetailActivity() {
        Log.d(TAG, "SearchFragment: moveToDetailActivity() - called")
        val intent = Intent(requireContext(), SearchTapResultDetailActivity::class.java)
        intent.putExtra(EXTRA_SEARCH_KEYWORD, searchViewModel._searchKeyWord.value)
        intent.putExtra(
            EXTRA_SEARCH_RESULT_ITEM_COUNT,
            searchViewModel.bookSearchResultTotalItemCount
        )
        startActivity(intent)
    }

    private fun replaceFragment(newFragment: Fragment, tag: String) {
        val childFragmentTransaction = childFragmentManager.beginTransaction()
        with(childFragmentTransaction) {
            setReorderingAllowed(true)
            replace(R.id.searchPage_layout, newFragment, tag)
            commit()
        }
    }

    /* 애니메이션 처리 전부 MotionLayout으로 마이그레이션 예정 */
    fun clickSearchWindow() {
        Log.d(TAG, "SearchFragment: clickedSearchWindow() - called")
        if (isClickedSearchWindow) return
        isClickedSearchWindow = true
        searchViewModel._searchTapStatus.value = SearchTapStatus.History

        val windowAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.clicked_searchwindow_animator
        )
        windowAnimator.apply {
            setTarget(binding.searchWindow)
            binding.searchWindow.pivotX = 0.0f
            binding.searchWindow.pivotY = 0.0f
            binding.backBtn.visibility = VISIBLE
            doOnStart {
                binding.searchEditText.isEnabled = true
                binding.searchEditText.requestFocus()
                openKeyboard(binding.searchEditText)
                binding.animationTouchEventView.visibility = INVISIBLE
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
    fun clickBackBtn() {
        Log.d(TAG, "SearchFragment: clickBackBtn() - called")
        if (!isClickedSearchWindow) return

        val windowAnimator = AnimatorInflater.loadAnimator(
            requireContext(),
            R.animator.unclicked_searchwindow_animator
        )
        windowAnimator.apply {
            setTarget(binding.searchWindow)
            binding.searchWindow.pivotX = 0.0f
            binding.searchWindow.pivotY = 0.0f
            doOnStart {
                binding.backBtn.visibility = INVISIBLE
                binding.searchEditText.setText("")
                isClickedSearchWindow = false
                searchViewModel._searchTapStatus.value = SearchTapStatus.Default
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
            doOnEnd { binding.animationTouchEventView.visibility = VISIBLE }
            start()
        }
    }

    fun clearSearchWindow() {
        binding.searchEditText.setText("")
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

    companion object {
        const val FRAGMENT_TAG_DEFAULT = "Default"
        const val FRAGMENT_TAG_HISTORY = "History"
        const val FRAGMENT_TAG_SEARCHING = "Searching"
        const val FRAGMENT_TAG_RESULT = "Result"
        const val EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD"
        const val EXTRA_SEARCH_RESULT_ITEM_COUNT = "EXTRA_SEARCH_RESULT_ITEM_COUNT"
    }

}