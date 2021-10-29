package com.reactnativeadyendropin

import android.content.Context
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.dropin.DropInConfiguration
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.reactnativeadyendropin.service.AdyenDropInService
import org.json.JSONObject
import java.util.*

class ConfigurationParser(private val clientKey: String, context: ReactApplicationContext) {

  private val context: Context = context

  private fun getShopperLocale(config: ReadableMap): Locale {
    val providedShopperLocale = config.getString("shopperLocale")

    if (providedShopperLocale != null) {
      return LocaleUtil.fromLanguageTag(providedShopperLocale)
    }

    return Locale.getDefault()
  }

  private fun getEnvironment(config: ReadableMap): Environment {
    val environmentName = config.getString("environment")

    return when (environmentName) {
      "test" -> Environment.TEST
      "live" -> Environment.EUROPE
      else -> Environment.TEST
    }
  }

  private fun getAmount(config: ReadableMap): Amount {
    val map = config.getMap("amount")
    val value = map?.getInt("value") as? Int
    val currencyCode = map?.getString("currencyCode") as? String
    if (value != null && currencyCode != null) {
      val json = JSONObject()
        .put("value", value)
        .put("currency", currencyCode)
      val serializer = Amount.SERIALIZER

      return serializer.deserialize(json)
    }

    return Amount()
  }

  fun getShopperReference(config: ReadableMap): String {
    if (config.hasKey("shopperReference")) {
      return config.getString("shopperReference")!!
    }

    return "${this.context.packageName}_${System.currentTimeMillis()}"
  }

  fun parse(config: ReadableMap): DropInConfiguration {
    val shopperLocale = this.getShopperLocale(config)
    val environment = this.getEnvironment(config)
    val amount = this.getAmount(config)
    val shopperReference = this.getShopperReference(config)

    val cardConfiguration = CardConfiguration.Builder(shopperLocale, environment, this.clientKey)
      .setShopperReference(shopperReference)
      .setSupportedCardTypes(CardType.MASTERCARD, CardType.VISA)
      .build()

    val builder = DropInConfiguration.Builder(
      this.context,
      AdyenDropInService::class.java,
      this.clientKey
    )
      .setShopperLocale(shopperLocale)
      .setEnvironment(environment)
      .setAmount(amount)
      .addCardConfiguration(cardConfiguration)

    return builder.build()
  }
}
