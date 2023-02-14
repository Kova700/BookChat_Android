package com.example.bookchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Agony
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ActivityAgonyEditBinding
import com.example.bookchat.repository.AgonyRepository
import com.example.bookchat.ui.activity.AgonyActivity.Companion.EXTRA_AGONY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

//API하나만 호출하면 됨으로 ViewModel 없이 진행
@AndroidEntryPoint
class AgonyEditActivity : AppCompatActivity() {

    private lateinit var binding :ActivityAgonyEditBinding
    private lateinit var oldAgony :Agony
    private lateinit var book :BookShelfItem
    val agonyTitle = MutableStateFlow<String>("")
    val agonyRepository = AgonyRepository()

    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_agony_edit)
        with(binding){
            lifecycleOwner = this@AgonyEditActivity
            activity = this@AgonyEditActivity
        }
        oldAgony = getAgony()
        agonyTitle.value = oldAgony.title
        book = getBook()

        setFocus()
    }

    private fun setFocus(){
        binding.agonyTitleEditText.requestFocus()
        openKeyboard(binding.agonyTitleEditText)
    }

    private fun openKeyboard(view : View) {
        Handler(Looper.getMainLooper()).postDelayed({
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }, SignUpActivity.DELAY_TIME)
    }

    private fun getAgony() = intent.getSerializableExtra(EXTRA_AGONY) as Agony
    private fun getBook() = intent.getSerializableExtra(AgonyActivity.EXTRA_BOOK) as BookShelfItem

    fun clickConfirmBtn(){
        if (oldAgony.title == agonyTitle.value) {
            finish()
            return
        }
        if(agonyTitle.value.isBlank()){
            makeToast(R.string.agony_title_empty)
            return
        }
        reviseAgony(agonyTitle.value.trim())
    }

    private fun reviseAgony(newTitle :String) = lifecycleScope.launch{
        runCatching { agonyRepository.reviseAgony(bookShelfId = book.bookShelfId, agony = oldAgony, newTitle = newTitle) }
            .onSuccess {
                makeToast(R.string.agony_title_edit_success)
                moveToPreviousActivity(newTitle)
            }
            .onFailure { makeToast(R.string.agony_title_edit_fail) }
    }

    private fun moveToPreviousActivity(newTitle :String){
        val intent = Intent(this@AgonyEditActivity, AgonyRecordActivity::class.java)
        intent.putExtra(EXTRA_NEW_AGONY_TITLE, newTitle)
        setResult(RESULT_OK,intent)
        finish()
    }

    fun clickXBtn(){
        finish()
    }

    fun clickDeleteTextBtn() {
        agonyTitle.value = ""
    }

    private fun makeToast(stringId :Int){
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val EXTRA_NEW_AGONY_TITLE = "EXTRA_NEW_AGONY_TITLE"
    }
}