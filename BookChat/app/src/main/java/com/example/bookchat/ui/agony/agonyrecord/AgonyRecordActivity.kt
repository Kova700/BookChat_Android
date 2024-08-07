package com.example.bookchat.ui.agony.agonyrecord

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ActivityAgonyRecordBinding
import com.example.bookchat.ui.agony.agonyedit.AgonyEditActivity
import com.example.bookchat.ui.agony.agonyrecord.adapter.AgonyRecordAdapter
import com.example.bookchat.ui.agony.agonyrecord.dialog.AgonyRecordWarningDialog
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgonyRecordActivity : AppCompatActivity() {

	//TODO: AgonyRecord 생성 시, Content 내용이 엄청 길다면 400 넘어오는 현상이 있음
	// {"errorCode":"500","message":"예상치 못한 예외가 발생했습니다."}
	// 길이 문제인줄 알았지만 수정시에는 더 긴 길이도 등록됨으로 길이 문제는 아닌 듯 (등록 API측에 문제가 있나봄)
	private lateinit var binding: ActivityAgonyRecordBinding

	@Inject
	lateinit var agonyRecordAdapter: AgonyRecordAdapter

	@Inject
	lateinit var agonyRecordSwipeHelper: AgonyRecordSwipeHelper

	private val agonyRecordViewModel: AgonyRecordViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAgonyRecordBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initAdapter()
		initRecyclerView()
		initViewState()
		setBackPressedDispatcher()
	}

	private fun observeUiState() = lifecycleScope.launch {
		agonyRecordViewModel.uiState.collect { uiState ->
			agonyRecordAdapter.submitList(uiState.records)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		agonyRecordViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { agonyRecordViewModel.onBackBtnClick() }
	}

	private fun initAdapter() {
		agonyRecordAdapter.onHeaderEditBtnClick = {
			agonyRecordViewModel.onHeaderEditBtnClick()
		}

		agonyRecordAdapter.onFirstItemClick = {
			agonyRecordViewModel.onFirstItemClick()
		}
		agonyRecordAdapter.onFirstItemEditCancelBtnClick = {
			agonyRecordViewModel.onFirstItemEditCancelBtnClick()
		}
		agonyRecordAdapter.onFirstItemEditFinishBtnClick = {
			val item = agonyRecordAdapter.currentList[1] as AgonyRecordListItem.FirstItem
			agonyRecordViewModel.onFirstItemEditFinishBtnClick(item)
		}

		agonyRecordAdapter.onItemClick = { position ->
			val item = agonyRecordAdapter.currentList[position] as AgonyRecordListItem.Item
			agonyRecordViewModel.onItemClick(item)
		}
		agonyRecordAdapter.onItemSwipe = { position, isSwiped ->
			val item = agonyRecordAdapter.currentList[position] as AgonyRecordListItem.Item
			agonyRecordViewModel.onItemSwipe(item, isSwiped)
		}
		agonyRecordAdapter.onItemEditCancelBtnClick = { position ->
			val item = agonyRecordAdapter.currentList[position] as AgonyRecordListItem.Item
			agonyRecordViewModel.onItemEditCancelBtnClick(item)
		}
		agonyRecordAdapter.onItemEditFinishBtnClick = { position ->
			val item = agonyRecordAdapter.currentList[position] as AgonyRecordListItem.Item
			agonyRecordViewModel.onItemEditFinishBtnClick(item)
		}
		agonyRecordAdapter.onItemDeleteBtnClick = { position ->
			val item = agonyRecordAdapter.currentList[position] as AgonyRecordListItem.Item
			agonyRecordViewModel.onItemDeleteBtnClick(item)
		}
	}

	private fun moveToAgonyTitleEdit(agonyId: Long, bookshelfItemId: Long) {
		val intent = Intent(this, AgonyEditActivity::class.java)
			.putExtra(EXTRA_AGONY_ID, agonyId)
			.putExtra(EXTRA_BOOKSHELF_ITEM_ID, bookshelfItemId)
		startActivity(intent)
	}

	private fun initRecyclerView() {
		val linearLayoutManager = LinearLayoutManager(this)
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				agonyRecordViewModel.loadNextAgonyRecords(
					linearLayoutManager.findLastVisibleItemPosition()
				)
			}
		}
		with(binding.agonyRecordRcv) {
			adapter = agonyRecordAdapter
			setHasFixedSize(false)
			layoutManager = linearLayoutManager
			initSwipeHelper(this)
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun initSwipeHelper(recyclerView: RecyclerView) {
		val itemTouchHelper = ItemTouchHelper(agonyRecordSwipeHelper)
		itemTouchHelper.attachToRecyclerView(recyclerView)
	}

	private fun showEditCancelWarning() {
		val warningDialog = AgonyRecordWarningDialog(
			onClickOkBtn = { agonyRecordViewModel.clearEditingState() }
		)
		warningDialog.show(supportFragmentManager, DIALOG_TAG_WARNING_EDIT_CANCEL)
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback { agonyRecordViewModel.onBackBtnClick() }
	}

	private fun handleEvent(event: AgonyRecordEvent) {
		when (event) {
			is AgonyRecordEvent.MoveToBack -> finish()
			is AgonyRecordEvent.MoveToAgonyTitleEdit -> moveToAgonyTitleEdit(
				agonyId = event.agonyId,
				bookshelfItemId = event.bookshelfItemId
			)

			is AgonyRecordEvent.ShowEditCancelWarning -> showEditCancelWarning()
			is AgonyRecordEvent.ShowSnackBar -> binding.root.showSnackBar(textId = event.stringId)
		}
	}

	companion object {
		private const val DIALOG_TAG_WARNING_EDIT_CANCEL = "DIALOG_TAG_WARNING_EDIT_CANCEL"
		const val EXTRA_AGONY_ID = "EXTRA_AGONY_ID"
		const val EXTRA_BOOKSHELF_ITEM_ID = "EXTRA_BOOKSHELF_ITEM_ID"
	}
}