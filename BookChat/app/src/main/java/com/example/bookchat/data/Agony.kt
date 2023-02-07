package com.example.bookchat.data

import com.example.bookchat.utils.AgonyFolderHexColor
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Agony(
    @SerializedName("agonyId")
    val agonyId :Long,
    @SerializedName("title")
    val title :String,
    @SerializedName("hexColorCode")
    val hexColorCode : AgonyFolderHexColor
): Serializable {
    fun getAgonyDataItem() = AgonyDataItem(this)
}

data class AgonyDataItem(
    val agony :Agony,
    var status :AgonyDataItemStatus = AgonyDataItemStatus.Default
): Serializable

sealed class AgonyDataItemStatus : Serializable{
    object Default :AgonyDataItemStatus()
    object Editing :AgonyDataItemStatus()
    object Selected :AgonyDataItemStatus()
}
