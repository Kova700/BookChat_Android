package com.example.bookchat.data

interface AgonyRecordItem{
    override fun equals(other: Any?): Boolean
}

data class AgonyRecordHeader(val agony: Agony) :AgonyRecordItem
data class AgonyRecordFirstItem(val agony: Agony) :AgonyRecordItem //임시로 Agony넣음
data class AgonyRecordDataItem(
    val agonyRecord : AgonyRecord
) :AgonyRecordItem