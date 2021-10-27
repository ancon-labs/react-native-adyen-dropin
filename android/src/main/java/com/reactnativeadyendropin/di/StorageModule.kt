package com.reactnativeadyendropin.di

import com.reactnativeadyendropin.data.storage.MemoryStorage
import org.koin.dsl.module

val storageManager = module {
  single { MemoryStorage() }
}
