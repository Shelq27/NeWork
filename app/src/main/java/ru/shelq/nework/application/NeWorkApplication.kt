package ru.shelq.nework.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.shelq.nework.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class NeWorkApplication : Application() {
    @Inject
    lateinit var auth: AppAuth
    override fun onCreate() {
        super.onCreate()
        AppAuth.initApp(this)
        AppAuth.getInstance()
    }
}