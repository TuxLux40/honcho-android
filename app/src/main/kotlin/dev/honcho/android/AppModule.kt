package dev.honcho.android

import android.content.Context
import dev.honcho.android.data.HonchoRepository
import dev.honcho.android.data.SettingsRepository

object AppModule {
    lateinit var settingsRepository: SettingsRepository
        private set

    val honchoRepository: HonchoRepository by lazy {
        HonchoRepository(settingsRepository)
    }

    fun init(context: Context) {
        settingsRepository = SettingsRepository(context)
    }
}
