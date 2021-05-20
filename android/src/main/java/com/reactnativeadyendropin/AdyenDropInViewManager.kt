package com.reactnativeadyendropin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.databinding.FragmentActionComponentBinding
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.ui.DropInActivity
import com.adyen.checkout.dropin.ui.DropInViewModel
import com.adyen.checkout.dropin.ui.base.DropInBottomSheetDialogFragment
import com.facebook.react.bridge.*
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import kotlinx.coroutines.isActive
import org.json.JSONObject


class AdyenDropInViewManager(private var reactContext : ReactApplicationContext) : SimpleViewManager<View>(), ActivityEventListener {
  companion object {
    private var instance: AdyenDropInViewManager? = null

    fun getInstance() : AdyenDropInViewManager? {
      return instance
    }
  }

  private val TAG = "AdyenDropInViewManager"

  var _paymentMethods: PaymentMethodsApiResponse? = null

  var _paymentMethodsConfiguration: DropInConfiguration? = null

  var _paymentResponse: JSONObject? = null

  var _detailsResponse: JSONObject? = null

  var _view: View? = null

  var _service: AdyenDropInService? = null

  // Events
  var onSuccessEmitter: (() -> Unit)? = null
  var onErrorEmitter: (() -> Unit)? = null
  var onCloseEmitter: (() -> Unit)? = null

  init {
    Logger.setLogcatLevel(Log.VERBOSE)
    AdyenDropInViewManager.instance = this
    reactContext.addActivityEventListener(this)
  }

  @ReactProp(name = "visible")
  fun setVisible(view: View, visible: Boolean?) {
    Log.d(TAG, "visible - ${visible}")
    if (visible == true) {
      this.open()
    } else if (visible == false) {
      this.close()
    }
  }

  @ReactProp(name = "paymentMethods")
  fun setPaymentMethods(view: View, paymentMethods: ReadableMap?) {
    if (paymentMethods == null) return

    try {
      val json = JSONObject(paymentMethods.toHashMap())
      val serializer = PaymentMethodsApiResponse.SERIALIZER
      this._paymentMethods = serializer.deserialize(json)
    } catch (err: Exception) {
      // TODO: onError
      Log.e(TAG, err.message ?: err.toString())
    }
  }

  @ReactProp(name = "paymentMethodsConfiguration")
  fun setPaymentMethodsConfiguration(view: View, paymentMethodsConfiguration: ReadableMap?) {
    if (paymentMethodsConfiguration == null) return

    try {
      val clientKey = paymentMethodsConfiguration.getString("clientKey") ?: return
      this._paymentMethodsConfiguration = ConfigurationParser(clientKey, this.reactContext).parse(paymentMethodsConfiguration)
    } catch (err: Exception) {
      // TODO: onError
      Log.e(TAG, err.message ?: err.toString())
    }
  }

  @ReactProp(name = "paymentResponse")
  fun setPaymentResponse(view: View, paymentResponse: ReadableMap?) {
    if (paymentResponse == null) return

    try {
      this._paymentResponse = JSONObject(paymentResponse.toHashMap())
      this.handleResponse(paymentResponse)
    } catch (err: Exception) {
      // TODO: onError
      Log.e(TAG, err.message ?: err.toString())
    }
  }

  @ReactProp(name = "detailsResponse")
  fun setDetailsResponse(view: View, detailsResponse: ReadableMap?) {
    if (detailsResponse == null) return

    try {
      this._detailsResponse = JSONObject(detailsResponse.toHashMap())
      this.handleResponse(detailsResponse)
    } catch (err: Exception) {
      // TODO: onError
      Log.e(TAG, err.message ?: err.toString())
    }
  }

  @ReactMethod
  fun onSubmit(event: WritableMap, service: AdyenDropInService) {
    this._service = service
    this.reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
      this._view!!.id,
      "onSubmit",
      event
    )
  }

  @ReactMethod
  fun onSuccess(event: WritableMap) {
    this.reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
      this._view!!.id,
      "onSuccess",
      event
    )
  }

  @ReactMethod
  fun onAdditionalDetails(event: WritableMap, service: AdyenDropInService) {
    this._service = service
    this.reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
      this._view!!.id,
      "onAdditionalDetails",
      event
    )
  }

  @ReactMethod
  fun onError(event: WritableMap) {
    this.reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
      this._view!!.id,
      "onError",
      event
    )
  }


  @ReactMethod
  fun onClose() {
    val event = Arguments.createMap()
    this.reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
      this._view!!.id,
      "onClose",
      event
    )
  }

  fun open() {
    Log.d(TAG, "Open was called")

    if (this.reactContext.currentActivity == null) {
      Log.d(TAG, "Skipping open - currentActivity was null")
      return
    }

    if (this._paymentMethods == null) {
      Log.d(TAG, "Skipping open - paymentMethods was null")
      return
    }


    if (this._paymentMethodsConfiguration == null) {
      Log.d(TAG, "Skipping open - paymentMethodsConfiguration was null")
      return
    }

    DropIn.startPayment(
      this.reactContext.currentActivity!!,
      this._paymentMethods!!,
      this._paymentMethodsConfiguration!!,
      null
    )
  }

  fun close() {
    Log.d(TAG, "Close was called")
    this._service?.sendDropInResult(DropInServiceResult.Finished("closed"))
  }

  fun handleResponse(response: ReadableMap) {
    val resultCode = response.getString("resultCode")
    Log.d(TAG, "Handle response - ${resultCode}")

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
          val actionJsonString = JSONObject(action.toHashMap()).toString()
          Log.d(TAG, actionJsonString)
          this.handleAction(actionJsonString)
        } else {
          this.handleFinishWithSuccess(resultCode)
        }
      }

      "Cancelled",
      "Error",
      "Refused" -> {
        this.handleFinishWithError(resultCode)
      }
    }
  }

  fun handleAction(action: String) {
    Log.d(TAG, "handleAction")
    this._service?.sendDropInResult(DropInServiceResult.Action(action))
  }

  fun handleFinishWithSuccess(resultCode: String) {
    Log.d(TAG, "handleFinishWithSuccess")
    this._service?.sendDropInResult(DropInServiceResult.Finished(resultCode))
    val event = Arguments.createMap()
    event.putString("resultCode", resultCode)
    this.onCloseEmitter = { this.onClose() }
    this.onSuccessEmitter = { this.onSuccess(event) }
  }

  fun handleFinishWithError(resultCode: String) {
    Log.d(TAG, "handleFinishWithError")
    this._service?.sendDropInResult(DropInServiceResult.Error())
    val event = Arguments.createMap()
    event.putString("resultCode", resultCode)
    this.onErrorEmitter = { this.onError(event) }
    this.close()
  }

  fun emitEvents() {
    this.onSuccessEmitter?.invoke()
    this.onSuccessEmitter = null

    this.onErrorEmitter?.invoke()
    this.onErrorEmitter = null

    this.onCloseEmitter?.invoke()
    this.onCloseEmitter = null
  }

  override fun getName() = "AdyenDropIn"

  override fun createViewInstance(reactContext: ThemedReactContext): View {
    this._view = View(reactContext)
    return this._view!!
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
    return MapBuilder.of(
      "onSubmit",
      MapBuilder.of("registrationName", "onSubmit"),
      "onSuccess",
      MapBuilder.of("registrationName", "onSuccess"),
      "onAdditionalDetails",
      MapBuilder.of("registrationName", "onAdditionalDetails"),
      "onError",
      MapBuilder.of("registrationName", "onError"),
      "onClose",
      MapBuilder.of("registrationName", "onClose")
    )
  }

  override fun onNewIntent(intent: Intent?) {
    Log.d(TAG, "onNewIntent - ${intent?.toString() ?: "unknown"}")
  }

  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode != DropIn.DROP_IN_REQUEST_CODE) return

    if (data != null) {
      Log.d(TAG, "INTENT RESULT - ${DropIn.getDropInResultFromIntent(data)}")
    }

    if (
      (resultCode == 0 && data != null && DropIn.getDropInResultFromIntent(data) == null) ||
      (data != null && DropIn.getDropInResultFromIntent(data) == "closed")
    ) {
      this.onClose()
    }

    Log.d(TAG, "onActivityResult - " +
      "data: ${activity?.toString() ?: "(null)"}" +
      "requestCode: ${requestCode}" +
      "resultCode: ${resultCode}" +
      "data: ${data?.toString() ?: "(null)"}")
  }
}
