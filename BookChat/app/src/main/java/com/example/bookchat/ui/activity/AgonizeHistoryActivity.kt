package com.example.bookchat.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityAgonizeHistoryBinding
import com.example.bookchat.ui.dialog.ReadingTapBookDialog.Companion.EXTRA_AGONIZE_BOOK
import com.example.bookchat.viewmodel.AgonizeHistoryViewModel
import com.example.bookchat.viewmodel.AgonizeHistoryViewModel.AgonizeUIEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class AgonizeHistoryActivity : AppCompatActivity() {

    private lateinit var binding :ActivityAgonizeHistoryBinding
    private lateinit var agonizeHistoryViewModel: AgonizeHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_agonize_history)
        agonizeHistoryViewModel = ViewModelProvider(this, ViewModelFactory()).get(AgonizeHistoryViewModel::class.java)
        with(binding){
            lifecycleOwner = this@AgonizeHistoryActivity
            viewmodel = agonizeHistoryViewModel
        }

        agonizeHistoryViewModel.book = getAgonizeBook()
        observeAgonizeEvent()

    }

    private fun observeAgonizeEvent() = lifecycleScope.launch{
        agonizeHistoryViewModel.eventFlow.collect{ event -> handleEvent(event) }
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