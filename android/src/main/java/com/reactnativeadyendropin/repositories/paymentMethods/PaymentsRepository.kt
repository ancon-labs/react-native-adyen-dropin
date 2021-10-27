package com.reactnativeadyendropin.repositories.paymentMethods

import com.reactnativeadyendropin.data.api.CheckoutApiService
import com.reactnativeadyendropin.repositories.BaseRepository
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

interface PaymentsRepository {
  fun paymentsRequest(headers: Map<String, String>, makePaymentUrl: String, paymentsRequest: RequestBody): Call<ResponseBody>
  fun detailsRequest(headers: Map<String, String>, makeDetailsCallUrl: String, paymentsRequest: RequestBody): Call<ResponseBody>
}

class PaymentsRepositoryImpl(private val checkoutApiService: CheckoutApiService) : PaymentsRepository, BaseRepository() {
  override fun paymentsRequest(headers: Map<String, String>, makePaymentUrl: String, paymentsRequest: RequestBody): Call<ResponseBody> {
    return checkoutApiService.payments(headers, makePaymentUrl, paymentsRequest)
  }

  override fun detailsRequest(headers: Map<String, String>, makeDetailsCallUrl: String, paymentsRequest: RequestBody): Call<ResponseBody> {
    return checkoutApiService.details(headers, makeDetailsCallUrl, paymentsRequest)
  }
}
