package com.apq.plus.Base

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

object mContextWrapper {
    fun wrap(base: Context) : ContextWrapper {
        fun systemLanguage(): Locale {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                base.resources.configuration.locales.get(0)
            else
                base.resources.configuration.locale
        }

        val configuration = base.resources.configuration
        val metrics = base.resources.displayMetrics

        when (PreferenceManager.getDefaultSharedPreferences(base).getString("lang","")) {
            "en" -> configuration.setLocale(Locale.ENGLISH)
            "zh" -> configuration.setLocale(Locale.SIMPLIFIED_CHINESE)
            else -> configuration.setLocale(systemLanguage())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ContextWrapper(base.createConfigurationContext(configuration))
        }
        else
            base.resources.updateConfiguration(configuration, metrics)
        return ContextWrapper(base)
    }
}