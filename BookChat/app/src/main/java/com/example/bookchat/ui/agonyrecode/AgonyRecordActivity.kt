package com.example.bookchat.ui.agonyrecode

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityAgonyRecordBinding
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.SwipeHelperCallback
import com.example.bookchat.ui.agony.AgonyActivity.Companion.EXTRA_AGONY_ID
import com.example.bookchat.ui.agony.AgonyActivity.Companion.EXTRA_BOOKSHELF_ID
import com.example.bookchat.ui.agony.AgonyEditActivity
import com.example.bookchat.ui.agony.model.AgonyListItem
import com.example.bookchat.ui.agonyrecode.AgonyRecordViewModel.AgonyRecordUiEvent
import com.example.bookchat.ui.agonyrecode.AgonyRecordViewModel.AgonyRecordUiState
import com.example.bookchat.ui.agonyrecode.adapter.AgonyRecordDataItemAdapter
import com.example.bookchat.ui.agonyrecode.adapter.AgonyRecordFirstItemAdapter
import com.example.bookchat.ui.agonyrecode.adapter.AgonyRecordHeaderItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgonyRecordActivity : AppCompatActivity() {

//	@Inject
//	lateinit var agonyRecordViewModelFactory: AgonyRecordViewModel.AssistedFactory

	private lateinit var binding: ActivityAgonyRecordBinding
	private lateinit var agonyRecordDataItemAdapter: AgonyRecordDataItemAdapter
	private lateinit var agonyRecordFirstItemAdapter: AgonyRecordFirstItemAdapter
	private lateinit var agonyRecordHeaderItemAdapter: AgonyRecordHeaderItemAdapter
	private lateinit var agonyListItem: AgonyListItem
	private lateinit var book: BookShelfItem
	private lateinit var firstAgonyTitle: String
//	private val agonyRecordViewModel: AgonyRecordViewModel by viewModels {
//		AgonyRecordViewModel.provideFactory(agonyRecordViewModelFactory, agonyListItem, book)
//	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_agony_record)
		agonyListItem = getAgonyDataItem()
		book = getBook()
//		firstAgonyTitle = agonyListItem.title
		with(binding) {
//			viewmodel = agonyRecordViewModel
			lifecycleOwner = this@AgonyRecordActivity
		}

		initAdapter()
		initRecyclerView()
		observeAgonyRecordUiEvent()
		observePagingAgonyRecord()
		setBackPressedDispatcher()
	}

	private fun initAdapter() {
//		agonyRecordDataItemAdapter = AgonyRecordDataItemAdapter(agonyRecordViewModel)
//		agonyRecordFirstItemAdapter = AgonyRecordFirstItemAdapter(agonyRecordViewModel)
//		agonyRecordHeaderItemAdapter = AgonyRecordHeaderItemAdapter(agonyListItem.agony)
		agonyRecordHeaderItemAdapter.setHeaderItemClickListener {
//            val intent = Intent(this@AgonyRecordActivity, AgonyEditActivity::class.java)
//                .putExtra(EXTRA_AGONY, agonyDataItem.agony)
//                .putExtra(EXTRA_BOOK, book)
//            agonyEditActivityResultLauncher.launch(intent)
		}
	}

	private val agonyEditActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val newTitle =
					intent?.getStringExtra(AgonyEditActivity.EXTRA_NEW_AGONY_TITLE) ?: throw Exception()
//				agonyListItem = agonyListItem.copy(agony = agonyListItem.agony.copy(title = newTitle))
				agonyRecordHeaderItemAdapter.agony =
					agonyRecordHeaderItemAdapter.agony.copy(title = newTitle)
				agonyRecordHeaderItemAdapter.notifyItemChanged(0)
			}
		}

	private fun initRecyclerView() {
		with(binding) {
			val concatAdapterConfig =
				ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
			val concatAdapter = ConcatAdapter(
				concatAdapterConfig,
				agonyRecordHeaderItemAdapter,
				agonyRecordFirstItemAdapter,
				agonyRecordDataItemAdapter
			)
			agonyRecordRcv.adapter = concatAdapter
			agonyRecordRcv.setHasFixedSize(true)
			agonyRecordRcv.layoutManager = LinearLayoutManager(this@AgonyRecordActivity)
			setSwipeHelperCallback(agonyRecordRcv)
		}
	}

	private fun setSwipeHelperCallback(recyclerView: RecyclerView) {
		val swipeHelperCallback = SwipeHelperCallback()
		val itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
		itemTouchHelper.attachToRecyclerView(recyclerView)
	}

	private fun handleEvent(event: AgonyRecordUiEvent) {
		when (event) {
			is AgonyRecordUiEvent.MoveToBack -> {
				finish()
			}

			is AgonyRecordUiEvent.ShowWarningDialog -> {
				showWarningDialog()
			}

			is AgonyRecordUiEvent.RenewFirstItemAdapter -> {
				renewFirstItemAdapter()
			}

			is AgonyRecordUiEvent.RefreshDataItemAdapter -> {
				agonyRecordDataItemAdapter.refresh()
			}
		}
	}

	private fun renewFirstItemAdapter() =
		agonyRecordFirstItemAdapter.notifyItemChanged(0)

	private fun showWarningDialog() {
		val warningDialog = AgonyRecordWarningDialog()
		warningDialog.show(this.supportFragmentManager, DIALOG_TAG_WARNING_EDIT_CANCEL)
	}

	private fun observeAgonyRecordUiEvent() = lifecycleScope.launch {
//		agonyRecordViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun observePagingAgonyRecord() = lifecycleScope.launch {
//		agonyRecordViewModel.agonyRecordCombined.observe(this@AgonyRecordActivity) { pagingData ->
//			agonyRecordDataItemAdapter.submitData(this@AgonyRecordActivity.lifecycle, pagingData)
//		}
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
//			if (agonyRecordViewModel.agonyRecordUiState.value == AgonyRecordUiState.Editing) {
//				showWarningDialog()
//				return@addCallback
//			}
			finish()
		}
	}

	private fun getAgonyDataItem(): AgonyListItem {
		return intent.getSerializableExtra(EXTRA_AGONY_ID) as AgonyListItem
	}

	private fun getBook(): BookShelfItem {
		return intent.getSerializableExtra(EXTRA_BOOKSHELF_ID) as BookShelfItem
	}

	override fun onResume() {
		if (firstAgonyTitle != agonyRecordHeaderItemAdapter.agony.title) {
			intent.putExtra(
				AgonyEditActivity.EXTRA_NEW_AGONY_TITLE,
				agonyRecordHeaderItemAdapter.agony.title
			)
			setResult(RESULT_OK, intent)
		}
		super.onResume()
	}

	companion object {
		private const val DIALOG_TAG_WARNING_EDIT_CANCEL = "DIALOG_TAG_WARNING_EDIT_CANCEL"
	}
}