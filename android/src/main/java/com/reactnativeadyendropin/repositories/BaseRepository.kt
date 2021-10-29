package com.reactnativeadyendropin.repositories

import okhttp3.ResponseBody
import retrofit2.Response

open class BaseRepository {

  suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String = ""): T? {

    val result: Result<T> = safeApiResult(call, errorMessage)
    var data: T? = null

    when (result) {
      is Result.Success ->
        data = result.data
    }

    return data
  }

  private suspend fun <T : Any> safeApiResult(call: suspend () -> Response<T>, errorMessage: String = ""): Result<T> {
    val response = call.invoke()
    if (response.isSuccessful) return Result.Success(response.body()!!)

    return Result.Error(response.errorBody())
  }
}

sealed class Result<out T : Any> {
  data class Success<out T : Any>(val data: T) : Result<T>()
  data class Error(val exception: ResponseBody?) : Result<Nothing>()
}
