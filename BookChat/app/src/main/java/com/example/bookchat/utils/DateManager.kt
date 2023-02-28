package com.example.bookchat.utils

import java.text.SimpleDateFormat
import java.util.*

object DateManager {
    private fun getCurrentDateAndTimeString(): String =
        SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.getDefault())
            .format(Date()).replace(SPACE, T)

    private fun getDateString(dateAndTimeString: String) =
        dateAndTimeString.split(T).first()

    private fun getTimeString(dateAndTimeString: String) =
        dateAndTimeString.split(T).last()

    fun getFormattingText(inputedDateAndTimeString: String): String = when {
        isToday(inputedDateAndTimeString) -> {
            getTodayFormattingText(inputedDateAndTimeString)
        }
        isYesterday(inputedDateAndTimeString) -> {
            getYesterdayFormattingText()
        }
        isThisYear(inputedDateAndTimeString) -> {
            getThisYearFormattingText(inputedDateAndTimeString)
        }
        else -> {
            getElseYearFormattingText(inputedDateAndTimeString)
        }
    }

    private fun getTodayFormattingText(inputedDateAndTimeString: String): String {
        val timeList = getTimeString(inputedDateAndTimeString).split(COLON)
        val flag = if (timeList[0].toInt() >= 12) PM else AM
        return when (flag) {
            PM -> flag + SPACE + getPmTime(timeList[0].toInt()) + COLON + timeList[1]
            AM -> flag + SPACE + timeList[0].toInt() + COLON + timeList[1]
            else -> throw Exception("Time Formatting is Fail")
        }
    }

    private fun getYesterdayFormattingText() = YESTERDAY

    private fun getThisYearFormattingText(inputedDateAndTimeString: String): String {
        val (iMonth, iDay) = getDateString(inputedDateAndTimeString).split(HYPHEN).drop(1)
        return iMonth.toInt().toString() + MONTH + iDay.toInt()
    }

    private fun getElseYearFormattingText(inputedDateAndTimeString: String): String {
        val (iYear, iMonth, iDay) = getDateString(inputedDateAndTimeString).split(HYPHEN)
        return "$iYear.$iMonth.$iDay"
    }

    private fun getPmTime(time: Int): Int =
        if (time - 12 == 0) 12 else time - 12

    private fun isToday(inputedDateAndTimeString: String): Boolean {
        val (cYear, cMonth, cDay) = getDateString(getCurrentDateAndTimeString()).split(HYPHEN)
        val (iYear, iMonth, iDay) = getDateString(inputedDateAndTimeString).split(HYPHEN)
        return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
    }

    private fun isYesterday(inputedDateAndTimeString: String): Boolean {
        val cYear: String
        val cMonth: String
        val cDay: String
        Calendar.getInstance().apply { add(Calendar.DATE, -1) }.also {
            cYear = it.get(Calendar.YEAR).toString()
            cMonth = (it.get(Calendar.MONTH) +1).toString()
            cDay = it.get(Calendar.DATE).toString()
        }
        val (iYear, iMonth, iDay) = getDateString(inputedDateAndTimeString).split(HYPHEN)
            .map { it.toInt().toString() }
        return (cYear == iYear) && (cMonth == iMonth) && (cDay == iDay)
    }

    private fun isThisYear(inputedDateAndTimeString: String): Boolean {
        val cYear = getDateString(getCurrentDateAndTimeString()).split(HYPHEN).first()
        val iYear = getDateString(inputedDateAndTimeString).split(HYPHEN).first()
        return cYear == iYear
    }

    private const val DATE_AND_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss"
    private const val YESTERDAY = "어제"
    private const val MONTH = "월"
    private const val AM = "오전"
    private const val PM = "오후"
    private const val HYPHEN = "-"
    private const val COLON = ":"
    private const val SPACE = " "
    private const val T = "T"
}