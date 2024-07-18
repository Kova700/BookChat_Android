package com.example.bookchat.fcm.renewfcmtoken

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.FCMTokenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RenewFcmTokenWorker @AssistedInject constructor(
	@Assisted appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val fcmTokenRepository: FCMTokenRepository,
) : CoroutineWorker(appContext, workerParams) {

	override suspend fun doWork(): Result {
		if (bookChatTokenRepository.isBookChatTokenExist().not()) return Result.success()

		val fcmTokenString: String = inputData.getString(EXTRA_FCM_TOKEN) ?: return Result.failure()
		runCatching { fcmTokenRepository.renewFCMToken(FCMToken(fcmTokenString)) }
			.onSuccess { return Result.success() }
		return Result.success()
	}

	companion object {
		private const val RENEW_FCM_TOKEN_WORK_NAME = "RENEW_FCM_TOKEN_WORK_NAME"
		private const val EXTRA_FCM_TOKEN = "EXTRA_FCM_TOKEN"

		fun start(
			context: Context,
			fcmTokenString: String,
		) {
			val logoutWork = OneTimeWorkRequestBuilder<RenewFcmTokenWorker>()
				.setInputData(
					workDataOf(
						EXTRA_FCM_TOKEN to fcmTokenString
					)
				)
				.build()

			WorkManager
				.getInstance(context)
				.enqueueUniqueWork(
					RENEW_FCM_TOKEN_WORK_NAME,
					ExistingWorkPolicy.REPLACE,
					logoutWork
				)
		}
	}
}