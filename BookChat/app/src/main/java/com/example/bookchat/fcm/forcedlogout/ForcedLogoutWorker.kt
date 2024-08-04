package com.example.bookchat.fcm.forcedlogout

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ForcedLogoutWorker @AssistedInject constructor(
	@Assisted appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val forcedLogoutManager: ForcedLogoutManager,
) : CoroutineWorker(appContext, workerParams) {

	override suspend fun doWork(): Result {
		forcedLogoutManager.onLogoutMessageReceived()
		return Result.success()
	}

	companion object {
		private const val LOGOUT_WORK_NAME = "LOGOUT_WORK_NAME"
		fun start(context: Context) {
			val logoutWork = OneTimeWorkRequestBuilder<ForcedLogoutWorker>()
				.build()

			WorkManager
				.getInstance(context)
				.enqueueUniqueWork(
					LOGOUT_WORK_NAME,
					ExistingWorkPolicy.REPLACE,
					logoutWork
				)
		}
	}
}