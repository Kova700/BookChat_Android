package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.AgonyRecordAdapter
import com.example.bookchat.data.Agony
import com.example.bookchat.databinding.ActivityAgonyRecordBinding
import com.example.bookchat.ui.activity.AgonyActivity.Companion.EXTRA_AGONY
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
    private lateinit var agonyRecordAdapter: AgonyRecordAdapter
    private lateinit var agony :Agony
    private val agonyRecordViewModel :AgonyRecordViewModel by viewModels {
        AgonyRecordViewModel.provideFactory(agonyRecordViewModelFactory, agony)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_agony_record)
        agony = getAgony()
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
        agonyRecordAdapter = AgonyRecordAdapter(agonyRecordViewModel)
        //onClickListener 설정 해야함
    }

    private fun initRecyclerView(){
        with(binding){
            agonyRecordRcv.adapter = agonyRecordAdapter
            agonyRecordRcv.setHasFixedSize(true)
            agonyRecordRcv.layoutManager = LinearLayoutManager(this@AgonyRecordActivity)
        }
    }

    private fun handleEvent(event : AgonyRecordUiEvent){
        when(event){
            is AgonyRecordUiEvent.MoveToBack -> { finish() }
            is AgonyRecordUiEvent.RenewAgonyList -> {
                //화면 갱신 이벤트
            }
        }
    }

    private fun observeAgonyRecordUiEvent() = lifecycleScope.launch{
        agonyRecordViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun observePagingAgonyRecord() = lifecycleScope.launch{
        agonyRecordViewModel.agonyRecordCombined.observe(this@AgonyRecordActivity){ pagingData ->
            agonyRecordAdapter.submitData(this@AgonyRecordActivity.lifecycle, pagingData)
        }
    }

    private fun getAgony() : Agony {
        return intent.getSerializableExtra(EXTRA_AGONY) as Agony
    }
}