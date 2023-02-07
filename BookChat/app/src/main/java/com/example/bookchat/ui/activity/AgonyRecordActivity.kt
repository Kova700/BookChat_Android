package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.AgonyRecord.AgonyRecordDataItemAdapter
import com.example.bookchat.adapter.AgonyRecord.AgonyRecordFirstItemAdapter
import com.example.bookchat.adapter.AgonyRecord.AgonyRecordHeaderItemAdapter
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityAgonyRecordBinding
import com.example.bookchat.ui.activity.AgonyActivity.Companion.EXTRA_AGONY
import com.example.bookchat.ui.activity.AgonyActivity.Companion.EXTRA_BOOK
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.AgonyRecordViewModel
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonyRecordUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgonyRecordActivity : AppCompatActivity() {

    @Inject
    lateinit var agonyRecordViewModelFactory :AgonyRecordViewModel.AssistedFactory

    private lateinit var binding : ActivityAgonyRecordBinding
    private lateinit var agonyRecordDataItemAdapter: AgonyRecordDataItemAdapter
    private lateinit var agonyRecordFirstItemAdapter: AgonyRecordFirstItemAdapter
    private lateinit var agonyRecordHeaderItemAdapter: AgonyRecordHeaderItemAdapter
    private lateinit var agonyDataItem :AgonyDataItem
    private lateinit var book :BookShelfItem
    private lateinit var firstAgonyTitle :String
    private val agonyRecordViewModel :AgonyRecordViewModel by viewModels {
        AgonyRecordViewModel.provideFactory(agonyRecordViewModelFactory, agonyDataItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_agony_record)
        agonyDataItem = getAgonyDataItem()
        book = getBook()
        firstAgonyTitle = agonyDataItem.agony.title
        with(binding){
            viewmodel = agonyRecordViewModel
            lifecycleOwner = this@AgonyRecordActivity
        }

        initAdapter()
        initRecyclerView()
        observeAgonyRecordUiEvent()
        observePagingAgonyRecord()
    }

    private fun initAdapter(){
        agonyRecordDataItemAdapter = AgonyRecordDataItemAdapter(agonyRecordViewModel)
        agonyRecordFirstItemAdapter = AgonyRecordFirstItemAdapter(agonyRecordViewModel)
        agonyRecordHeaderItemAdapter = AgonyRecordHeaderItemAdapter(agonyDataItem.agony)
        agonyRecordHeaderItemAdapter.setHeaderItemClickListener{
            val intent = Intent(this@AgonyRecordActivity, AgonyEditActivity::class.java)
                .putExtra(EXTRA_AGONY, agonyDataItem.agony)
                .putExtra(EXTRA_BOOK, book)
            agonyEditActivityResultLauncher.launch(intent)
        }
        //onClickListener 설정 해야함
    }

    private val agonyEditActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val intent = result.data
                val newTitle = intent?.getStringExtra(AgonyEditActivity.EXTRA_NEW_AGONY_TITLE) ?: throw Exception()
                agonyDataItem = agonyDataItem.copy(agony = agonyDataItem.agony.copy(title = newTitle))
                agonyRecordHeaderItemAdapter.agony = agonyRecordHeaderItemAdapter.agony.copy(title = newTitle)
                agonyRecordHeaderItemAdapter.notifyDataSetChanged()
            }
        }

    private fun initRecyclerView(){
        with(binding){
            val concatAdapterConfig = ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
            val concatAdapter = ConcatAdapter(concatAdapterConfig, agonyRecordHeaderItemAdapter, agonyRecordFirstItemAdapter, agonyRecordDataItemAdapter)
            agonyRecordRcv.adapter = concatAdapter
            agonyRecordRcv.setHasFixedSize(true)
            agonyRecordRcv.layoutManager = LinearLayoutManager(this@AgonyRecordActivity)
        }
    }

    private fun handleEvent(event : AgonyRecordUiEvent){
        when(event){
            is AgonyRecordUiEvent.MoveToBack -> { finish() }
            is AgonyRecordUiEvent.RenewAgonyList -> {
                //화면 갱신 이벤트
                //(고민 기록 생셩시 생성된 서버 데이터 받아와야지)
            }
        }
    }

    private fun observeAgonyRecordUiEvent() = lifecycleScope.launch{
        agonyRecordViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun observePagingAgonyRecord() = lifecycleScope.launch{
        agonyRecordViewModel.agonyRecordCombined.observe(this@AgonyRecordActivity){ pagingData ->
            agonyRecordDataItemAdapter.submitData(this@AgonyRecordActivity.lifecycle, pagingData)
        }
    }

    private fun getAgonyDataItem() : AgonyDataItem {
        return intent.getSerializableExtra(EXTRA_AGONY) as AgonyDataItem
    }

    private fun getBook() :BookShelfItem {
        return intent.getSerializableExtra(EXTRA_BOOK) as BookShelfItem
    }

    override fun onResume() {
        if (firstAgonyTitle != agonyRecordHeaderItemAdapter.agony.title){
            val intent = Intent(this@AgonyRecordActivity, AgonyActivity::class.java)
            intent.putExtra(AgonyEditActivity.EXTRA_NEW_AGONY_TITLE, agonyRecordHeaderItemAdapter.agony.title)
            setResult(RESULT_OK,intent)
        }
        super.onResume()
    }
}