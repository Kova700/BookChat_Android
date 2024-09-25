package com.kova700.bookchat.feature.agony.agony

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.feature.agony.agony.AgonyUiState.UiState
import com.kova700.bookchat.feature.agony.agony.adapter.AgonyAdapter
import com.kova700.bookchat.feature.agony.agony.adapter.AgonyItemDecorator
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordActivity
import com.kova700.bookchat.feature.agony.databinding.ActivityAgonyBinding
import com.kova700.bookchat.feature.agony.makeagony.MakeAgonyBottomSheetDialog
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kova700.bookchat.feature.agony.R as agonyR

@AndroidEntryPoint
class AgonyActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAgonyBinding

	@Inject
	lateinit var agonyAdapter: AgonyAdapter

	private val agonyViewModel: AgonyViewModel by viewModels()

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	@Inject
	lateinit var agonyItemDecorator: AgonyItemDecorator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAgonyBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initAdapter()
		initRecyclerView()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		agonyViewModel.uiState.collect { uiState ->
			agonyAdapter.submitList(uiState.agonies)
			agonyAdapter.agonyUiState = uiState
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		agonyViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { agonyViewModel.onClickBackBtn() }
		binding.wasteBasketBtn.setOnClickListener { agonyViewModel.onClickEditBtn() }
		binding.editCancelBtn.setOnClickListener { agonyViewModel.onClickEditCancelBtn() }
		binding.agonyDeleteBtn.setOnClickListener { agonyViewModel.onClickDeleteBtn() }
		initShimmerState()
	}

	private fun setViewState(state: AgonyUiState) {
		with(binding) {
			edittingStateGroup.visibility =
				if (state.uiState == UiState.EDITING) View.VISIBLE else View.INVISIBLE
			wasteBasketBtn.visibility =
				if (state.uiState == UiState.SUCCESS) View.VISIBLE else View.INVISIBLE
			agonyRcv.visibility =
				if (state.uiState != UiState.INIT_LOADING) View.VISIBLE else View.GONE
			agonyShimmerLayout.root.visibility =
				if (state.uiState == UiState.INIT_LOADING) View.VISIBLE else View.GONE
					.also { agonyShimmerLayout.shimmerLayout.stopShimmer() }
			progressbar.visibility =
				if (state.uiState == UiState.LOADING) View.VISIBLE else View.GONE
		}
	}

	private fun initShimmerState() {
		bookImgSizeManager.setBookImgSize(binding.agonyShimmerLayout.bookImg)
	}

	private fun initAdapter() {
		agonyAdapter.onFirstItemClick = {
			agonyViewModel.onClickFirstItem()
		}

		agonyAdapter.onItemClick = { itemPosition ->
			val item = agonyAdapter.currentList[itemPosition] as AgonyListItem.Item
			agonyViewModel.onClickItem(item)
		}

		agonyAdapter.onItemSelect = { itemPosition ->
			val item = agonyAdapter.currentList[itemPosition] as AgonyListItem.Item
			agonyViewModel.onItemSelect(item)
		}
	}

	private fun moveToAgonyRecord(
		bookshelfItemId: Long,
		agonyListItemId: Long,
	) {
		AgonyRecordActivity.start(
			currentActivity = this@AgonyActivity,
			bookshelfItemId = bookshelfItemId,
			agonyId = agonyListItemId
		)
	}

	private fun openBottomSheetDialog(bookshelfItemId: Long) {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_MAKE_AGONY)
		if (existingFragment != null) return

		val dialog = MakeAgonyBottomSheetDialog()
		dialog.arguments = bundleOf(EXTRA_BOOKSHELF_ID to bookshelfItemId)
		dialog.show(supportFragmentManager, DIALOG_TAG_MAKE_AGONY)
	}

	private fun initRecyclerView() {
		val gridLayoutManager = GridLayoutManager(this@AgonyActivity, 2)
		gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int {
				return when (agonyAdapter.getItemViewType(position)) {
					agonyR.layout.item_agony_header -> 2
					else -> 1
				}
			}
		}

		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				agonyViewModel.loadNextAgonies(
					gridLayoutManager.findLastVisibleItemPosition()
				)
			}
		}
		with(binding.agonyRcv) {
			adapter = agonyAdapter
			layoutManager = gridLayoutManager
			addOnScrollListener(rcvScrollListener)
			addItemDecoration(agonyItemDecorator)
		}
	}

	private fun renewItemViewMode() {
		agonyAdapter.notifyItemRangeChanged(1, agonyAdapter.itemCount - 1)
	}

	private fun handleEvent(event: AgonyEvent) {
		when (event) {
			is AgonyEvent.MoveToBack -> finish()
			is AgonyEvent.MoveToAgonyRecord ->
				moveToAgonyRecord(event.bookshelfItemId, event.agonyListItemId)

			is AgonyEvent.OpenBottomSheetDialog -> openBottomSheetDialog(event.bookshelfItemId)
			is AgonyEvent.RenewItemViewMode -> renewItemViewMode()
			is AgonyEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.agonyDeleteBtn
			)
		}
	}

	companion object {
		private const val DIALOG_TAG_MAKE_AGONY = "DIALOG_TAG_MAKE_AGONY"
		const val EXTRA_BOOKSHELF_ID = "EXTRA_BOOK"
	}

}