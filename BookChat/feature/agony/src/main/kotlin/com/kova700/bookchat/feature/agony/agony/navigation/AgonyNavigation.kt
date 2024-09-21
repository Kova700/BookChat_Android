package com.kova700.bookchat.feature.agony.agony.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.AgonyNavigator
import com.kova700.bookchat.feature.agony.agony.AgonyActivity
import com.kova700.bookchat.feature.agony.agony.AgonyViewModel.Companion.EXTRA_AGONY_BOOKSHELF_ITEM_ID
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class AgonyNavigatorImpl @Inject constructor() : AgonyNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
		bookshelfId: Long,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, AgonyActivity::class.java)
				.putExtra(EXTRA_AGONY_BOOKSHELF_ITEM_ID, bookshelfId)
				.intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface AgonyNavigatorModule {
	@Binds
	@Singleton
	fun bindAgonyNavigator(
		agonyNavigator: AgonyNavigatorImpl,
	): AgonyNavigator
}
