package com.kova700.bookchat.util.date

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat
	get() = SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.getDefault())

/** String = Date String (DATE_AND_TIME_FORMAT) */
fun String.toDate(): Date? =
	runCatching { dateFormat.parse(this) }.getOrNull()

/** Long = timeStamp */
fun Long.toDateString(): String? =
	runCatching { dateFormat.format(Date(this)) }.getOrNull()

fun getCurrentDateTimeString(): String = dateFormat.format(Date())

private fun String.toDateString() = split(T).firstOrNull()
private fun String.toTimeString() = split(T).lastOrNull()

fun String.toDateKoreanString(): String {
	val (year, month, day) = this.toDateString()?.split(HYPHEN) ?: return EMPTY
	return "${year}$YEAR ${month.toInt()}$MONTH  ${day.toInt()}$DAY " +
					toWeekKoreanString()
}

private fun String.toWeekKoreanString(): String {
	val dayNum = Calendar.getInstance()
		.apply { time = toDate() ?: return EMPTY }
		.get(Calendar.DAY_OF_WEEK)

	return when (dayNum) {
		1 -> SUNDAY; 2 -> MONDAY; 3 -> TUESDAY
		4 -> WEDNESDAY; 5 -> THURSDAY; 6 -> FRIDAY
		7 -> SATURDAY; else -> EMPTY
	}
}

fun String.toFormattedDetailDateTimeText(): String =
	when {
		isBlank() -> EMPTY
		isToday() -> toDetailFormattedTodayText()
		isYesterday() -> YESTERDAY
		isThisYear() -> toFormattedThisYearText()
		else -> toFormattedElseYearText()
	}

fun String.toFormattedAbstractDateTimeText(): String =
	when {
		isBlank() -> EMPTY
		isToday() -> toAbstractFormattedTodayText()
		isYesterday() -> YESTERDAY
		isThisYear() -> toFormattedThisYearText()
		else -> toFormattedElseYearText()
	}

fun String.toFormattedTimeText(): String = toDetailFormattedTodayText()

private fun String.toAbstractFormattedTodayText(): String {
	val (cTime, cMinute) = getCurrentDateTimeString().toTimeString()?.split(COLON)
		?.dropLast(1)?.map { it.toInt() } ?: return EMPTY
	val (iTime, iMinute) = this.toTimeString()?.split(COLON)
		?.dropLast(1)?.map { it.toInt() } ?: return EMPTY

	if ((cTime == iTime) && (cMinute - iMinute <= 5)) return JUST_AGO
	if ((cTime == iTime) && (cMinute - iMinute <= 30)) return THIRTY_AGO
	return (cTime - iTime).toString() + HOUR_AGO
}

private fun String.toDetailFormattedTodayText(): String {
	val (time, minute, second) = this.toTimeString()?.split(COLON) ?: return EMPTY
	return when (val flag = if (time.toInt() >= 12) PM else AM) {
		PM -> flag + SPACE + getPmTime(time.toInt()) + COLON + minute
		AM -> flag + SPACE + getAmTime(time.toInt()) + COLON + minute
		else -> return EMPTY
	}
}

private fun String.toFormattedThisYearText(): String {
	val (iMonth, iDay) = this.toDateString()?.split(HYPHEN)?.drop(1) ?: return EMPTY
	return iMonth.toInt().toString() + MONTH + iDay.toInt() + DAY
}

private fun String.toFormattedElseYearText(): String {
	val (iYear, iMonth, iDay) = this.toDateString()?.split(HYPHEN) ?: return EMPTY
	return "$iYear.$iMonth.$iDay"
}

private fun getPmTime(time: Int): Int = if (time - 12 == 0) 12 else time - 12
private fun getAmTime(time: Int): Int = if (time == 0) 12 else time

private fun String.isToday(): Boolean {
	val (cYear, cMonth, cDay) = getCurrentDateTimeString().toDateString()?.split(HYPHEN)
		?: return false
	val (iYear, iMonth, iDay) = this.toDateString()?.split(HYPHEN)
		?: return false
	return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
}

private fun String.isYesterday(): Boolean {
	val calendar = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
	val cYear = calendar.get(Calendar.YEAR).toString()
	val cMonth = (calendar.get(Calendar.MONTH) + 1).toString()
	val cDay = calendar.get(Calendar.DATE).toString()

	val (iYear, iMonth, iDay) = this.toDateString()?.split(HYPHEN)
		?.map { it.toInt().toString() } ?: return false
	return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
}

private fun String.isThisYear(): Boolean {
	val cYear = getCurrentDateTimeString().toDateString()?.split(HYPHEN)?.first()
	val iYear = this.toDateString()?.split(HYPHEN)?.first()
	return cYear == iYear
}

fun String?.isSameDate(other: String?): Boolean {
	if (this.isNullOrBlank() || other.isNullOrBlank()) return false
	val (year, month, day) = this.toDateString()?.split(HYPHEN) ?: return false
	val (oYear, oMonth, oDay) = other.toDateString()?.split(HYPHEN) ?: return false
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