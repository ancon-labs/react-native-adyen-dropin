package com.reactnativeadyendropin.repositories

import com.reactnativeadyendropin.data.api.RecurringApiService
import retrofit2.Call
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface RecurringRepository {
  fun removeStoredPaymentMethod(
    headers: Map<String, String>,
    queryParameters: Map<String, String>,
    disableUrl: String,
    requestBody: RequestBody): Call<ResponseBody>
}

class RecurringRepositoryImpl(private val recurringApiService: RecurringApiService): RecurringRepository, BaseRepository() {
  override fun removeStoredPaymentMethod(
    headers: Map<String, String>,
    queryParameters: Map<String, String>,
    disableUrl: String,
    requestBody: RequestBody): Call<ResponseBody> {
    return recurringApiService.disable(headers, disableUrl, queryParameters, requestBody)
  }
}
