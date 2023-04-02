package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.DialogMakeChatRoomSelectBookBinding
import com.example.bookchat.ui.activity.MakeChatRoomSelectBookActivity

class MakeChatRoomSelectBookDialog(val book: Book) : DialogFragment() {
    private lateinit var binding :DialogMakeChatRoomSelectBookBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_make_chat_room_select_book, container, false)
        with(binding){
            lifecycleOwner = this@MakeChatRoomSelectBookDialog
            dialog = this@MakeChatRoomSelectBookDialog
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    fun clickMakeChatRoom(){
        val parentActivity = (requireActivity() as? MakeChatRoomSelectBookActivity)
        parentActivity?.selectedBook = book
        dismiss()
        parentActivity?.finishSelect()
    }
}