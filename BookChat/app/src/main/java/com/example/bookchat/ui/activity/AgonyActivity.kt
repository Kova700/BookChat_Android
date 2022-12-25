package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.AgonyAdapter
import com.example.bookchat.data.Agony
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityAgonyBinding
import com.example.bookchat.ui.dialog.ReadingTapBookDialog.Companion.EXTRA_AGONIZE_BOOK
import com.example.bookchat.viewmodel.AgonyViewModel
import com.example.bookchat.viewmodel.AgonyViewModel.AgonizeUIEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgonyActivity : AppCompatActivity() {

    @Inject
    lateinit var agonyViewModelFactory : AgonyViewModel.AssistedFactory

    private lateinit var binding :ActivityAgonyBinding
    private lateinit var agonyAdapter: AgonyAdapter
    private val agonyViewModel: AgonyViewModel by viewModels{
        AgonyViewModel.provideFactory(agonyViewModelFactory, getAgonizeBook())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_agony)
        with(binding){
            lifecycleOwner = this@AgonyActivity
            viewmodel = agonyViewModel
        }
        initAdapter()
        initRecyclerView()
        observeAgonizeEvent()
        observePagingAgony()

    }

    private fun observePagingAgony() = lifecycleScope.launch{
        agonyViewModel.agonyCombined.observe(this@AgonyActivity){ pagingData ->
            agonyAdapter.submitData(this@AgonyActivity.lifecycle, pagingData)
        }
    }

    private fun observeAgonizeEvent() = lifecycleScope.launch{
        agonyViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun initAdapter() {
        val agonyClickListener = object : AgonyAdapter.OnItemClickListener{
            override fun onItemClick(agony: Agony) {
                //AgonyRecordActivity로 이동
            }
        }
        agonyAdapter = AgonyAdapter(agonyViewModel)
        agonyAdapter.setItemClickListener(agonyClickListener)
    }

    private fun initRecyclerView(){
        with(binding){
            agonyRcv.adapter = agonyAdapter
            val gridLayoutManager = GridLayoutManager(this@AgonyActivity,2)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {
                    return when(agonyAdapter.getItemViewType(position)){
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

    private fun handleEvent(event :AgonizeUIEvent){
        when(event){
            is AgonizeUIEvent.MoveToBack -> { finish() }
        }
    }

    private fun getAgonizeBook() : BookShelfItem{
        return intent.getSerializableExtra(EXTRA_AGONIZE_BOOK) as BookShelfItem
    }

}