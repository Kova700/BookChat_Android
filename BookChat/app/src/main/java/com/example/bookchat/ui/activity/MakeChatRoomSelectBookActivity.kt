package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ActivityMakeChatRoomSelectBookBinding
import com.example.bookchat.ui.fragment.SearchFragment
import com.example.bookchat.utils.SearchPurpose
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeChatRoomSelectBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMakeChatRoomSelectBookBinding
    private val searchFragment by lazy { SearchFragment(SearchPurpose.MakeChatRoom) }
    var selectedBook: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_chat_room_select_book)

        inflateFragment(searchFragment, FRAGMENT_TAG_SEARCH)
    }

    private fun inflateFragment(newFragment: Fragment, tag: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        with(fragmentTransaction) {
            setReorderingAllowed(true)
            add(R.id.select_book_frame_layout, newFragment, tag)
            commitNow()
        }
    }
    fun finishSelect(){
        val intent = Intent(this, MakeChatRoomActivity::class.java)
        intent.putExtra(EXTRA_SELECTED_BOOK, selectedBook)
        setResult(RESULT_OK,intent)
        finish()
    }

    companion object {
        private const val FRAGMENT_TAG_SEARCH = "Search"
        const val EXTRA_SELECTED_BOOK = "EXTRA_SELECTED_BOOK"
    }
}