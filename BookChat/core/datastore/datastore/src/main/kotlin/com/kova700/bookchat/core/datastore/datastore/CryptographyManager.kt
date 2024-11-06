package com.kova700.bookchat.core.datastore.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class CryptographyManager @Inject constructor() {
	private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

	private fun getKey(): SecretKey {
		val existingKey = keyStore.getEntry(KEY_STORE_ALIAS, null) as? KeyStore.SecretKeyEntry
		return existingKey?.secretKey ?: createKey()
	}

	private fun createKey(): SecretKey {
		return KeyGenerator.getInstance(ALGORITHM).apply {
			init(
				KeyGenParameterSpec.Builder(
					KEY_STORE_ALIAS,
					KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
				).setBlockModes(BLOCK_MODE)
					.setEncryptionPaddings(PADDING)
					.setUserAuthenticationRequired(false)
					.setRandomizedEncryptionRequired(true)
					.build()
			)
		}.generateKey()
	}

	fun encrypt(text: String): String {
		val encryptCipher = getEncryptCipher()
		val encryptedBytes = encryptCipher.doFinal(text.encodeToByteArray())
		return "${encryptCipher.iv.encode()}$DELIMITER${encryptedBytes.encode()}"
	}

	fun decrypt(text: String): String {
		val encryptedData = text.split(DELIMITER)
		if (encryptedData.size != 2) throw IllegalArgumentException("Inappropriate text format for decryption data :$text")
		val (encryptIv, encryptTarget) = encryptedData.map { it.decode() }
		return getDecryptCipherForIv(encryptIv).doFinal(encryptTarget).decodeToString()
	}

	private fun getEncryptCipher(): Cipher {
		return Cipher.getInstance(TRANSFORMATION).apply {
			init(Cipher.ENCRYPT_MODE, getKey())
		}
	}

	private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
		return Cipher.getInstance(TRANSFORMATION).apply {
			init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
		}
	}

	private fun ByteArray.encode(): String =
		Base64.encodeToString(this, Base64.DEFAULT)

	private fun String.decode(): ByteArray =
		Base64.decode(this, Base64.DEFAULT)

	companion object {
		private const val ANDROID_KEY_STORE = "AndroidKeyStore"
		private const val KEY_STORE_ALIAS = "secret"
		private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
		private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
		private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
		private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
		private const val DELIMITER = "|"
	}
}