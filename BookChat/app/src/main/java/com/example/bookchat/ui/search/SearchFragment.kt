package com.example.bookchat.ui.search

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnStart
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchBinding
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.ui.createchannel.MakeChannelBookSelectDialog
import com.example.bookchat.ui.createchannel.MakeChannelSelectBookActivity
import com.example.bookchat.ui.search.SearchUiState.SearchTapState
import com.example.bookchat.ui.search.channelInfo.ChannelInfoActivity
import com.example.bookchat.ui.search.dialog.SearchBookDialog
import com.example.bookchat.ui.search.model.SearchTarget
import com.example.bookchat.ui.search.searchdetail.SearchDetailActivity
import com.example.bookchat.ui.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

	private var _binding: FragmentSearchBinding? = null
	private val binding get() = _binding!!

	private val searchViewModel by activityViewModels<SearchViewModel>()

	private lateinit var navHostFragment: NavHostFragment

	private val imm by lazy {
		requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.fragment_search, container, false
		)
		binding.lifecycleOwner = this
		binding.viewModel = searchViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initNavHost()
		observeUiState()
		observeEvent()
		initSearchBar()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchViewModel.uiState.collect { uiState ->
			handleFragment(uiState.searchTapState)
			setViewVisibility(uiState.searchTapState)
			setSearchBarState(uiState)
		}
	}

	private fun observeEvent() = viewLifecycleOwner.lifecycleScope.launch {
		searchViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initNavHost() {
		navHostFragment =
			childFragmentManager.findFragmentById(R.id.container_search) as NavHostFragment
	}

	private fun initSearchBar() {
		with(binding.searchEditText) {
			addTextChangedListener { text: Editable? ->
				val keyword = text?.toString() ?: return@addTextChangedListener
				searchViewModel.onSearchBarTextChange(keyword)
			}
			setOnEditorActionListener { _, _, _ ->
				searchViewModel.onClickSearchBtn()
				false
			}
		}
		if (searchViewModel.uiState.value.searchTapState !is SearchTapState.Default) {
			foldSearchWindowAnimation()
		}
	}

	private fun setSearchBarState(uiState: SearchUiState) {
		with(binding.searchEditText) {
			if (uiState.searchKeyword != text.toString()) {
				setText(uiState.searchKeyword)
				setSelection(uiState.searchKeyword.length)
			}
		}
	}

	private fun setViewVisibility(searchTapState: SearchTapState) {
		with(binding) {
			backBtn.visibility = if (isSearchTapDefault(searchTapState)) View.INVISIBLE else View.VISIBLE
			animationTouchEventView.visibility =
				if (isSearchTapDefault(searchTapState)) View.VISIBLE else View.INVISIBLE
			searchDeleteBtn.visibility =
				if (isSearchTapDefaultOrHistory(searchTapState)) View.INVISIBLE else View.VISIBLE
		}
	}

	private fun isSearchTapDefault(searchTapState: SearchTapState) =
		searchTapState is SearchTapState.Default

	private fun isSearchTapDefaultOrHistory(searchTapState: SearchTapState) =
		(searchTapState is SearchTapState.Default) || (searchTapState is SearchTapState.History)

	private fun handleFragment(searchTapState: SearchTapState) {
		val currentDestinationId = navHostFragment.findNavController().currentDestination?.id
		if (searchTapState.fragmentId == currentDestinationId) return

		if (currentDestinationId != null) {
			navHostFragment.findNavController().popBackStack(currentDestinationId, true)
		}
		when (searchTapState) {
			is SearchTapState.Default -> {
				expandSearchWindowAnimation()
				navHostFragment.findNavController().navigate(searchTapState.fragmentId)
			}

			is SearchTapState.History -> {
				foldSearchWindowAnimation()
				navHostFragment.findNavController().navigate(searchTapState.fragmentId)
			}

			is SearchTapState.Searching -> {
				navHostFragment.findNavController().navigate(searchTapState.fragmentId)
			}

			is SearchTapState.Result -> {
				closeKeyboard()
				navHostFragment.findNavController().navigate(searchTapState.fragmentId)
			}
		}
	}

	private val detailActivityLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == AppCompatActivity.RESULT_OK) {
				val intent = result.data
				val selectedBookIsbn = intent?.getStringExtra(EXTRA_SELECTED_BOOK_ISBN)
					?: return@registerForActivityResult
				finishWithSelectedChannelBook(selectedBookIsbn)
			}
		}

	private fun moveToDetail(
		searchKeyword: String,
		searchTarget: SearchTarget,
		searchPurpose: SearchPurpose,
		searchFilter: SearchFilter
	) {
		val intent = Intent(requireActivity(), SearchDetailActivity::class.java)
		intent.putExtra(EXTRA_SEARCH_KEYWORD, searchKeyword)
		intent.putExtra(EXTRA_SEARCH_PURPOSE, searchPurpose)
		intent.putExtra(EXTRA_SEARCH_TARGET, searchTarget)
		intent.putExtra(EXTRA_SEARCH_FILTER, searchFilter)
		detailActivityLauncher.launch(intent)
	}

	/* 애니메이션 처리 전부 MotionLayout으로 마이그레이션 예정 */
	private fun foldSearchWindowAnimation() {
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
	private fun expandSearchWindowAnimation() {
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

	private fun closeKeyboard() {
		binding.searchEditText.clearFocus()
		imm.hideSoftInputFromWindow(
			binding.searchEditText.windowToken,
			InputMethodManager.HIDE_NOT_ALWAYS
		)
	}

	private fun moveToSearchTapBookDialog(book: Book) {
		val dialog = SearchBookDialog()
		dialog.arguments = bundleOf(EXTRA_SEARCHED_BOOK_ITEM_ID to book.isbn)
		dialog.show(childFragmentManager, DIALOG_TAG_SEARCH_BOOK)
	}

	private fun moveToMakeChannelSelectBookDialog(book: Book) {
		val dialog = MakeChannelBookSelectDialog(
			onClickMakeChannel = { finishWithSelectedChannelBook(book.isbn) },
			selectedBook = book
		)
		dialog.show(childFragmentManager, DIALOG_TAG_SELECT_BOOK)
	}

	private fun finishWithSelectedChannelBook(bookIsbn: String) {
		when (val parentActivity = requireActivity()) {
			is MakeChannelSelectBookActivity -> parentActivity.finishBookSelect(bookIsbn)
		}
	}

	private fun moveToChannelInfo(channelId: Long) {
		val intent = Intent(requireContext(), ChannelInfoActivity::class.java)
		intent.putExtra(EXTRA_CLICKED_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun handleEvent(event: SearchEvent) {
		when (event) {
			is SearchEvent.MoveToDetail -> moveToDetail(
				searchKeyword = event.searchKeyword,
				searchTarget = event.searchTarget,
				searchPurpose = event.searchPurpose,
				searchFilter = event.searchFilter
			)

			is SearchEvent.MoveToSearchBookDialog -> moveToSearchTapBookDialog(event.book)
			is SearchEvent.MoveToMakeChannelSelectBookDialog ->
				moveToMakeChannelSelectBookDialog(event.book)

			is SearchEvent.MoveToChannelInfo -> moveToChannelInfo(event.channelId)
			is SearchEvent.MakeToast -> makeToast(event.stringId)
		}
	}

	companion object {
		const val EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD"
		const val EXTRA_SEARCH_PURPOSE = "EXTRA_SEARCH_PURPOSE"
		const val EXTRA_SEARCH_TARGET = "EXTRA_NECESSARY_DATA"
		const val EXTRA_SEARCH_FILTER = "EXTRA_CHAT_SEARCH_FILTER"

		const val EXTRA_SEARCHED_BOOK_ITEM_ID = "EXTRA_SEARCHED_BOOK_ITEM_ID"
		const val DIALOG_TAG_SEARCH_BOOK = "DIALOG_TAG_SEARCH_BOOK"
		const val DIALOG_TAG_SELECT_BOOK = "DIALOG_TAG_SELECT_BOOK"
		const val EXTRA_CLICKED_CHANNEL_ID = "EXTRA_CLICKED_CHANNEL_ID"
	}
}