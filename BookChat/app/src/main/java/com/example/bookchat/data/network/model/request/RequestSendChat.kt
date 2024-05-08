package com.example.bookchat.data.network.model.request

import com.google.gson.annotations.SerializedName

data class RequestSendChat(
    @SerializedName("receiptId")
    val receiptId :Long,
    @SerializedName("message")
    val message :String
)