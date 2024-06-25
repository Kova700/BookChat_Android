package com.example.bookchat.notification

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmapOrNull
import com.example.bookchat.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

interface UserIconBuilder {
	suspend fun buildIcon(user: User): IconCompat?
}

//TODO : 이미지 load 라이브러리로 비트맵 생성되게 변경 (캐시 메모리 활요을 위해)
class UserIconBuilderImpl @Inject constructor(
	@ApplicationContext private val context: Context,
) : UserIconBuilder {
	override suspend fun buildIcon(user: User): IconCompat? {
		val profileUrl = user.profileImageUrl ?: return null
		if (profileUrl.isBlank()) return null

		return withContext(Dispatchers.IO) {
			runCatching {
				URL(profileUrl).openStream().use {
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