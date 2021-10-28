package com.reactnativeadyendropin.data.storage

import com.adyen.checkout.components.model.payments.Amount
import com.reactnativeadyendropin.data.api.model.paymentsRequest.AdditionalData

class MemoryStorage {
  companion object {
    // Adyen DropIn related
    private const val DEFAULT_MERCHANT_ACCOUNT = ""
    private const val DEFAULT_COUNTRY = "SE"
    private const val DEFAULT_LOCALE = "sv_SE"
    private const val DEFAULT_VALUE = 100
    private const val DEFAULT_CURRENCY = "SEK"
    private const val DEFAULT_ALLOW_3DS2 = true
    private const val DEFAULT_EXECUTE_THREE_D = true
    private const val DEFAULT_SHOPPER_REFERENCE = "123456"

    // RN module related
    private const val DEFAULT_BASE_URL = "http://localhost:3000/api/"
    private const val DEFAULT_PAYMENT_ENDPOINT = "payments"
    private const val DEFAULT_DETAILS_ENDPOINT = "payments/details"
  }

  // Adyen DropIn related
  var merchantAccount: String = DEFAULT_MERCHANT_ACCOUNT
  var countryCode: String = DEFAULT_COUNTRY
  var shopperLocale: String = DEFAULT_LOCALE
  var amountValue: Int = DEFAULT_VALUE
  var amountCurrency: String = DEFAULT_CURRENCY
  var allow3DS2: Boolean = DEFAULT_ALLOW_3DS2
  var executeThreeD: Boolean = DEFAULT_EXECUTE_THREE_D
  var shopperReference: String = DEFAULT_SHOPPER_REFERENCE

  // RN module related
  var baseUrl: String = DEFAULT_BASE_URL
  var debug: Boolean = false
  var headers: Map<String, String> = mutableMapOf()
  var makePaymentEndpoint: String = DEFAULT_PAYMENT_ENDPOINT
  var makeDetailsCallEndpoint: String = DEFAULT_DETAILS_ENDPOINT

  fun getAmount(): Amount {
    val amount = Amount()

    amount.value = this.amountValue
    amount.currency = this.amountCurrency

    return amount
  }

  fun getAdditionalData(): AdditionalData {
    return AdditionalData(allow3DS2 = allow3DS2, executeThreeD = executeThreeD)
  }
}
