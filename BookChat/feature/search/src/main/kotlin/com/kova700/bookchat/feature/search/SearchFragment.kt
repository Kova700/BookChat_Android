package com.kova700.bookchat.feature.search

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.MakeChannelActivityNavigator
import com.kova700.bookchat.feature.search.SearchUiState.SearchTapState
import com.kova700.bookchat.feature.search.channelInfo.ChannelInfoActivity
import com.kova700.bookchat.feature.search.createchannel.MakeChannelBookSelectDialog
import com.kova700.bookchat.feature.search.createchannel.MakeChannelSelectBookActivity
import com.kova700.bookchat.feature.search.databinding.FragmentSearchBinding
import com.kova700.bookchat.feature.search.dialog.SearchBookDialog
import com.kova700.bookchat.feature.search.dialog.SearchFilterSelectDialog
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchTarget
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailActivity
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailActivity.Companion.EXTRA_SELECTED_BOOK_ISBN
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kova700.bookchat.feature.search.R as searchR

@AndroidEntryPoint
class SearchFragment : Fragment() {

	private var _binding: FragmentSearchBinding? = null
	private val binding get() = _binding!!

	private val searchViewModel by activityViewModels<SearchViewModel>()

	private lateinit var navHostFragment: NavHostFragment
	private lateinit var navController: NavController

	@Inject
	lateinit var makeChannelNavigator: MakeChannelActivityNavigator

	private val imm by lazy {
		requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentSearchBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initNavHost()
		observeUiState()
		observeEvent()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchViewModel.uiState.collect { uiState ->
			handleFragment(uiState.searchTapState)
			setViewState(uiState)
		}
	}

	private fun observeEvent() = viewLifecycleOwner.lifecycleScope.launch {
		searchViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initNavHost() {
		navHostFragment =
			childFragmentManager.findFragmentById(searchR.id.container_search) as NavHostFragment
		navController = navHostFragment.findNavController()
	}

	private fun initViewState() {
		initSearchBar()
		with(binding) {
			backBtn.setOnClickListener { searchViewModel.onClickBackBtn() }
			animationTouchEventView.setOnClickListener { searchViewModel.onClickSearchBar() }
			searchBtn.setOnClickListener { searchViewModel.onClickSearchBtn() }
			searchKeywordClearBtn.setOnClickListener { searchViewModel.onClickKeywordClearBtn() }
			searchFilterBtn.setOnClickListener { searchViewModel.onClickSearchFilterBtn() }
		}
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

	private fun setViewState(uiState: SearchUiState) {
		setViewVisibility(uiState.searchTapState)
		setSearchBarState(uiState)
		setSearchFilterBtnState(uiState)
	}

	private fun setSearchBarState(uiState: SearchUiState) {
		with(binding.searchEditText) {
			if (uiState.searchKeyword != text.toString()) {
				setText(uiState.searchKeyword)
				setSelection(uiState.searchKeyword.length)
			}
		}
	}

	private fun setSearchFilterBtnState(uiState: SearchUiState) {
		with(binding.searchFilterBtn) {
			if (uiState.searchFilter != SearchFilter.BOOK_TITLE) setImageResource(R.drawable.search_filter_blue_icon)
			else setImageResource(R.drawable.search_filter_black_icon)
		}
	}

	private fun setViewVisibility(searchTapState: SearchTapState) {
		with(binding) {
			backBtn.visibility = if (searchTapState.isDefault) View.INVISIBLE else View.VISIBLE
			animationTouchEventView.visibility =
				if (searchTapState.isDefault) View.VISIBLE else View.INVISIBLE
			searchFilterBtn.visibility =
				if (searchTapState.isDefault) View.VISIBLE else View.INVISIBLE
			searchKeywordClearBtn.visibility =
				if (searchTapState.isDefaultOrHistory) View.INVISIBLE else View.VISIBLE
		}
	}

	private fun handleFragment(searchTapState: SearchTapState) {
		val currentDestinationId = navHostFragment.findNavController().currentDestination?.id
		if (searchTapState.fragmentId == currentDestinationId) return

		if (currentDestinationId != null) {
			navController.popBackStack(currentDestinationId, true)
		}
		when (searchTapState) {
			is SearchTapState.Default -> {
				expandSearchWindowAnimation()
				navController.navigate(searchTapState.fragmentId)
			}

			is SearchTapState.History -> {
				foldSearchWindowAnimation()
				navController.navigate(searchTapState.fragmentId)
			}

			is SearchTapState.Searching -> {
				navController.navigate(searchTapState.fragmentId)
			}

			is SearchTapState.Result -> {
				closeKeyboard()
				navController.navigate(searchTapState.fragmentId)
			}
		}
	}

	private val detailActivityLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == AppCompatActivity.RESULT_OK) {
				val selectedBookIsbn = result.data?.getStringExtra(EXTRA_SELECTED_BOOK_ISBN)
					?: return@registerForActivityResult
				finishWithSelectedChannelBook(selectedBookIsbn)
			}
		}

	private fun moveToDetail(
		searchKeyword: String,
		searchTarget: SearchTarget,
		searchPurpose: SearchPurpose,
		searchFilter: SearchFilter,
	) {
		val intent = Intent(requireActivity(), SearchDetailActivity::class.java)
		intent.putExtra(EXTRA_SEARCH_KEYWORD, searchKeyword)
		intent.putExtra(EXTRA_SEARCH_PURPOSE, searchPurpose)
		intent.putExtra(EXTRA_SEARCH_TARGET, searchTarget)
		intent.putExtra(EXTRA_SEARCH_FILTER, searchFilter)
		detailActivityLauncher.launch(intent)
	}

	private fun applyAnimation(target: View, animatorResId: Int, onStart: () -> Unit) {
		target.pivotX = 0f
		target.pivotY = 0f
		val animator = AnimatorInflater.loadAnimator(requireContext(), animatorResId)
		animator.apply {
			setTarget(target)
			doOnStart { onStart() }
			start()
		}
	}

	private fun foldSearchWindowAnimation() {
		applyAnimation(binding.searchWindow, searchR.animator.clicked_searchwindow_animator) {
			binding.searchEditText.isEnabled = true
			binding.searchEditText.requestFocus()
			openKeyboard(binding.searchEditText)
		}
		applyAnimation(binding.searchBtn, searchR.animator.clicked_searchwindow_btn_animator) {}
		applyAnimation(binding.searchEditText, searchR.animator.clicked_searchwindow_et_animator) {}
	}

	private fun expandSearchWindowAnimation() {
		applyAnimation(binding.searchWindow, searchR.animator.unclicked_searchwindow_animator) {
			binding.searchEditText.isEnabled = false
		}
		applyAnimation(binding.searchBtn, searchR.animator.unclicked_searchwindow_btn_animator) {}
		applyAnimation(binding.searchEditText, searchR.animator.unclicked_searchwindow_et_animator) {}
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

	private fun showSearchTapBookDialog(book: Book) {
		val existingFragment = childFragmentManager.findFragmentByTag(DIALOG_TAG_SEARCH_BOOK)
		if (existingFragment != null) return
		val dialog = SearchBookDialog()
		dialog.arguments = bundleOf(EXTRA_SEARCHED_BOOK_ITEM_ID to book.isbn)
		dialog.show(childFragmentManager, DIALOG_TAG_SEARCH_BOOK)
	}

	private fun showMakeChannelSelectBookDialog(book: Book) {
		val existingFragment = childFragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_BOOK)
		if (existingFragment != null) return
		val dialog = MakeChannelBookSelectDialog(
			onClickMakeChannel = { finishWithSelectedChannelBook(book.isbn) },
			selectedBook = book
		)
		dialog.show(childFragmentManager, DIALOG_TAG_SELECT_BOOK)
	}

	private fun showSearchFilterSelectDialog() {
		val existingFragment = childFragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_SEARCH_FILTER)
		if (existingFragment != null) return
		val dialog = SearchFilterSelectDialog(
			onSelectFilter = { searchViewModel.onClickSearchFilter(it) },
			searchUiState = searchViewModel.uiState
		)
		dialog.show(childFragmentManager, DIALOG_TAG_SELECT_SEARCH_FILTER)
	}

	private fun finishWithSelectedChannelBook(bookIsbn: String) {
		val parentActivity = requireActivity() as MakeChannelSelectBookActivity
		parentActivity.finishBookSelect(bookIsbn)
	}

	private fun moveToChannelInfo(channelId: Long) {
		val intent = Intent(requireContext(), ChannelInfoActivity::class.java)
		intent.putExtra(EXTRA_CLICKED_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun moveToMakeChannel() {
		makeChannelNavigator.navigate(
			currentActivity = requireActivity(),
		)
	}

	private fun handleEvent(event: SearchEvent) {
		when (event) {
			is SearchEvent.MoveToDetail -> moveToDetail(
				searchKeyword = event.searchKeyword,
				searchTarget = event.searchTarget,
				searchPurpose = event.searchPurpose,
				searchFilter = event.searchFilter
			)

			is SearchEvent.ShowSearchBookDialog -> showSearchTapBookDialog(event.book)
			is SearchEvent.ShowMakeChannelSelectBookDialog ->
				showMakeChannelSelectBookDialog(event.book)

			is SearchEvent.MoveToChannelInfo -> moveToChannelInfo(event.channelId)
			is SearchEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.snackbarPoint
			)

			is SearchEvent.ShowSearchFilterSelectDialog -> showSearchFilterSelectDialog()
			SearchEvent.MoveToMakeChannel -> moveToMakeChannel()
			is SearchEvent.ShowSearchFilterChangeSnackBar -> binding.root.showSnackBar(
				text = "${getString(R.string.selected_search_filter)} ${getString(event.stringId)}",
				anchor = binding.snackbarPoint
			)
		}
	}

	companion object {
		const val EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD"
		const val EXTRA_SEARCH_PURPOSE = "EXTRA_SEARCH_PURPOSE"
		const val EXTRA_SEARCH_TARGET = "EXTRA_SEARCH_TARGET"
		const val EXTRA_SEARCH_FILTER = "EXTRA_SEARCH_FILTER"

		const val EXTRA_SEARCHED_BOOK_ITEM_ID = "EXTRA_SEARCHED_BOOK_ITEM_ID"
		const val EXTRA_CLICKED_CHANNEL_ID = "EXTRA_CLICKED_CHANNEL_ID"
		const val DIALOG_TAG_SEARCH_BOOK = "DIALOG_TAG_SEARCH_BOOK"
		const val DIALOG_TAG_SELECT_BOOK = "DIALOG_TAG_SELECT_BOOK"
		const val DIALOG_TAG_SELECT_SEARCH_FILTER = "DIALOG_TAG_SELECT_SEARCH_FILTER"
	}
}