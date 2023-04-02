package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ActivityMakeChatRoomBinding
import com.example.bookchat.viewmodel.MakeChatRoomViewModel
import com.example.bookchat.viewmodel.MakeChatRoomViewModel.MakeChatRoomUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MakeChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMakeChatRoomBinding
    private val makeChatRoomViewModel: MakeChatRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_chat_room)
        with(binding) {
            viewmodel = makeChatRoomViewModel
            lifecycleOwner = this@MakeChatRoomActivity
        }
        observeUiEvent()
    }

    private fun moveToSelectBook() {
        val intent = Intent(this, MakeChatRoomSelectBookActivity::class.java)
        selectBookResultLauncher.launch(intent)
    }

    private val selectBookResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val intent = result.data
                val selectBook = intent?.getSerializableExtra(MakeChatRoomSelectBookActivity.EXTRA_SELECTED_BOOK) as? Book
                selectBook?.let { makeChatRoomViewModel.selectedBook.value = selectBook }
            }
        }

    private fun observeUiEvent() = lifecycleScope.launch {
        makeChatRoomViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun handleEvent(event: MakeChatRoomUiEvent) = when (event) {
        MakeChatRoomUiEvent.MoveToBack -> finish()
        MakeChatRoomUiEvent.MoveSelectBook -> moveToSelectBook()
    }
}