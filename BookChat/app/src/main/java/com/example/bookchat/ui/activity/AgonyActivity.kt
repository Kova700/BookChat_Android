package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.Agony.AgonyDataItemAdapter
import com.example.bookchat.adapter.Agony.AgonyFirstItemAdapter
import com.example.bookchat.adapter.Agony.AgonyHeaderItemAdapter
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityAgonyBinding
import com.example.bookchat.ui.dialog.MakeAgonyBottomSheetDialog
import com.example.bookchat.ui.dialog.ReadingTapBookDialog.Companion.EXTRA_AGONIZE_BOOK
import com.example.bookchat.viewmodel.AgonyViewModel
import com.example.bookchat.viewmodel.AgonyViewModel.AgonyUiEvent
import com.example.bookchat.viewmodel.AgonyViewModel.PagingViewEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgonyActivity : AppCompatActivity() {

    @Inject
    lateinit var agonyViewModelFactory : AgonyViewModel.AssistedFactory

    private lateinit var binding :ActivityAgonyBinding
    private lateinit var agonyDataItemAdapter: AgonyDataItemAdapter
    private lateinit var agonyFirstItemAdapter : AgonyFirstItemAdapter
    private lateinit var agonyHeaderItemAdapter : AgonyHeaderItemAdapter
    private lateinit var book : BookShelfItem
    private val agonyViewModel: AgonyViewModel by viewModels{
        AgonyViewModel.provideFactory(agonyViewModelFactory, book)
    }
    private lateinit var clickedDataItem :AgonyDataItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_agony)
        book = getAgonizeBook()
        with(binding){
            lifecycleOwner = this@AgonyActivity
            viewmodel = agonyViewModel
        }
        initAdapter()
        initRecyclerView()
        observeAgonyUiEvent()
        observePagingAgony()
    }

    private fun initAdapter() {
        val dataItemClickListener = object : AgonyDataItemAdapter.OnDataItemClickListener{
            override fun onItemClick(agonyDataItem: AgonyDataItem) {
                val intent = Intent(this@AgonyActivity, AgonyRecordActivity::class.java)
                    .putExtra(EXTRA_BOOK, book)
                    .putExtra(EXTRA_AGONY, agonyDataItem)
                clickedDataItem = agonyDataItem
                agonyRecordActivityResultLauncher.launch(intent)
            }
        }
        val firstItemClickListener = object  : AgonyFirstItemAdapter.OnFirstItemClickListener {
            override fun onItemClick() {
                MakeAgonyBottomSheetDialog(book).show(supportFragmentManager, DIALOG_TAG_MAKE_AGONY)
            }
        }
        agonyDataItemAdapter = AgonyDataItemAdapter(agonyViewModel)
        agonyFirstItemAdapter = AgonyFirstItemAdapter(agonyViewModel)
        agonyHeaderItemAdapter = AgonyHeaderItemAdapter(agonyViewModel)
        agonyDataItemAdapter.setDataItemClickListener(dataItemClickListener)
        agonyFirstItemAdapter.setFirstItemClickListner(firstItemClickListener)
    }

    private val agonyRecordActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val newTitle = result.data?.getStringExtra(AgonyEditActivity.EXTRA_NEW_AGONY_TITLE)
                    ?: throw Exception("newTitle is not exist")
                agonyViewModel.onPagingViewEvent(PagingViewEvent.ChangeItemTitle(clickedDataItem, newTitle))
            }
        }

    private fun initRecyclerView(){
        with(binding){
            val concatAdapterConfig = ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(concatAdapterConfig, agonyHeaderItemAdapter, agonyFirstItemAdapter, agonyDataItemAdapter)
            agonyRcv.adapter = concatAdapter
            val gridLayoutManager = GridLayoutManager(this@AgonyActivity,2)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {
                    return when(concatAdapter.getItemViewType(position)){
                        R.layout.item_agony_header -> 2
                        R.layout.item_agony_first -> 1
                        R.layout.item_agony_data -> 1
                        else -> throw Exception("Unknown ViewType")
                    }
                }
            }
            agonyRcv.layoutManager = gridLayoutManager
        }
    }

    private fun observeAgonyUiEvent() = lifecycleScope.launch{
        agonyViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun observePagingAgony() = lifecycleScope.launch{
        agonyViewModel.agonyCombined.observe(this@AgonyActivity){ pagingData ->
            agonyDataItemAdapter.submitData(this@AgonyActivity.lifecycle, pagingData)
        }
    }

    private fun handleEvent(event :AgonyUiEvent){
        when(event){
            is AgonyUiEvent.MoveToBack -> { finish() }
            is AgonyUiEvent.RenewAgonyList -> { agonyDataItemAdapter.refresh() }
        }
    }

    private fun getAgonizeBook() : BookShelfItem{
        return intent.getSerializableExtra(EXTRA_AGONIZE_BOOK) as BookShelfItem
    }

    companion object {
        private const val DIALOG_TAG_MAKE_AGONY = "MakeAgonyBottomSheetDialog"
        const val EXTRA_AGONY = "EXTRA_AGONY"
        const val EXTRA_BOOK = "EXTRA_BOOK"
    }

}