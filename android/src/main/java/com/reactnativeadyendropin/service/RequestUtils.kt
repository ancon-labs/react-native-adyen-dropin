package com.reactnativeadyendropin.service

import com.adyen.checkout.components.model.payments.Amount
import com.google.gson.Gson
import com.reactnativeadyendropin.data.api.model.paymentsRequest.AdditionalData
import org.json.JSONObject

@Suppress("LongParameterList")
fun createPaymentRequest(
  paymentComponentData: JSONObject,
  shopperReference: String,
  amount: Amount,
  countryCode: String,
  merchantAccount: String,
  redirectUrl: String,
  additionalData: AdditionalData,
  force3DS2Challenge: Boolean = false,
  threeDSAuthenticationOnly: Boolean = false
): JSONObject {

  val request = JSONObject(paymentComponentData.toString())

  request.put("shopperReference", shopperReference)
  request.put("amount", JSONObject(Gson().toJson(amount)))
  request.put("merchantAccount", merchantAccount)
  request.put("returnUrl", redirectUrl)
  request.put("countryCode", countryCode)
  request.put("channel", "android")
  request.put("additionalData", JSONObject(Gson().toJson(additionalData)))
  request.put("threeDSAuthenticationOnly", threeDSAuthenticationOnly)

  if (force3DS2Challenge) {
    val threeDS2RequestData = JSONObject()
    threeDS2RequestData.put("deviceChannel", "app")
    threeDS2RequestData.put("challengeIndicator", "requestChallenge")
    request.put("threeDS2RequestData", threeDS2RequestData)
  }

  return request
}
