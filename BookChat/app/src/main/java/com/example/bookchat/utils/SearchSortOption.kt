package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class SearchSortOption {
    @SerializedName("id,DESC")
    ID_DESC,            //ID기준 내림차순 (=최신순)
    @SerializedName("id,ASC")
    ID_ASC,             //ID기준 오른차순 (=등록순)
    @SerializedName("updatedAt,DESC")
    UPDATED_AT_DESC,     //변경일기준 내림차순 (=최근 변경순)
    @SerializedName("updatedAt,ASC")
    UPDATED_AT_ASC,      //변경일기준 오름차순 (=오래된 변경순)
}