package com.example.bookchat.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun ByteArray.toMultiPartBody(
	contentType: String,
	multipartName: String,
	fileName: String,
	fileExtension: String
): MultipartBody.Part {
	val imageRequestBody = toRequestBody(contentType.toMediaTypeOrNull(), 0, size)
	return MultipartBody.Part.createFormData(
		name = multipartName,
		filename = fileName + fileExtension,
		body = imageRequestBody
	)
}