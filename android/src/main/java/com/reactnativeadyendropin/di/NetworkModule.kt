package com.reactnativeadyendropin.di

import android.os.Build
import com.adyen.checkout.core.api.SSLSocketUtil
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.reactnativeadyendropin.data.api.CheckoutApiService
import com.reactnativeadyendropin.data.api.RecurringApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.KeyStore
import java.util.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val networkModule = module {
  fun provideHttpClient(): OkHttpClient {
    val builder = OkHttpClient.Builder().let {
      val interceptor = HttpLoggingInterceptor()
      interceptor.level = HttpLoggingInterceptor.Level.BODY
      it.addNetworkInterceptor(interceptor)
    }

    if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {

      val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
      trustManagerFactory.init(null as KeyStore?)
      val trustManagers = trustManagerFactory.trustManagers
      check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
        "Unexpected default trust managers:" + Arrays.toString(trustManagers)
      }

      val trustManager = trustManagers[0] as X509TrustManager
      builder.sslSocketFactory(SSLSocketUtil.TLS_SOCKET_FACTORY, trustManager)
    }

    return builder.build()
  }

  fun provide(httpClient: OkHttpClient, cl: Class<*>?): Any {
    return Retrofit.Builder()
      .baseUrl("https://localhost/") // NOT USED BUT REQUIRED TO BE SET
      .client(httpClient)
      .addConverterFactory(
        MoshiConverterFactory.create(
          Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        )
      )
      .addCallAdapterFactory(CoroutineCallAdapterFactory())
      .build()
      .create(cl)
  }

  fun provideCheckoutApi(httpClient: OkHttpClient): CheckoutApiService {
    return provide(httpClient, CheckoutApiService::class.java) as CheckoutApiService
  }

  fun provideRecurringApi(httpClient: OkHttpClient): RecurringApiService {
    return provide(httpClient, RecurringApiService::class.java) as RecurringApiService
  }

  single<OkHttpClient>(named("httpClient")){ provideHttpClient() }
  single { provideCheckoutApi(get(named("httpClient"))) }
  single { provideRecurringApi(get(named("httpClient"))) }
}
