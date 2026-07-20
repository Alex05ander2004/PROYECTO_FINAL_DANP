package com.example.refood

import android.app.Application
import com.example.refood.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ReFoodApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, applicationScope)
    }
}
