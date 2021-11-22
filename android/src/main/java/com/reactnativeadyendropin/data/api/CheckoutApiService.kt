package com.reactnativeadyendropin.data.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CheckoutApiService {
  // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
  @POST()
  fun payments(@HeaderMap headers: Map<String, String>, @Url makePaymentUrl: String, @QueryMap queryParameters: Map<String, String>, @Body paymentsRequest: RequestBody): Call<ResponseBody>

  @POST()
  fun details(@HeaderMap headers: Map<String, String>, @Url makeDetailsCallUrl: String, @QueryMap queryParameters: Map<String, String>, @Body detailsRequest: RequestBody): Call<ResponseBody>
}
