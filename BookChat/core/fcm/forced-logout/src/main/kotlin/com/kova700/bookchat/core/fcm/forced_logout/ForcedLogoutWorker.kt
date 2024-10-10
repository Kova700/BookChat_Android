package com.kova700.bookchat.core.fcm.forced_logout

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kova700.bookchat.core.fcm.forced_logout.model.ForcedLogoutReason
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ForcedLogoutWorker @AssistedInject constructor(
	@Assisted appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val forcedLogoutManager: ForcedLogoutManager,
) : CoroutineWorker(appContext, workerParams) {

	override suspend fun doWork(): Result {
		val reason = ForcedLogoutReason.valueOf(
			inputData.getString(FORCED_LOGOUT_REASON) ?: return Result.failure()
		)
		when (reason) {
			ForcedLogoutReason.CHANGE_DEVICE -> forcedLogoutManager.onDeviceChanged()
			ForcedLogoutReason.TOKEN_EXPIRED -> forcedLogoutManager.onBookChatTokenExpired()
		}
		return Result.success()
	}

	companion object {
		private const val LOGOUT_WORK_NAME = "LOGOUT_WORK_NAME"
		private const val FORCED_LOGOUT_REASON = "FORCED_LOGOUT_REASON"

		fun start(
			context: Context,
			reason: ForcedLogoutReason
		) {
			val logoutWork = OneTimeWorkRequestBuilder<ForcedLogoutWorker>()
				.setInputData(workDataOf(FORCED_LOGOUT_REASON to reason.toString()))
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