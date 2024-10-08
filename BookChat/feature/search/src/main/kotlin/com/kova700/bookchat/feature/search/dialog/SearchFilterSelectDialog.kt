package com.kova700.bookchat.feature.search.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.feature.search.SearchUiState
import com.kova700.bookchat.feature.search.databinding.DialogSearchFilterSelectBinding
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.util.dialog.DialogSizeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFilterSelectDialog(
	private val onSelectFilter: (SearchFilter) -> Unit,
	private val searchUiState: Flow<SearchUiState>,
) : DialogFragment() {

	private var _binding: DialogSearchFilterSelectBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogSearchFilterSelectBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		initViewState()
		observeUiState()
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchUiState.collect { state ->
			setViewState(state)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun initViewState() {
		dialogSizeManager.setDialogSize(binding.root)
		with(binding) {
			searchFilterBookTitleBtn.setOnClickListener {
				onSelectFilter.invoke(SearchFilter.BOOK_TITLE)
				dismiss()
			}
			searchFilterBookIsbnBtn.setOnClickListener {
				onSelectFilter.invoke(SearchFilter.BOOK_ISBN)
				dismiss()
			}
			searchFilterChannelTitleBtn.setOnClickListener {
				onSelectFilter.invoke(SearchFilter.ROOM_NAME)
				dismiss()
			}
			searchFilterChannelTagBtn.setOnClickListener {
				onSelectFilter.invoke(SearchFilter.ROOM_TAGS)
				dismiss()
			}
		}
	}

	private fun setViewState(state: SearchUiState) {
		if (state.searchPurpose == SearchPurpose.MAKE_CHANNEL) {
			binding.searchFilterChannelTitleBtn.visibility = View.GONE
			binding.searchFilterChannelTagBtn.visibility = View.GONE
		}
		setBtnColor(state)
	}

	private fun setBtnColor(state: SearchUiState) {
		fun TextView.setColor(targetFilter: SearchFilter) {
			if (state.searchFilter == targetFilter) setTextColor(Color.parseColor("#5648FF"))
			else setTextColor(Color.parseColor("#000000"))
		}
		with(binding) {
			searchFilterBookTitleBtn.setColor(SearchFilter.BOOK_TITLE)
			searchFilterBookIsbnBtn.setColor(SearchFilter.BOOK_ISBN)
			searchFilterChannelTitleBtn.setColor(SearchFilter.ROOM_NAME)
			searchFilterChannelTagBtn.setColor(SearchFilter.ROOM_TAGS)
		}
	}

}