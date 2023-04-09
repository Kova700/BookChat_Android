package com.example.bookchat.utils

import java.text.SimpleDateFormat
import java.util.*

object DateManager {

    private fun getCurrentDateTimeString(): String =
        SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.getDefault())
            .format(Date()).replace(SPACE, T)

    private fun getDateString(dateTimeString: String) =
        dateTimeString.split(T).first()

    private fun getTimeString(dateTimeString: String) =
        dateTimeString.split(T).last()

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
        val timeList = getTimeString(dateTimeString).split(COLON)
        val flag = if (timeList[0].toInt() >= 12) PM else AM
        return when (flag) {
            PM -> flag + SPACE + getPmTime(timeList[0].toInt()) + COLON + timeList[1]
            AM -> flag + SPACE + timeList[0].toInt() + COLON + timeList[1]
            else -> throw Exception("Time Formatting is Fail")
        }
    }

    private fun getFormattedYesterdayText() = YESTERDAY

    private fun getFormattedThisYearText(dateTimeString: String): String {
        val (iMonth, iDay) = getDateString(dateTimeString).split(HYPHEN).drop(1)
        return iMonth.toInt().toString() + MONTH + iDay.toInt()
    }

    private fun getFormattedElseYearText(dateTimeString: String): String {
        val (iYear, iMonth, iDay) = getDateString(dateTimeString).split(HYPHEN)
        return "$iYear.$iMonth.$iDay"
    }

    private fun getPmTime(time: Int): Int =
        if (time - 12 == 0) 12 else time - 12

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

    private const val DATE_AND_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss"
    private const val YESTERDAY = "어제"
    private const val MONTH = "월"
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
}