package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityAgonyRecordBinding
import com.example.bookchat.ui.dialog.ReadingTapBookDialog.Companion.EXTRA_AGONIZE_BOOK
import com.example.bookchat.viewmodel.AgonyRecordViewModel
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonizeUIEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgonyRecordActivity : AppCompatActivity() {

    private lateinit var binding :ActivityAgonyRecordBinding
    private val agonyRecordViewModel: AgonyRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_agony_record)
        with(binding){
            lifecycleOwner = this@AgonyRecordActivity
            viewmodel = agonyRecordViewModel
        }

        agonyRecordViewModel.book = getAgonizeBook()
        observeAgonizeEvent()

    }

    private fun observeAgonizeEvent() = lifecycleScope.launch{
        agonyRecordViewModel.eventFlow.collect{ event -> handleEvent(event) }
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