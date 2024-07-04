package com.example.bookchat.notification

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmapOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

interface IconBuilder {
	suspend fun buildIcon(imageUrl: String?): IconCompat?
}

//TODO : 이미지 load 라이브러리로 비트맵 생성되게 변경 (캐시 메모리 활요을 위해)
//TODO : 스쿼클 모양으로 수정
class IconBuilderImpl @Inject constructor(
	@ApplicationContext private val context: Context,
) : IconBuilder {
	override suspend fun buildIcon(imageUrl: String?): IconCompat? {
		if (imageUrl.isNullOrBlank()) return null

		return withContext(Dispatchers.IO) {
			runCatching {
				URL(imageUrl).openStream().use {
					RoundedBitmapDrawableFactory.create(
						context.resources,
						BitmapFactory.decodeStream(it),
					)
						.apply { isCircular = true }
						.toBitmapOrNull()
				}
					?.let(IconCompat::createWithBitmap)
			}.getOrNull()
		}
	}
}