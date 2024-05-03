package com.example.bookchat.data.network.model

import com.google.gson.annotations.SerializedName

/**ECONOMY 	    : 경제,
PHILOSOPHY 	    : 철학,
HISTORY		    : 역사,
TRAVEL		    : 여행,
HEALTH		    : 건강,
HOBBY		    : 취미,
HUMANITIES	    : 인문,
NOVEL		    : 소설,
ART		        : 예술,
DESIGN		    : 디자인,
DEVELOPMENT	    : 개발,
SCIENCE		    : 과학,
MAGAZINE		: 잡지,
RELIGION		: 종교,
CHARACTER		: 인물,
 */
enum class ReadingTasteNetwork {
	@SerializedName("경제")
	ECONOMY,

	@SerializedName("철학")
	PHILOSOPHY,

	@SerializedName("역사")
	HISTORY,

	@SerializedName("여행")
	TRAVEL,

	@SerializedName("건강")
	HEALTH,

	@SerializedName("취미")
	HOBBY,

	@SerializedName("인문")
	HUMANITIES,

	@SerializedName("소설")
	NOVEL,

	@SerializedName("예술")
	ART,

	@SerializedName("디자인")
	DESIGN,

	@SerializedName("개발")
	DEVELOPMENT,

	@SerializedName("과학")
	SCIENCE,

	@SerializedName("잡지")
	MAGAZINE,

	@SerializedName("종교")
	RELIGION,

	@SerializedName("인물")
	CHARACTER;
}