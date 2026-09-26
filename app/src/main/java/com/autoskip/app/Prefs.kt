package com.autoskip.app

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

object Prefs {
    private const val FILE = "autoskip_prefs"
    private const val KEY_ENABLED = "autoskip_enabled"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    const val KEY_SKIPPED_COUNT = "skipped_count"

    fun get(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isAutoSkipEnabled(context: Context) = get(context).getBoolean(KEY_ENABLED, true)
    fun setAutoSkipEnabled(context: Context, enabled: Boolean) =
        get(context).edit().putBoolean(KEY_ENABLED, enabled).apply()

    fun isOnboardingDone(context: Context) = get(context).getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingDone(context: Context) =
        get(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()

    fun skippedCount(context: Context) = get(context).getInt(KEY_SKIPPED_COUNT, 0)
    fun incrementSkippedCount(context: Context) {
        val prefs = get(context)
        prefs.edit().putInt(KEY_SKIPPED_COUNT, prefs.getInt(KEY_SKIPPED_COUNT, 0) + 1).apply()
    }

    /** Whether the user has switched AutoSkip on in the system Accessibility settings. */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expected = ComponentName(context, AutoSkipService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
    }
}
