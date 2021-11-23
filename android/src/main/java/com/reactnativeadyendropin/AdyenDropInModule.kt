package com.reactnativeadyendropin

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.util.Log.VERBOSE
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.facebook.react.bridge.*
import com.reactnativeadyendropin.data.storage.MemoryStorage
import com.reactnativeadyendropin.service.AdyenDropInService
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AdyenDropInModule(private val reactContext : ReactApplicationContext): ReactContextBaseJavaModule(reactContext), KoinComponent, ActivityEventListener  {
  private val TAG = "AdyenDropInModule"

  private val memoryStorage: MemoryStorage = get()

  private var dropInConfiguration: DropInConfiguration? = null

  private var resolveCallback: Callback? = null

  private var rejectCallback: Callback? = null

  init {
    reactContext.addActivityEventListener(this)
  }

  override fun getName(): String {
    return "AdyenDropInModule"
  }

  @ReactMethod
  fun setDropInConfig(config: ReadableMap?) {
    Log.d(TAG, "Called setDropInConfig")

    if (config == null) {
      rejectCallback?.invoke("setDropInConfig was called without a config object")
    } else {
      try {
        val clientKey = config.getString("clientKey")
        val parser = ConfigurationParser(clientKey!!, this.reactContext)
        this.dropInConfiguration = parser.parse(config)

        if (config.hasKey("shopperReference")) {
          memoryStorage.shopperReference = config.getString("shopperReference")!!
        }

        if (config.hasKey("amount")) {
          val amount = config.getMap("amount")!!
          memoryStorage.amountValue = amount.getInt("value")
          memoryStorage.amountCurrency = amount.getString("currencyCode")!!
        }

        if (config.hasKey("countryCode")) {
          memoryStorage.countryCode = config.getString("countryCode")!!
        }

        if (config.hasKey("shopperLocale")) {
          memoryStorage.shopperLocale = config.getString("shopperLocale")!!
        }

        if (config.hasKey("additionalData")) {
          val additionalData = config.getMap("additionalData")!!
          memoryStorage.allow3DS2 = additionalData.getBoolean("allow3DS2")
          memoryStorage.executeThreeD = additionalData.getBoolean("executeThreeD")
        }
      } catch (err: Error) {
        Log.d(TAG, "Error in setDropInConfiguration - ${err.message}")
        Log.e(TAG, err.toString())
        rejectCallback?.invoke(err.message)
      }
    }
  }

  @ReactMethod
  fun setModuleConfig(config: ReadableMap?) {
    Log.d(TAG, "Called setModuleConfig")

    if (config == null) {
      rejectCallback?.invoke("setModuleConfig was called without a config object")
    } else {
      try {
        if (config.hasKey("baseUrl")) {
          memoryStorage.baseUrl = config.getString("baseUrl")!!
        }

        if (config.hasKey("debug")) {
          val debug = config.getBoolean("debug")
          if (debug is Boolean) {
            memoryStorage.debug = debug
            Logger.setLogcatLevel(VERBOSE)
          }
        }

        if (config.hasKey("disableNativeRequests")) {
          memoryStorage.disableNativeRequests = config.getBoolean("disableNativeRequests")
        }

        if (config.hasKey("headers")) {
          val map = config.getMap("headers")!!
          memoryStorage.headers = RNUtils.readableMapToStringMap(map)
        }

        if (config.hasKey("queryParameters")) {
          val map = config.getMap("queryParameters")!!
          memoryStorage.queryParameters = RNUtils.readableMapToStringMap(map)
        }

        if (config.hasKey("endpoints")) {
          val endpoints = config.getMap("endpoints")!!
          memoryStorage.makePaymentEndpoint = endpoints.getString("makePayment")!!
          memoryStorage.makeDetailsCallEndpoint = endpoints.getString("makeDetailsCall")!!
        }
      } catch (err: Error) {
        Log.d(TAG, "Error in setModuleConfig - ${err.message}")
        Log.e(TAG, err.toString())
        rejectCallback?.invoke("Failed to set module config. Check the config object values and that its types are correct. ${err.message}")
      }
    }
  }

  @ReactMethod
  fun setSubmitCallback(callback: Callback?) {
    Log.d(TAG, "Called setSubmitCallback")
    memoryStorage.onSubmitCallback = callback
  }

  @ReactMethod
  fun setAdditionalDetailsCallback(callback: Callback?) {
    Log.d(TAG, "Called setAdditionalDetailsCallback")
    memoryStorage.onAdditionalDetailsCallback = callback
  }

  @ReactMethod
  fun setPaymentResponse(paymentResponse: ReadableMap?) {
    Log.d(TAG, "Called setPaymentResponse")
    if (paymentResponse == null) return

    try {
      AdyenDropInService.handleAsyncResponse(paymentResponse)
    } catch (err: Exception) {
      this.handleError(err.message  ?: "Unknown error in setPaymentResponse")
    }
  }

  @ReactMethod
  fun setDetailsResponse(detailsResponse: ReadableMap?) {
    Log.d(TAG, "Called setDetailsResponse")
    if (detailsResponse == null) return

    try {
      AdyenDropInService.handleAsyncResponse(detailsResponse)
    } catch (err: Exception) {
      this.handleError(err.message  ?: "Unknown error in setDetailsResponse")
    }
  }

  @ReactMethod
  fun start(paymentMethodsResponse: ReadableMap, resolveCallback: Callback, rejectCallback: Callback) {
    Log.d(TAG, "Called start")

    if (this.dropInConfiguration == null) {
      rejectCallback.invoke("start was called without dropInConfig being set")
      return
    }

    try {
      this.resolveCallback = resolveCallback
      this.rejectCallback = rejectCallback

      val jsonObject = RNUtils.convertMapToJson(paymentMethodsResponse)
      val paymentMethodsApiResponse = PaymentMethodsApiResponse.SERIALIZER.deserialize(jsonObject)
      DropIn.startPayment(this.reactContext.currentActivity!!, paymentMethodsApiResponse, this.dropInConfiguration!!)
    } catch (err: Error) {
      rejectCallback.invoke("An error occurred while attempting to start payment: ${err.message}")
    }
  }

  fun handleError(reason: String?) {
    Log.d(TAG, "handleError - ${reason}")
    this.rejectCallback?.invoke(reason)
  }

  fun handleCancelled() {
    Log.d(TAG, "handleCancelled")
    this.rejectCallback?.invoke("Cancelled")
  }

  fun handleFinished(result: String) {
    Log.d(TAG, "handleFinished - ${result}")
    this.resolveCallback?.invoke(result)
  }

  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    Log.d(TAG, "onActivityResult - ${activity}, ${requestCode}, ${resultCode}, ${data}")

    val dropInResult = DropIn.handleActivityResult(requestCode, resultCode, data) ?: return
    when (dropInResult) {
      is DropInResult.Error -> handleError(dropInResult.reason)
      is DropInResult.CancelledByUser -> handleCancelled()
      is DropInResult.Finished -> {
        Log.d(TAG, "dropInResult - ${dropInResult.result}")

        try {
          val json = JSONObject(dropInResult.result)
          if (json.getStringOrNull("resultCode") == null) {
            handleError(dropInResult.result ?: json.getStringOrNull("message") ?: json.getStringOrNull("refusalReason") ?: "Unknown error")
          } else {
             handleFinished(dropInResult.result)
          }
        } catch (err: Error) {
          Log.e(TAG, err.message ?: "Unknown parsing error")
          handleFinished(dropInResult.result)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    Log.d(TAG, "onNewIntent - ${intent}")
  }
}
