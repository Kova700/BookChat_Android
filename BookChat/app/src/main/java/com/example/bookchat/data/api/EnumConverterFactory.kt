package com.example.bookchat.data.api

import com.google.gson.annotations.SerializedName
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
			return Converter<Any?, String> { value -> getSerializedNameValue(value as Enum<*>) }
		}
		return null
	}
}

fun <E : Enum<*>> getSerializedNameValue(e: E): String {
	try {
		return e.javaClass.getField(e.name).getAnnotation(SerializedName::class.java)?.value ?: ""
	} catch (exception: NoSuchFieldException) {
		exception.printStackTrace()
	}
	return ""
}