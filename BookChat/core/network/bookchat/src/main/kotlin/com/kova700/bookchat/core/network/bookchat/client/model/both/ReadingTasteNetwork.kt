package com.kova700.bookchat.core.network.bookchat.client.model.both

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
@Serializable
enum class ReadingTasteNetwork {
	@SerialName("경제")
	ECONOMY,

	@SerialName("철학")
	PHILOSOPHY,

	@SerialName("역사")
	HISTORY,

	@SerialName("여행")
	TRAVEL,

	@SerialName("건강")
	HEALTH,

	@SerialName("취미")
	HOBBY,

	@SerialName("인문")
	HUMANITIES,

	@SerialName("소설")
	NOVEL,

	@SerialName("예술")
	ART,

	@SerialName("디자인")
	DESIGN,

	@SerialName("개발")
	DEVELOPMENT,

	@SerialName("과학")
	SCIENCE,

	@SerialName("잡지")
	MAGAZINE,

	@SerialName("종교")
	RELIGION,

	@SerialName("인물")
	CHARACTER;
}