/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova

import android.content.SharedPreferences
import androidx.core.content.edit
import com.urbanairship.android.framework.proxy.NotificationConfig
import com.urbanairship.android.framework.proxy.ProxyConfig
import com.urbanairship.android.framework.proxy.ProxyStore

internal class ProxyDataMigrator(private val preferences: SharedPreferences) {

    companion object {
        private const val PRODUCTION_KEY = "com.urbanairship.production_app_key"
        private const val PRODUCTION_SECRET = "com.urbanairship.production_app_secret"
        private const val DEVELOPMENT_KEY = "com.urbanairship.development_app_key"
        private const val DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret"

        private const val DEFAULT_NOTIFICATION_CHANNEL_ID = "com.urbanairship.default_notification_channel_id"
        private const val AUTO_LAUNCH_MESSAGE_CENTER = "com.urbanairship.auto_launch_message_center"

        private const val NOTIFICATION_ICON = "com.urbanairship.notification_icon"
        private const val NOTIFICATION_LARGE_ICON = "com.urbanairship.notification_large_icon"
        private const val NOTIFICATION_ACCENT_COLOR = "com.urbanairship.notification_accent_color"

        private const val PREFERENCE_CENTER_PREFIX = "preference_"
        private const val PREFERENCE_CENTER_SUFFIX = "_use_custom_ui"

        private const val FOREGROUND_NOTIFICATIONS = "com.urbanairship.foreground_notifications"

    }

    internal fun migrate(store: ProxyStore, settings: CordovaSettings) {
        migrateAppConfig(store, settings)
        migrateAutoLaunchMessageCenter(store)
        migrateShowForegroundNotifications(store)
        migrateNotificationConfig(store)
        migrateAutoLaunchPreferenceCenter(store)
    }

    private fun migrateAppConfig(store: ProxyStore, settings: CordovaSettings) {
        val productionAppKey = preferences.getString(PRODUCTION_KEY, null)
        val productionAppSecret = preferences.getString(PRODUCTION_SECRET, null)
        val developmentAppKey = preferences.getString(DEVELOPMENT_KEY, null)
        val developmentAppSecret = preferences.getString(DEVELOPMENT_SECRET, null)

        var production: ProxyConfig.Environment? = null
        if (productionAppKey != null && productionAppSecret != null) {
            production = ProxyConfig.Environment(
                appKey = productionAppKey,
                appSecret = productionAppSecret,
                logLevel = settings.productionLogLevel
            )
        }

        var development: ProxyConfig.Environment? = null
        if (developmentAppKey != null && developmentAppSecret != null) {
            development = ProxyConfig.Environment(
                appKey = productionAppKey,
                appSecret = productionAppSecret,
                logLevel = settings.productionLogLevel
            )
        }

        if (development != null || production != null) {
            store.airshipConfig = ProxyConfig(
                productionEnvironment = production,
                developmentEnvironment = development
            )

            preferences.edit {
                this.remove(DEVELOPMENT_KEY)
                this.remove(DEVELOPMENT_SECRET)
                this.remove(PRODUCTION_KEY)
                this.remove(PRODUCTION_SECRET)
            }
        }
    }

    private fun migrateAutoLaunchMessageCenter(store: ProxyStore) {
        if (!preferences.contains(AUTO_LAUNCH_MESSAGE_CENTER)) {
            return
        }

        store.isAutoLaunchMessageCenterEnabled = preferences.getBoolean(AUTO_LAUNCH_MESSAGE_CENTER, true)
        preferences.edit { this.remove(AUTO_LAUNCH_MESSAGE_CENTER) }
    }

    private fun migrateShowForegroundNotifications(store: ProxyStore) {
        if (!preferences.contains(FOREGROUND_NOTIFICATIONS)) {
            return
        }

        store.isForegroundNotificationsEnabled = preferences.getBoolean(FOREGROUND_NOTIFICATIONS, true)
        preferences.edit { this.remove(FOREGROUND_NOTIFICATIONS) }
    }

    private fun migrateNotificationConfig(store: ProxyStore) {
        val icon = preferences.getString(NOTIFICATION_ICON, null)
        val largeIcon = preferences.getString(NOTIFICATION_LARGE_ICON, null)
        val accentColor = preferences.getString(NOTIFICATION_ACCENT_COLOR, null)
        val defaultChannelId = preferences.getString(DEFAULT_NOTIFICATION_CHANNEL_ID, null)

        if (icon != null || largeIcon != null || accentColor != null || defaultChannelId != null) {
            store.notificationConfig = NotificationConfig(
                icon = icon,
                largeIcon = largeIcon,
                accentColor = accentColor,
                defaultChannelId = defaultChannelId
            )
        }

        preferences.edit {
            this.remove(NOTIFICATION_ICON)
            this.remove(NOTIFICATION_LARGE_ICON)
            this.remove(NOTIFICATION_ACCENT_COLOR)
            this.remove(DEFAULT_NOTIFICATION_CHANNEL_ID)
        }
    }

    private fun migrateAutoLaunchPreferenceCenter(store: ProxyStore) {
        val keysToRemove = mutableListOf<String>()
        // Preference Center
        preferences.all.filter {
            it.key.startsWith(PREFERENCE_CENTER_PREFIX)
        }.mapValues {
            if (it.value is Boolean) { it.value as Boolean } else { false }
        }.forEach {
            val preferenceCenterId = it.key
                .removePrefix(PREFERENCE_CENTER_PREFIX)
                .removeSuffix(PREFERENCE_CENTER_SUFFIX)

            store.setAutoLaunchPreferenceCenter(preferenceCenterId, !it.value)
            keysToRemove.add(it.key)
        }

        if (keysToRemove.isNotEmpty()) {
            preferences.edit {
                keysToRemove.forEach { this.remove(it) }
            }
        }
    }
}