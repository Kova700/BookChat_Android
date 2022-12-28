package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class AgonyFolderHexColor(hexCode: String) {
    @SerializedName("#F6F6F6") WHITE("#F6F6F6"),
    @SerializedName("#595959") BLACK("#595959"),
    @SerializedName("#C972FF") PURPLE("#C972FF"),
    @SerializedName("#00CEDB") MINT("#00CEDB"),
    @SerializedName("#24D174") GREEN("#24D174"),
    @SerializedName("#FFE55C") YELLOW("#FFE55C"),
    @SerializedName("#FF9D43") ORANGE("#FF9D43")
}