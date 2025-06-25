/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.UAirship
import com.urbanairship.analytics.Extension
import com.urbanairship.android.framework.proxy.BaseAutopilot
import com.urbanairship.android.framework.proxy.ProxyStore

@Keep
class CordovaAutopilot : BaseAutopilot() {

    companion object {
        private const val PREFERENCE_FILE = "com.urbanairship.ua_plugin_shared_preferences"
        private const val PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG = "com.urbanairship.PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG"
    }

    private var _settings: CordovaSettings? = null
    private fun settings(context: Context): CordovaSettings {
        return _settings ?: CordovaSettings.fromConfig(context).also { _settings = it }
    }

    private var _preferences: SharedPreferences? = null
    private fun preferences(context: Context): SharedPreferences {
        return _preferences ?: context.getSharedPreferences(
            PREFERENCE_FILE,
            Context.MODE_PRIVATE
        ).also { _preferences = it }
    }

    override fun onReady(context: Context, airship: UAirship) {
        Log.i("CordovaAutopilot", "onAirshipReady")

        airship.analytics.registerSDKExtension(Extension.CORDOVA, AirshipCordovaVersion.version);
        val settings = settings(context)
        val preferences = preferences(context)

        settings.disableNotificationsOnOptOut?.let {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                when (it) {
                    CordovaSettings.OptOutFrequency.ALWAYS -> {
                        airship.pushManager.userNotificationsEnabled = false
                    }

                    CordovaSettings.OptOutFrequency.ONCE -> {
                        if (!preferences.getBoolean(PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG, false)) {
                            airship.pushManager.userNotificationsEnabled = false
                        }
                    }
                }
            }
            preferences.edit { putBoolean(PROCESSED_NOTIFICATIONS_OPTED_OUT_FLAG, true) }
        }

        if (settings.enablePushOnLaunch == true) {
            airship.pushManager.userNotificationsEnabled = true
        }
    }

    override fun onMigrateData(context: Context, proxyStore: ProxyStore) {
        val settings = settings(context)
        ProxyDataMigrator(preferences(context)).migrate(proxyStore, settings)
        proxyStore.defaultAutoLaunchMessageCenter = settings.autoLaunchMessageCenter ?: true
    }

    override fun createConfigBuilder(context: Context): AirshipConfigOptions.Builder {
        return super.createConfigBuilder(context).also { builder ->
            val settings = settings(context)

            if (settings.developmentAppKey != null && settings.developmentAppSecret != null) {
                builder.setDevelopmentAppKey(settings.developmentAppKey)
                builder.setDevelopmentAppSecret(settings.developmentAppSecret)
            }

            if (settings.productionAppKey != null && settings.productionAppSecret != null) {
                builder.setProductionAppKey(settings.productionAppKey)
                builder.setProductionAppSecret(settings.productionAppSecret)
            }

            settings.developmentLogLevel?.apply { builder.setDevelopmentLogLevel(this) }
            settings.productionLogLevel?.apply { builder.setProductionLogLevel(this) }
            settings.logPrivacyLevel?.apply {
                builder.setDevelopmentLogPrivacyLevel(this)
                builder.setProductionLogPrivacyLevel(this)
            }
            settings.inProduction?.apply { builder.setInProduction(this) }
            settings.enableAnalytics?.apply { builder.setAnalyticsEnabled(this) }
            settings.cloudSite?.apply { builder.setSite(this) }
            settings.defaultChannelId?.apply { builder.setNotificationChannel(this) }
            settings.fcmFirebaseAppName?.apply { builder.setFcmFirebaseAppName(this) }
            settings.initialConfigUrl?.apply { builder.setInitialConfigUrl(this) }

            settings.notificationIcon?.apply { builder.setNotificationIcon(this) }
            settings.notificationLargeIcon?.apply { builder.setNotificationLargeIcon(this) }
            settings.notificationAccentColor?.apply { builder.setNotificationAccentColor(this) }
        }
    }
}