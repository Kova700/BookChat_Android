package com.kova700.bookchat.feature.bookreport.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.BookReportNavigator
import com.kova700.bookchat.feature.bookreport.BookReportActivity
import com.kova700.bookchat.feature.bookreport.BookReportActivity.Companion.EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class BookReportNavigatorImpl @Inject constructor() : BookReportNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
		bookshelfId: Long,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, BookReportActivity::class.java)
				.putExtra(EXTRA_BOOKREPORT_BOOKSHELF_ITEM_ID, bookshelfId)
				.intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface BookReportNavigatorModule {
	@Binds
	@Singleton
	fun bindBookReportNavigator(
		bookReportNavigator: BookReportNavigatorImpl,
	): BookReportNavigator
}
