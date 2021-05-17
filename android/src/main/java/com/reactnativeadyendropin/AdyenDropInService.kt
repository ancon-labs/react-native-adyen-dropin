package com.reactnativeadyendropin

import android.content.Intent
import android.util.Log
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.facebook.react.bridge.ReactApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AdyenDropInService : DropInService() {
  companion object {
    private var instance: AdyenDropInService? = null

    fun getInstance() : AdyenDropInService? {
      return instance
    }
  }

  private val TAG = "AdyenDropInService"

  init {
      AdyenDropInService.instance = this
  }

  fun sendDropInResult(result: DropInServiceResult) {
    this.sendResult(result)
  }

  override fun onPaymentsCallRequested(paymentComponentState: PaymentComponentState<*>, paymentComponentJson: JSONObject) {
    Log.d(TAG, "onPaymentsCallRequested")
    val instance = AdyenDropInViewManager.getInstance()
    val event = RNUtils.jsonToWritableMap(paymentComponentJson)

    if (event != null && instance != null) {
      event.putString("channel", "Android")
      instance.onSubmit(event, this)
    }
  }

  override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
    Log.d(TAG, "onDetailsCallRequested")
    val instance = AdyenDropInViewManager.getInstance()
    val event = RNUtils.jsonToWritableMap(actionComponentJson)

    if (event != null && instance != null) {
      instance.onAdditionalDetails(event, this)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    val instance = AdyenDropInViewManager.getInstance()
    instance?.onClose()
    instance?.emitEvents()
  }
}
