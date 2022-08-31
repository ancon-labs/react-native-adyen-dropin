package com.reactnativeadyendropin.data.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RecurringApiService {
  @POST()
  fun disable(@HeaderMap headers: Map<String, String>,
              @Url disableStoredPaymentMethodUrl: String,
              @QueryMap queryParameters: Map<String, String>,
              @Body disableRequest: RequestBody): Call<ResponseBody>
}
