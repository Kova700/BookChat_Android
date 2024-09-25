package com.kova700.bookchat.util.emoji

import android.icu.text.BreakIterator

fun String.isSingleTextOrEmoji(): Boolean {
	val boundary = BreakIterator.getCharacterInstance().apply {
		setText(this@isSingleTextOrEmoji)
	}
	var count = 0
	while (boundary.next() != BreakIterator.DONE) {
		count++
		if (count > 1) return false
	}
	return true
}