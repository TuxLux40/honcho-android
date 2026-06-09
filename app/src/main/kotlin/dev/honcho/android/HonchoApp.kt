package dev.honcho.android

import android.app.Application

class HonchoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(applicationContext)
    }
}
