package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.bookchat.App
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.AgonyRecordDataItem
import com.example.bookchat.data.AgonyRecordFirstItemStatus
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.paging.AgonyRecordPagingSource
import com.example.bookchat.repository.AgonyRecordRepository
import com.example.bookchat.utils.SearchSortOption
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgonyRecordViewModel @AssistedInject constructor(
    private val agonyRecordRepository: AgonyRecordRepository,
    @Assisted val agonyDataItem: AgonyDataItem,
    @Assisted val book :BookShelfItem
) :ViewModel() {

    private val _eventFlow = MutableSharedFlow<AgonyRecordUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val firstItemState = MutableStateFlow<AgonyRecordFirstItemStatus>(AgonyRecordFirstItemStatus.Default)
    val firstItemTitle = MutableStateFlow<String>("")
    val firstItemContent = MutableStateFlow<String>("")

    val agonyRecordModificationEvents = MutableStateFlow<List<PagingViewEvent>>(emptyList())

    val agonyRecordPagingData = Pager(
        config = PagingConfig(
            pageSize = AGONY_RECORD_LOAD_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { AgonyRecordPagingSource(agonyDataItem.agony, book, SearchSortOption.DESC) }
    ).flow
        .map { pagingData -> pagingData.map { agonyRecord -> agonyRecord.getAgonyRecordDataItem() } }
        .cachedIn(viewModelScope)

    val agonyRecordCombined by lazy {
        agonyRecordPagingData.combine(agonyRecordModificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event -> applyEvents(acc, event) }
        }.cachedIn(viewModelScope).asLiveData()
    }

    private fun makeAgonyRecord(title :String, content :String) = viewModelScope.launch {
        val bookShelfId = book.bookShelfId
        val agonyId = agonyDataItem.agony.agonyId
        runCatching { agonyRecordRepository.makeAgonyRecord(bookShelfId, agonyId, title, content) }
            .onSuccess {
                clearFirstItemData()
                renewFirstItemUi(AgonyRecordFirstItemStatus.Default)
                startEvent(AgonyRecordUiEvent.RefreshDataItemAdapter)
            }
            .onFailure {
                makeToast("고민기록 등록을 실패했습니다.")
                renewFirstItemUi(AgonyRecordFirstItemStatus.Editing)
            }
    }

    fun clickFirstItemXBtn(){
        if (isFirstItemEditing()){
            startEvent(AgonyRecordUiEvent.ShowWarningDialog)
            return
        }
        renewFirstItemUi(AgonyRecordFirstItemStatus.Default)
    }

    fun clickFirstItemSubmitBtn(){
        if(isBlankFirstItemValue()) {
            makeToast("제목, 내용을 입력해주세요.")
            return
        }
        renewFirstItemUi(AgonyRecordFirstItemStatus.Loading)
        makeAgonyRecord(firstItemTitle.value.trim(), firstItemContent.value.trim())
    }

    fun clickReviseBtn(){
        //수정된 정보 파라미터로 넘겨받아서 API호출
        //성공시 ModificationEvent에 Edit이벤트 넣어야함
        //실패시 makeToast("고민기록 수정을 실패했습니다.")
    }

    fun clickBackBtn(){
        if (isFirstItemEditing()){
            startEvent(AgonyRecordUiEvent.ShowWarningDialog)
            return
        }
        startEvent(AgonyRecordUiEvent.MoveToBack)
    }

    fun clickFolderBtn(){
        makeToast("폴더 버튼 클릭 : 아래에서 SlidingWindow 올라 옴")
    }

    fun clearFirstItemData(){
        firstItemTitle.value = ""
        firstItemContent.value = ""
    }

    fun renewFirstItemUi(newState :AgonyRecordFirstItemStatus){
        firstItemState.value = newState
        startEvent(AgonyRecordUiEvent.RenewFirstItemAdapter)
    }

    private fun isBlankFirstItemValue() :Boolean =
        firstItemTitle.value.trim().isBlank() || firstItemContent.value.trim().isBlank()

    fun isFirstItemEditing() :Boolean =
        firstItemTitle.value.isNotBlank() || firstItemContent.value.isNotBlank()

    private fun makeToast(text :String){
        Toast.makeText(App.instance.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private fun startEvent (event : AgonyRecordUiEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    private fun applyEvents(
        paging: PagingData<AgonyRecordDataItem>,
        pagingViewEvent : PagingViewEvent
    ): PagingData<AgonyRecordDataItem> {
        return when(pagingViewEvent){
            is PagingViewEvent.ChangeItemStatusToEdit -> { paging } //임시
            is PagingViewEvent.RemoveItem -> { paging } //임시
        }
    }

    sealed class PagingViewEvent{
        data class ChangeItemStatusToEdit(val agonyRecordItem : AgonyRecordDataItem) : PagingViewEvent()
        data class RemoveItem(val agonyRecordItem : AgonyRecordDataItem) : PagingViewEvent()
    }

    sealed class AgonyRecordUiEvent{
        object MoveToBack :AgonyRecordUiEvent()
        object RenewAgonyList :AgonyRecordUiEvent()
        object ShowWarningDialog :AgonyRecordUiEvent()
        object RenewFirstItemAdapter :AgonyRecordUiEvent()
        object RefreshDataItemAdapter :AgonyRecordUiEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(
            agonyDataItem: AgonyDataItem,
            book :BookShelfItem
        ) :AgonyRecordViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            agonyDataItem: AgonyDataItem,
            book :BookShelfItem
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(agonyDataItem, book) as T
            }
        }

        const val AGONY_RECORD_LOAD_SIZE = 4
    }
}