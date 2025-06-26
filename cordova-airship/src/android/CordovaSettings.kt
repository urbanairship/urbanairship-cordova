/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.Color
import android.util.Log
import androidx.annotation.ColorInt
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.UALog

internal data class CordovaSettings(
    val cloudSite: String?,
    val productionAppKey: String?,
    val productionAppSecret: String?,
    val productionLogLevel: Int?,
    val developmentAppKey: String?,
    val developmentAppSecret: String?,
    val developmentLogLevel: Int?,
    val autoLaunchMessageCenter: Boolean?,
    val enablePushOnLaunch: Boolean?,
    val enableAnalytics: Boolean?,
    val notificationIcon: Int?,
    val notificationLargeIcon: Int?,
    val notificationAccentColor: Int?,
    val inProduction: Boolean?,
    val fcmFirebaseAppName: String?,
    val initialConfigUrl: String?,
    val defaultChannelId: String?,
    val disableNotificationsOnOptOut: OptOutFrequency?,
    val logPrivacyLevel: AirshipConfigOptions.PrivacyLevel?
) {

    enum class OptOutFrequency {
        ALWAYS, ONCE
    }

    companion object {
        private const val UA_PREFIX = "com.urbanairship"

        private const val PRODUCTION_KEY = "com.urbanairship.production_app_key"
        private const val PRODUCTION_SECRET = "com.urbanairship.production_app_secret"
        private const val DEVELOPMENT_KEY = "com.urbanairship.development_app_key"
        private const val DEVELOPMENT_SECRET = "com.urbanairship.development_app_secret"

        private const val NOTIFICATION_ICON = "com.urbanairship.notification_icon"
        private const val NOTIFICATION_LARGE_ICON = "com.urbanairship.notification_large_icon"
        private const val NOTIFICATION_ACCENT_COLOR = "com.urbanairship.notification_accent_color"
        private const val DEFAULT_NOTIFICATION_CHANNEL_ID = "com.urbanairship.default_notification_channel_id"

        private const val AUTO_LAUNCH_MESSAGE_CENTER = "com.urbanairship.auto_launch_message_center"

        private const val PRODUCTION_LOG_LEVEL = "com.urbanairship.production_log_level"
        private const val DEVELOPMENT_LOG_LEVEL = "com.urbanairship.development_log_level"
        private const val IN_PRODUCTION = "com.urbanairship.in_production"

        private const val ENABLE_PUSH_ONLAUNCH = "com.urbanairship.enable_push_onlaunch"

        private const val DISABLE_ANDROID_NOTIFICATIONS_ON_OPT_OUT = "com.urbanairship.android.disable_user_notifications_on_system_opt_out"

        private const val ENABLE_ANALYTICS = "com.urbanairship.enable_analytics"

        private const val CLOUD_SITE = "com.urbanairship.site"
        private const val FCM_FIREBASE_APP_NAME = "com.urbanairship.fcm_firebase_app_name"
        private const val INITIAL_CONFIG_URL = "com.urbanairship.initial_config_url"
        private const val LOG_PRIVACY_LEVEL = "com.urbanairship.log_privacy_level"

        fun fromConfig(context: Context): CordovaSettings {
            val config = parseConfigXml(context)
            return CordovaSettings(
                cloudSite = parseCloudSite(config[CLOUD_SITE]),
                productionAppKey = config[PRODUCTION_KEY],
                productionAppSecret = config[PRODUCTION_SECRET],
                productionLogLevel = parseLogLevel(config[PRODUCTION_LOG_LEVEL]),
                developmentAppKey = config[DEVELOPMENT_KEY],
                developmentAppSecret = config[DEVELOPMENT_SECRET],
                developmentLogLevel = parseLogLevel(config[DEVELOPMENT_LOG_LEVEL]),
                autoLaunchMessageCenter = config[AUTO_LAUNCH_MESSAGE_CENTER]?.toBoolean(),
                enablePushOnLaunch = config[ENABLE_PUSH_ONLAUNCH]?.toBoolean(),
                enableAnalytics = config[ENABLE_ANALYTICS]?.toBoolean(),
                notificationIcon = parseIcon(context, config[NOTIFICATION_ICON]),
                notificationLargeIcon = parseIcon(context, config[NOTIFICATION_LARGE_ICON]),
                notificationAccentColor = parseColor(config[NOTIFICATION_ACCENT_COLOR]),
                inProduction = config[IN_PRODUCTION]?.toBoolean(),
                fcmFirebaseAppName = config[FCM_FIREBASE_APP_NAME],
                initialConfigUrl = config[INITIAL_CONFIG_URL],
                defaultChannelId = config[DEFAULT_NOTIFICATION_CHANNEL_ID],
                disableNotificationsOnOptOut = parseFrequency(config[DISABLE_ANDROID_NOTIFICATIONS_ON_OPT_OUT]),
                logPrivacyLevel = parseLogPrivacyLevel(config[LOG_PRIVACY_LEVEL])
            )
        }

        private fun parseFrequency(value: String?): OptOutFrequency? {
            if (value.isNullOrEmpty()) { return null }
            try {
                return OptOutFrequency.valueOf(value.uppercase())
            } catch(e: IllegalArgumentException) {
                UALog.e("Invalid frequency $value", e)
                return null
            }
        }

        private fun parseLogPrivacyLevel(privacyLevel: String?): AirshipConfigOptions.PrivacyLevel? {
            if (privacyLevel.isNullOrEmpty()) { return null }
            return when (privacyLevel.lowercase()) {
                "public" -> AirshipConfigOptions.PrivacyLevel.PUBLIC
                "private" -> AirshipConfigOptions.PrivacyLevel.PRIVATE
                else -> {
                    UALog.e("Invalid log privacy level: $privacyLevel")
                    null
                }
            }
        }

        private fun parseLogLevel(logLevel: String?): Int? {
            if (logLevel.isNullOrEmpty()) { return null }
            return when (logLevel.lowercase()) {
                "verbose" -> Log.VERBOSE
                "debug" -> Log.DEBUG
                "info" ->  Log.INFO
                "warn" ->  Log.WARN
                "error" -> Log.ERROR
                "none" -> Log.ASSERT
                else -> {
                    UALog.e("Unexpected log level $logLevel")
                    null
                }
            }
        }

        @AirshipConfigOptions.Site
        private fun parseCloudSite(site: String?): String? {
            if (site.isNullOrEmpty()) { return null }
            return when (site.uppercase()) {
                AirshipConfigOptions.SITE_EU -> AirshipConfigOptions.SITE_EU
                AirshipConfigOptions.SITE_US -> AirshipConfigOptions.SITE_US
                else -> {
                    UALog.e("Unexpected site $site")
                    null
                }
            }
        }

        private fun parseIcon(context: Context, name: String?): Int? {
            if (name.isNullOrEmpty()) { return null }
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            return if (id != 0) {
                id
            } else {
                UALog.e("Unable to find drawable with name: $name")
                null
            }
        }

        @ColorInt
        private fun parseColor(color: String?): Int? {
            if (color.isNullOrEmpty()) { return null }
            return try {
                Color.parseColor(color)
            } catch (e: IllegalArgumentException) {
                UALog.e(e) { "Unable to parse color: $color" }
                null
            }
        }

        private fun parseConfigXml(context: Context): Map<String, String> {
            val config: MutableMap<String, String> = HashMap()
            val id = context.resources.getIdentifier("config", "xml", context.packageName)
            if (id == 0) {
                return config
            }
            val xml = context.resources.getXml(id)
            var eventType = -1
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    if (xml.name == "preference") {
                        val name = xml.getAttributeValue(null, "name").lowercase()
                        val value = xml.getAttributeValue(null, "value")
                        if (name.startsWith(UA_PREFIX) && value != null) {
                            config[name] = value.trim()
                        }
                    }
                }
                try {
                    eventType = xml.next()
                } catch (e: Exception) {
                    UALog.e(e) { "Error parsing config file" }
                }
            }
            return config
        }
    }
}
