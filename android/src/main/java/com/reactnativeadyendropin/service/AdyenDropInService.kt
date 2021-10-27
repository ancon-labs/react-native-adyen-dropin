package com.reactnativeadyendropin.service

import android.util.Log
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.redirect.RedirectComponent
import com.reactnativeadyendropin.data.storage.MemoryStorage
import com.reactnativeadyendropin.repositories.paymentMethods.PaymentsRepository
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call
import java.io.IOException

/**
 * This is just an example on how to make networkModule calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 */
class AdyenDropInService : DropInService() {

  companion object {
    private val TAG = "AdyenDropInService"
    private val CONTENT_TYPE: MediaType = "application/json".toMediaType()
  }

  private val paymentsRepository: PaymentsRepository by inject()
  private val memoryStorage: MemoryStorage by inject()

  override fun makePaymentsCall(paymentComponentJson: JSONObject): DropInServiceResult {
    Log.d(TAG, "makePaymentsCall")

    // Check out the documentation of this method on the parent DropInService class
    val paymentRequest = createPaymentRequest(
      paymentComponentJson,
      memoryStorage.shopperReference,
      memoryStorage.getAmount(),
      memoryStorage.countryCode,
      memoryStorage.merchantAccount,
      RedirectComponent.getReturnUrl(applicationContext),
      memoryStorage.getAdditionalData()
    )

    Log.d(TAG, "paymentComponentJson - \"${paymentComponentJson.toStringPretty()}\"")

    val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)

    val url = "${memoryStorage.baseUrl}${memoryStorage.makePaymentEndpoint}"

    val call = paymentsRepository.paymentsRequest(
      memoryStorage.headers,
      url,
      requestBody
    )

    val res = handleResponse(call)
    return res
  }

  override fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
    Log.d(TAG, "makeDetailsCall - \"${actionComponentJson.toStringPretty()}\"")

    val requestBody = actionComponentJson.toString().toRequestBody(CONTENT_TYPE)

    val url = "${memoryStorage.baseUrl}${memoryStorage.makeDetailsCallEndpoint}"

    val call = paymentsRepository.detailsRequest(
      memoryStorage.headers,
      url,
      requestBody
    )

    return handleResponse(call)
  }

  @Suppress("NestedBlockDepth")
  private fun handleResponse(call: Call<ResponseBody>): DropInServiceResult {
    Log.d(TAG, "handleResponse")
    return try {
      val response = call.execute()

      val byteArray = response.errorBody()?.bytes()
      if (byteArray != null) {
        Log.e(TAG, "errorBody - ${String(byteArray)}")
      }

      if (response.isSuccessful) {
        val detailsResponse = JSONObject(response.body()?.string() ?: "{}")
        if (detailsResponse.has("action")) {
          val action = detailsResponse.get("action").toString()
          DropInServiceResult.Action(action)
        } else {
          DropInServiceResult.Finished(detailsResponse.toStringPretty())
        }
      } else {
        Log.e(TAG, "FAILED - ${response.message()}")
        val jsonString = byteArray?.decodeToString() ?: "{ message: ${response.message()} }"
        val detailsResponse = JSONObject(jsonString)
        DropInServiceResult.Finished(detailsResponse.toStringPretty())
      }
    } catch (e: IOException) {
      Log.e(TAG, "IOException", e)
      DropInServiceResult.Error(reason = "IOException")
    }
  }
}
