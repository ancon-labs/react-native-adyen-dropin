package com.reactnativeadyendropin.di

import com.reactnativeadyendropin.repositories.paymentMethods.PaymentsRepository
import com.reactnativeadyendropin.repositories.paymentMethods.PaymentsRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
  factory<PaymentsRepository> { PaymentsRepositoryImpl(get()) }
}
