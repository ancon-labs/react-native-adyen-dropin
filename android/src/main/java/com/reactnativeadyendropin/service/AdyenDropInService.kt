package com.reactnativeadyendropin.service

import android.util.Log
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.redirect.RedirectComponent
import com.facebook.react.bridge.ReadableMap
import com.reactnativeadyendropin.RNUtils
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
    private var instance: AdyenDropInService? = null

    fun handleAsyncResponse(response: ReadableMap) {
      val resultCode = response.getString("resultCode")
      Log.d(TAG, "handleAsyncResponse - ${resultCode}")

      when (resultCode) {
        "Authorised",
        "Pending",
        "Received",
        "ChallengeShopper",
        "IdentifyShopper",
        "PresentToShopper",
        "RedirectShopper" -> {
          val action = response.getMap("action")
          if (action != null) {
            instance?.sendResult(DropInServiceResult.Action(JSONObject(action.toHashMap()).toString()))
          } else {
            instance?.sendResult(DropInServiceResult.Finished(JSONObject(response.toHashMap()).toString()))
          }
        }

        "Cancelled",
        "Error",
        "Refused" -> {
          instance?.sendResult(DropInServiceResult.Error(reason = resultCode))
        }
      }
    }
  }

  private val paymentsRepository: PaymentsRepository by inject()
  private val memoryStorage: MemoryStorage by inject()

  init {
    instance = this
  }

  override fun onPaymentsCallRequested(paymentComponentState: PaymentComponentState<*>, paymentComponentJson: JSONObject) {
    Log.d(TAG, "onPaymentsCallRequested")

    if (memoryStorage.onSubmitCallback == null) {
      super.onPaymentsCallRequested(paymentComponentState, paymentComponentJson)
      return
    }

    val event = RNUtils.jsonToWritableMap(paymentComponentJson)

    if (event != null) {
      event.putString("channel", "Android")
      memoryStorage.onSubmitCallback?.invoke(event)
    }
  }

  override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
    Log.d(TAG, "onDetailsCallRequested")

    if (memoryStorage.onAdditionalDetailsCallback == null) {
      super.onDetailsCallRequested(actionComponentData, actionComponentJson)
      return
    }

    val event = RNUtils.jsonToWritableMap(actionComponentJson)

    if (event != null) {
      memoryStorage.onAdditionalDetailsCallback?.invoke(event)
    }
  }

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
