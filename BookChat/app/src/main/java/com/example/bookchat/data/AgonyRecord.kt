package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class AgonyRecord(
    @SerializedName("agonyRecordId")
    val agonyRecordId: Long,
    @SerializedName("agonyRecordTitle")
    val agonyRecordTitle: String,
    @SerializedName("agonyRecordContent")
    val agonyRecordContent: String,
    @SerializedName("createdAt")
    val createdAt :String
){
    fun getAgonyRecordDataItem() :AgonyRecordDataItem{
        return AgonyRecordDataItem(this)
    }
}

data class AgonyRecordDataItem(
    val agonyRecord : AgonyRecord,
    var status :AgonyRecordDataItemStatus = AgonyRecordDataItemStatus.Default
)

sealed class AgonyRecordDataItemStatus{
    object Default :AgonyRecordDataItemStatus()
    object Editing :AgonyRecordDataItemStatus()
}

