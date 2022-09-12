package com.example.bookchat.utils
/** Economy 	: 경제,
Philosophy 	    : 철학,
History		    : 역사,
Travel		    : 여행,
Health		    : 건강,
Hobby		    : 취미,
Humanities	    : 인문,
Novel		    : 소설,
Art		        : 예술,
Design		    : 디자인,
Development	    : 개발,
Science		    : 과학,
Magazine		: 잡지,
Religion		: 종교,
Character		: 인물,
 */
enum class ReadingTaste(val taste :String) {
    Economy("경제"), Philosophy("철학"), History("역사"), Travel("여행"), Health("건상"), Hobby("취미"),
    Humanities("인문"), Novel("소설"), Art("예술"), Design("디자인"), Development("개발"), Science("과학"),
    Magazine("잡지"), Religion("종교"), Character("인물");
}
