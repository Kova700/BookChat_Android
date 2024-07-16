package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.repository.ClientRepository
import javax.inject.Inject

class WithdrawUseCase @Inject constructor(
	private val clientRepository: ClientRepository,
	private val clearAllDataUseCase: ClearAllDataUseCase,
) {
	suspend operator fun invoke() {
		clientRepository.withdraw()
		clearAllDataUseCase()
	}
}