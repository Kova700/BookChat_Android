package com.example.bookchat.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat
	get() = SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.getDefault())

//String = Date String (DATE_AND_TIME_FORMAT)
fun String.toDate(): Date? =
	runCatching { dateFormat.parse(this) }.getOrNull()

//Long = timeStamp
fun Long.toDateString(): String? =
	runCatching { dateFormat.format(Date(this)) }.getOrNull()

fun getCurrentDateTimeString(): String = dateFormat.format(Date())

private fun String.getDateString() = split(T).firstOrNull()
private fun String.getTimeString() = split(T).lastOrNull()

fun getDateKoreanString(dateTimeString: String): String {
	val (year, month, day) = dateTimeString.getDateString()?.split(HYPHEN) ?: return ""
	return "${year}$YEAR ${month.toInt()}$MONTH  ${day.toInt()}$DAY " +
					getWeekKoreanString(dateTimeString)
}

private fun getWeekKoreanString(dateTimeString: String): String {
	val dayNum = Calendar.getInstance()
		.apply { time = dateTimeString.toDate() ?: return "" }
		.get(Calendar.DAY_OF_WEEK)

	return when (dayNum) {
		1 -> SUNDAY; 2 -> MONDAY; 3 -> TUESDAY
		4 -> WEDNESDAY; 5 -> THURSDAY; 6 -> FRIDAY
		7 -> SATURDAY; else -> ""
	}
}

fun getFormattedDetailDateTimeText(dateTimeString: String): String =
	when {
		isToday(dateTimeString) -> getDetailFormattedTodayText(dateTimeString)
		isYesterday(dateTimeString) -> getFormattedYesterdayText()
		isThisYear(dateTimeString) -> getFormattedThisYearText(dateTimeString)
		else -> getFormattedElseYearText(dateTimeString)
	}

fun getFormattedAbstractDateTimeText(dateTimeString: String): String =
	when {
		isToday(dateTimeString) -> getAbstractFormattedTodayText(dateTimeString)
		isYesterday(dateTimeString) -> getFormattedYesterdayText()
		isThisYear(dateTimeString) -> getFormattedThisYearText(dateTimeString)
		else -> getFormattedElseYearText(dateTimeString)
	}

fun getFormattedTimeText(dateTimeString: String): String =
	getDetailFormattedTodayText(dateTimeString)

private fun getAbstractFormattedTodayText(dateTimeString: String): String {
	val (cTime, cMinute) = getCurrentDateTimeString().getTimeString()?.split(COLON)
		?.dropLast(1)?.map { it.toInt() } ?: return ""
	val (iTime, iMinute) = dateTimeString.getTimeString()?.split(COLON)
		?.dropLast(1)?.map { it.toInt() } ?: return ""

	if ((cTime == iTime) && (cMinute - iMinute <= 5)) return JUST_AGO
	if ((cTime == iTime) && (cMinute - iMinute <= 30)) return THIRTY_AGO
	return (cTime - iTime).toString() + HOUR_AGO
}

private fun getDetailFormattedTodayText(dateTimeString: String): String {
	val (time, minute, second) = dateTimeString.getTimeString()?.split(COLON) ?: return ""
	return when (val flag = if (time.toInt() >= 12) PM else AM) {
		PM -> flag + SPACE + getPmTime(time.toInt()) + COLON + minute
		AM -> flag + SPACE + getAmTime(time.toInt()) + COLON + minute
		else -> return ""
	}
}

private fun getFormattedYesterdayText() = YESTERDAY

private fun getFormattedThisYearText(dateTimeString: String): String {
	val (iMonth, iDay) = dateTimeString.getDateString()?.split(HYPHEN)?.drop(1) ?: return ""
	return iMonth.toInt().toString() + MONTH + iDay.toInt() + DAY
}

private fun getFormattedElseYearText(dateTimeString: String): String {
	val (iYear, iMonth, iDay) = dateTimeString.getDateString()?.split(HYPHEN) ?: return ""
	return "$iYear.$iMonth.$iDay"
}

private fun getPmTime(time: Int): Int = if (time - 12 == 0) 12 else time - 12
private fun getAmTime(time: Int): Int = if (time == 0) 12 else time

private fun isToday(dateTimeString: String): Boolean {
	val (cYear, cMonth, cDay) = getCurrentDateTimeString().getDateString()?.split(HYPHEN)
		?: return false
	val (iYear, iMonth, iDay) = dateTimeString.getDateString()?.split(HYPHEN)
		?: return false
	return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
}

private fun isYesterday(dateTimeString: String): Boolean {
	val cYear: String
	val cMonth: String
	val cDay: String
	Calendar.getInstance().apply { add(Calendar.DATE, -1) }.also {
		cYear = it.get(Calendar.YEAR).toString()
		cMonth = (it.get(Calendar.MONTH) + 1).toString()
		cDay = it.get(Calendar.DATE).toString()
	}
	val (iYear, iMonth, iDay) = dateTimeString.getDateString()?.split(HYPHEN)
		?.map { it.toInt().toString() } ?: return false
	return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
}

private fun isThisYear(dateTimeString: String): Boolean {
	val cYear = getCurrentDateTimeString().getDateString()?.split(HYPHEN)?.first()
	val iYear = dateTimeString.getDateString()?.split(HYPHEN)?.first()
	return cYear == iYear
}

fun isSameDate(dateTimeString: String?, other: String?): Boolean {
	if (dateTimeString.isNullOrBlank() || other.isNullOrBlank()) return false
	val (year, month, day) = dateTimeString.getDateString()?.split(HYPHEN) ?: return false
	val (oYear, oMonth, oDay) = other.getDateString()?.split(HYPHEN) ?: return false
	return (year == oYear) && (month == oMonth) && (day == oDay)
}

private const val DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
private const val YESTERDAY = "어제"
private const val YEAR = "년"
private const val MONTH = "월"
private const val DAY = "일"
private const val AM = "오전"
private const val PM = "오후"
private const val JUST_AGO = "방금 전"
private const val THIRTY_AGO = "30분 전"
private const val HOUR_AGO = "시간 전"
private const val HYPHEN = "-"
private const val COLON = ":"
private const val SPACE = " "
private const val EMPTY = ""
private const val T = "T"
private const val SUNDAY = "일요일"
private const val MONDAY = "월요일"
private const val TUESDAY = "화요일"
private const val WEDNESDAY = "수요일"
private const val THURSDAY = "목요일"
private const val FRIDAY = "금요일"
private const val SATURDAY = "토요일"
