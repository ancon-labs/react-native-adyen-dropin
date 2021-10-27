package com.reactnativeadyendropin

import android.app.Application
import com.reactnativeadyendropin.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun start(app: Application) {
  startKoin {
    androidContext(app.applicationContext)
    modules(appModule)
  }
}
