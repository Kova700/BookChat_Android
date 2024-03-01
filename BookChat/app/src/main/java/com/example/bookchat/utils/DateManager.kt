package com.example.bookchat.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateManager {

	private fun stringToDate(dateString: String): Date? {
		val format = SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.getDefault())
		return format.parse(dateString)
	}

	fun getCurrentDateTimeString(): String =
		SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.getDefault())
			.format(Date())

	private fun getDateString(dateTimeString: String) =
		dateTimeString.split(T).first()

	private fun getTimeString(dateTimeString: String) =
		dateTimeString.split(T).last()

	fun getDateKoreanString(dateTimeString: String): String {
		val (year, month, day) = getDateString(dateTimeString).split(HYPHEN)
		return "${year}$YEAR ${month.toInt()}$MONTH  ${day.toInt()}$DAY " +
						getWeekKoreanString(dateTimeString)
	}

	private fun getWeekKoreanString(dateTimeString: String): String {
		var date: Date? = null
		runCatching { date = stringToDate(dateTimeString) }

		val dayNum = Calendar.getInstance()
			.apply { time = date ?: return "" }
			.get(Calendar.DAY_OF_WEEK)

		return when (dayNum) {
			1 -> SUNDAY; 2 -> MONDAY; 3 -> TUESDAY
			4 -> WEDNESDAY; 5 -> THURSDAY; 6 -> FRIDAY
			7 -> SATURDAY; else -> ""
		}
	}

	fun getFormattedDetailDateTimeText(dateTimeString: String): String =
		when {
			isToday(dateTimeString) -> {
				getDetailFormattedTodayText(dateTimeString)
			}

			isYesterday(dateTimeString) -> {
				getFormattedYesterdayText()
			}

			isThisYear(dateTimeString) -> {
				getFormattedThisYearText(dateTimeString)
			}

			else -> {
				getFormattedElseYearText(dateTimeString)
			}
		}

	fun getFormattedAbstractDateTimeText(dateTimeString: String): String =
		when {
			isToday(dateTimeString) -> {
				getAbstractFormattedTodayText(dateTimeString)
			}

			isYesterday(dateTimeString) -> {
				getFormattedYesterdayText()
			}

			else -> EMPTY
		}

	fun getFormattedTimeText(dateTimeString: String): String =
		getDetailFormattedTodayText(dateTimeString)

	private fun getAbstractFormattedTodayText(dateTimeString: String): String {
		val (cTime, cMinute) = getTimeString(getCurrentDateTimeString()).split(COLON)
			.dropLast(1).map { it.toInt() }
		val (iTime, iMinute) = getTimeString(dateTimeString).split(COLON)
			.dropLast(1).map { it.toInt() }
		if ((cTime == iTime) && (cMinute - iMinute <= 5)) return JUST_AGO
		if ((cTime == iTime) && (cMinute - iMinute <= 30)) return THIRTY_AGO
		return (cTime - iTime).toString() + HOUR_AGO
	}

	private fun getDetailFormattedTodayText(dateTimeString: String): String {
		val (time, minute, second) = getTimeString(dateTimeString).split(COLON)
		val flag = if (time.toInt() >= 12) PM else AM
		return when (flag) {
			PM -> flag + SPACE + getPmTime(time.toInt()) + COLON + minute
			AM -> flag + SPACE + getAmTime(time.toInt()) + COLON + minute
			else -> throw Exception("Time Formatting is Fail")
		}
	}

	private fun getFormattedYesterdayText() = YESTERDAY

	private fun getFormattedThisYearText(dateTimeString: String): String {
		val (iMonth, iDay) = getDateString(dateTimeString).split(HYPHEN).drop(1)
		return iMonth.toInt().toString() + MONTH + iDay.toInt() + DAY
	}

	private fun getFormattedElseYearText(dateTimeString: String): String {
		val (iYear, iMonth, iDay) = getDateString(dateTimeString).split(HYPHEN)
		return "$iYear.$iMonth.$iDay"
	}

	private fun getPmTime(time: Int): Int = if (time - 12 == 0) 12 else time - 12
	private fun getAmTime(time: Int): Int = if (time == 0) 12 else time

	private fun isToday(dateTimeString: String): Boolean {
		val (cYear, cMonth, cDay) = getDateString(getCurrentDateTimeString()).split(HYPHEN)
		val (iYear, iMonth, iDay) = getDateString(dateTimeString).split(HYPHEN)
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
		val (iYear, iMonth, iDay) = getDateString(dateTimeString).split(HYPHEN)
			.map { it.toInt().toString() }
		return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
	}

	private fun isThisYear(dateTimeString: String): Boolean {
		val cYear = getDateString(getCurrentDateTimeString()).split(HYPHEN).first()
		val iYear = getDateString(dateTimeString).split(HYPHEN).first()
		return cYear == iYear
	}

	fun isSameDate(dateTimeString: String?, other: String?): Boolean {
		if (dateTimeString.isNullOrBlank() || other.isNullOrBlank()) return false
		val (year, month, day) = getDateString(dateTimeString).split(HYPHEN)
		val (oYear, oMonth, oDay) = getDateString(other).split(HYPHEN)
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
}