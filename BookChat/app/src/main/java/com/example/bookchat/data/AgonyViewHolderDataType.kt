package com.example.bookchat.data

interface AgonyItem {
    override fun equals(other: Any?): Boolean
}

data class AgonyHeader(val bookShelfItem : BookShelfItem) :AgonyItem //도서 정보 (표지 , 제목 , 작가)
data class AgonyFirstItem(val bookShelfItem : BookShelfItem) :AgonyItem //(+ 버튼) // 임시로 BookShelfItem 넣음
data class AgonyDataItem(
    val agony :Agony,
    var status :AgonyDataItemStatus = AgonyDataItemStatus.Default
) :AgonyItem //고민명(폴더명)

sealed class AgonyDataItemStatus{
    object Default :AgonyDataItemStatus()
    object Editing :AgonyDataItemStatus()
    object Selected :AgonyDataItemStatus()
}