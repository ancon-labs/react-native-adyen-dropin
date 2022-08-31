package com.reactnativeadyendropin.service

import android.util.Log
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.RecurringDropInServiceResult
import com.adyen.checkout.redirect.RedirectComponent
import com.facebook.react.bridge.ReadableMap
import com.reactnativeadyendropin.RNUtils
import com.reactnativeadyendropin.data.storage.MemoryStorage
import com.reactnativeadyendropin.repositories.RecurringRepository
import com.reactnativeadyendropin.repositories.paymentMethods.PaymentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
  private val recurringRepository: RecurringRepository by inject()
  private val memoryStorage: MemoryStorage by inject()

  init {
    instance = this
  }

  override fun onPaymentsCallRequested(paymentComponentState: PaymentComponentState<*>, paymentComponentJson: JSONObject) {
    Log.d(TAG, "onPaymentsCallRequested")

    if (!memoryStorage.disableNativeRequests) {
      super.onPaymentsCallRequested(paymentComponentState, paymentComponentJson)
    }

    if (memoryStorage.onSubmitCallback == null) {
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

    if (!memoryStorage.disableNativeRequests) {
      super.onDetailsCallRequested(actionComponentData, actionComponentJson)
    }

    if (memoryStorage.onAdditionalDetailsCallback == null) {
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
      RedirectComponent.getReturnUrl(applicationContext),
      memoryStorage.getAdditionalData(),
      memoryStorage.merchantAccount,
    )

    Log.d(TAG, "paymentComponentJson - \"${paymentComponentJson.toStringPretty()}\"")

    val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)

    val url = "${memoryStorage.baseUrl}${memoryStorage.makePaymentEndpoint}"

    val call = paymentsRepository.paymentsRequest(
      memoryStorage.headers,
      memoryStorage.queryParameters,
      url,
      requestBody
    )

    val res = handlePaymentResponse(call)
    return res
  }

  override fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
    Log.d(TAG, "makeDetailsCall - \"${actionComponentJson.toStringPretty()}\"")

    val requestBody = actionComponentJson.toString().toRequestBody(CONTENT_TYPE)

    val url = "${memoryStorage.baseUrl}${memoryStorage.makeDetailsCallEndpoint}"

    val call = paymentsRepository.detailsRequest(
      memoryStorage.headers,
      memoryStorage.queryParameters,
      url,
      requestBody
    )

    return handlePaymentResponse(call)
  }

  override fun removeStoredPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    storedPaymentJSON: JSONObject
  ) {
    launch(Dispatchers.IO) {
      Log.d(TAG, "removeStoredPaymentMethod")

      val url = "${memoryStorage.baseUrl}${memoryStorage.disableStoredPaymentMethodEndpoint}"

      val recurringId = storedPaymentMethod.id.orEmpty()
      val requestBody = createRemoveStoredPaymentMethodRequest(
        recurringId,
        memoryStorage.shopperReference,
        memoryStorage.merchantAccount
      ).toString().toRequestBody(CONTENT_TYPE)
      val call = recurringRepository.removeStoredPaymentMethod(
        memoryStorage.headers,
        memoryStorage.queryParameters,
        url,
        requestBody
      )

      val result = handleDisableResponse(call, recurringId)

      sendRecurringResult(result)
    }
  }

  @Suppress("NestedBlockDepth")
  private fun handlePaymentResponse(call: Call<ResponseBody>): DropInServiceResult {
    Log.d(TAG, "handlePaymentResponse")
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

  private fun handleDisableResponse(call: Call<ResponseBody>, id: String): RecurringDropInServiceResult {
    Log.d(AdyenDropInService.TAG, "handleDisableResponse")
    return try {
      val response = call.execute()

      val byteArray = response.errorBody()?.bytes()
      if (byteArray != null) {
        Log.e(TAG, "errorBody - ${String(byteArray)}")
      }

      if (response.isSuccessful) {
        val responseJSON = JSONObject(response.body()?.string() ?: "{}")

        Log.d(TAG, "Response - ${responseJSON.toStringPretty()}")

        when (val responseCode = responseJSON.getStringOrNull("response")) {
          "[detail-successfully-disabled]" -> RecurringDropInServiceResult.PaymentMethodRemoved(id)
          else -> RecurringDropInServiceResult.Error(reason = responseCode, dismissDropIn = false)
        }
      } else {
        Log.e(TAG, "FAILED - ${response.message()}")
        RecurringDropInServiceResult.Error(reason = response.message())
      }
    } catch (e: IOException) {
      Log.e(TAG, "IOException", e)
      RecurringDropInServiceResult.Error(reason = "IOException")
    }
  }
}
