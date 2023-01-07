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
    fun getAgonyDataItem() :AgonyItem{
        return AgonyDataItem(this)
    }
}
