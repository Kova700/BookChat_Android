package com.kova700.bookchat.core.network.network.converter

import android.os.Build
import com.kova700.bookchat.core.data.util.model.network.BookChatApiResult
import okhttp3.Request
import okio.IOException
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultCallAdapterFactory : CallAdapter.Factory() {
	override fun get(
		returnType: Type,
		annotations: Array<out Annotation>,
		retrofit: Retrofit,
	): CallAdapter<*, *>? {

		if (getRawType(returnType) != Call::class.java) return null
		check(returnType is ParameterizedType) {
			val name = returnType.parseTypeName()
			"The Return type must be defined as BookChatApiResponse<Foo> or BookChatApiResponse<out Foo>. currentType : $name"
		}

		val wrapperType = getParameterUpperBound(0, returnType)
		if (getRawType(wrapperType) != BookChatApiResult::class.java) return null
		check(wrapperType is ParameterizedType) {
			val name = returnType.parseTypeName()
			"Return type must be defined as BookChatApiResponse<ResponseBody>. currentType : $name"
		}

		val bodyType = getParameterUpperBound(0, wrapperType)
		return ApiResultCallAdapter<Any>(bodyType)
	}

	private fun Type.parseTypeName(): String {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) typeName
		else this.toString()
	}
}

internal class ApiResultCallAdapter<R>(
	private val successType: Type,
) : CallAdapter<R, Call<BookChatApiResult<R>>> {

	override fun adapt(call: Call<R>): Call<BookChatApiResult<R>> =
		BookChatApiCall(call, successType)

	override fun responseType(): Type = successType
}

private class BookChatApiCall<R>(
	private val delegate: Call<R>,
	private val successType: Type,
) : Call<BookChatApiResult<R>> {

	override fun enqueue(callback: Callback<BookChatApiResult<R>>) = delegate.enqueue(
		object : Callback<R> {
			override fun onResponse(call: Call<R>, response: Response<R>) {
				callback.onResponse(
					this@BookChatApiCall,
					Response.success(response.toBookChatApiResult())
				)
			}

			override fun onFailure(call: Call<R?>, throwable: Throwable) {
				throw throwable
			}
		}
	)

	@Suppress("UNCHECKED_CAST")
	private fun Response<R>.toBookChatApiResult(): BookChatApiResult<R> {
		val body = body()
		return when {
			isSuccessful.not() ->
				BookChatApiResult.Failure(
					code = code(),
					headers = headers().toMap(),
					message = message(),
					body = errorBody()?.string() ?: ""
				)

			body != null -> BookChatApiResult.Success(
				code = code(),
				headers = headers().toMap(),
				data = body,
			)

			successType == Unit::class.java -> BookChatApiResult.Success(
				code = code(),
				headers = headers().toMap(),
				data = Unit as R,
			)

			else -> throw IllegalStateException(
				"The Body does not exist, but it was defined as a type other than Unit. Define it as BookChatApiResponse<Unit>"
			)
		}
	}

	override fun clone(): Call<BookChatApiResult<R>> {
		return BookChatApiCall(delegate.clone(), successType)
	}

	override fun execute(): Response<BookChatApiResult<R>> {
		val response = delegate.execute()
		return Response.success(response.toBookChatApiResult())
	}

	override fun isExecuted(): Boolean = delegate.isExecuted
	override fun cancel() = delegate.cancel()
	override fun isCanceled(): Boolean = delegate.isCanceled
	override fun request(): Request = delegate.request()
	override fun timeout(): Timeout = delegate.timeout()
}