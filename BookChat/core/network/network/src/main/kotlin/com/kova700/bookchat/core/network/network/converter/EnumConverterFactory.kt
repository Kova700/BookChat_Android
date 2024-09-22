package com.kova700.bookchat.core.network.network.converter

import kotlinx.serialization.SerialName
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Inject

class EnumConverterFactory @Inject constructor() : Converter.Factory() {

	override fun stringConverter(
		type: Type,
		annotations: Array<out Annotation>,
		retrofit: Retrofit
	): Converter<*, String>? {
		if (type is Class<*> && type.isEnum) {
			return Converter<Any?, String> { value -> getSerialNameValue(value as Enum<*>) }
		}
		return null
	}
}

fun <E : Enum<*>> getSerialNameValue(e: E): String {
	try {
		return e.javaClass.getField(e.name).getAnnotation(SerialName::class.java)?.value ?: ""
	} catch (exception: NoSuchFieldException) {
		exception.printStackTrace()
	}
	return ""
}