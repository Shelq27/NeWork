package ru.shelq.nework.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import ru.shelq.nework.BuildConfig
import ru.shelq.nework.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class NeWorkApplication : Application() {
    @Inject
    lateinit var auth: AppAuth
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAP_KEY)
        AppAuth.initApp(this)
        AppAuth.getInstance()
    }
}