/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova

import android.content.Context
import android.os.Build
import com.urbanairship.Autopilot
import com.urbanairship.UALog
import com.urbanairship.actions.ActionResult
import com.urbanairship.android.framework.proxy.EventType
import com.urbanairship.android.framework.proxy.events.EventEmitter
import com.urbanairship.android.framework.proxy.proxies.AirshipProxy
import com.urbanairship.android.framework.proxy.proxies.FeatureFlagProxy
import com.urbanairship.json.JsonList
import com.urbanairship.json.JsonMap
import com.urbanairship.json.JsonSerializable
import com.urbanairship.json.JsonValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONObject

class AirshipCordova : CordovaPlugin() {

    internal data class Listener(
        val listenerId: Int,
        val callbackContext: CallbackContext
    )

    private var listeners: MutableMap<String, MutableList<Listener>> = mutableMapOf()

    companion object {
        private val EVENT_NAME_MAP = mapOf(
            EventType.BACKGROUND_NOTIFICATION_RESPONSE_RECEIVED to "airship.event.notification_response",
            EventType.FOREGROUND_NOTIFICATION_RESPONSE_RECEIVED to "airship.event.notification_response",
            EventType.CHANNEL_CREATED to "airship.event.channel_created",
            EventType.DEEP_LINK_RECEIVED to "airship.event.deep_link_received",
            EventType.DISPLAY_MESSAGE_CENTER to "airship.event.display_message_center",
            EventType.DISPLAY_PREFERENCE_CENTER to "airship.event.display_preference_center",
            EventType.MESSAGE_CENTER_UPDATED to "airship.event.message_center_updated",
            EventType.PUSH_TOKEN_RECEIVED to "airship.event.push_token_received",
            EventType.FOREGROUND_PUSH_RECEIVED to "airship.event.push_received",
            EventType.BACKGROUND_PUSH_RECEIVED to "airship.event.push_received",
            EventType.NOTIFICATION_STATUS_CHANGED to "airship.event.notification_status_changed"
        )
    }

    private lateinit var applicationContext: Context
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main) + SupervisorJob()

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        try {
            when (action) {
                "perform" -> perform(args, callbackContext)
                "addListener" -> addListener(args, callbackContext)
                "removeListener" -> removeListener(args)
                else -> return false
            }
            return true
        } catch (exception: java.lang.Exception) {
            callbackContext.error(action, exception)
        }
        return false
    }

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        UALog.i { "Initializing Urban Airship cordova plugin." }
        applicationContext = cordova.getActivity().applicationContext
        Autopilot.automaticTakeOff(applicationContext)

        scope.launch {
            EventEmitter.shared().pendingEventListener.collect {
                notifyPendingEvents()
            }
        }
    }

    override fun onReset() {
        super.onReset()
        this.listeners.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.listeners.clear()
    }

    private fun addListener(args: JSONArray, callbackContext: CallbackContext) {
        val jsonArgs = JsonValue.wrap(args).requireList()

        val eventName = jsonArgs.get(0).requireString()

        val listener = Listener(
            listenerId = jsonArgs.get(1).requireInt(),
            callbackContext = callbackContext
        )

        this.listeners.getOrPut(eventName) { mutableListOf() }.add(listener)
        notifyPendingEvents()
    }

    private fun removeListener(args: JSONArray) {
        val jsonArgs = JsonValue.wrap(args).requireList()

        val eventName = jsonArgs.get(0).requireString()

        val listenerId = jsonArgs.get(1).requireInt()
        this.listeners[eventName]?.removeAll {
            it.listenerId == listenerId
        }
    }

    private fun notifyPendingEvents() {
        EventType.entries.forEach { eventType ->
            val listeners = this.listeners[EVENT_NAME_MAP[eventType]]
            if (listeners?.isNotEmpty() == true) {
                EventEmitter.shared().processPending(listOf(eventType)) { event ->
                    listeners.forEach { listeners ->
                        val pluginResult = event.body.pluginResult()
                        pluginResult.keepCallback = true
                        listeners.callbackContext.sendPluginResult(pluginResult)
                    }
                    true
                }
            }
        }
    }

    private fun perform(args: JSONArray, callback: CallbackContext) {
        val jsonArgs = JsonValue.wrap(args).requireList()
        val method = jsonArgs.get(0).requireString()
        val arg: JsonValue = if (jsonArgs.size() == 2) { jsonArgs.get(1) } else { JsonValue.NULL }

        val proxy = AirshipProxy.shared(applicationContext)

        scope.launch {
            when (method) {
                // Airship
                "takeOff" -> callback.resolve(scope, method) { proxy.takeOff(arg) }
                "isFlying" -> callback.resolve(scope, method) { proxy.isFlying() }

                // Channel
                "channel#getChannelId" -> callback.resolve(scope, method) { proxy.channel.getChannelId() }

                "channel#editTags" -> callback.resolve(scope, method) { proxy.channel.editTags(arg) }
                "channel#getTags" -> callback.resolve(scope, method) { proxy.channel.getTags().toList() }
                "channel#editTagGroups" -> callback.resolve(scope, method) { proxy.channel.editTagGroups(arg) }
                "channel#editSubscriptionLists" -> callback.resolve(scope, method) { proxy.channel.editSubscriptionLists(arg) }
                "channel#editAttributes" -> callback.resolve(scope, method) { proxy.channel.editAttributes(arg) }
                "channel#getSubscriptionLists" -> callback.resolve(scope, method) { proxy.channel.getSubscriptionLists() }
                "channel#enableChannelCreation" -> callback.resolve(scope, method) { proxy.channel.enableChannelCreation() }

                // Contact
                "contact#reset" -> callback.resolve(scope, method) { proxy.contact.reset() }
                "contact#notifyRemoteLogin" -> callback.resolve(scope, method) { proxy.contact.notifyRemoteLogin() }
                "contact#identify" -> callback.resolve(scope, method) { proxy.contact.identify(arg.requireString()) }
                "contact#getNamedUserId" -> callback.resolve(scope, method) { proxy.contact.getNamedUserId() }
                "contact#editTagGroups" -> callback.resolve(scope, method) { proxy.contact.editTagGroups(arg) }
                "contact#editSubscriptionLists" -> callback.resolve(scope, method) { proxy.contact.editSubscriptionLists(arg) }
                "contact#editAttributes" -> callback.resolve(scope, method) { proxy.contact.editAttributes(arg) }
                "contact#getSubscriptionLists" -> callback.resolve(scope, method) { proxy.contact.getSubscriptionLists() }

                // Push
                "push#setUserNotificationsEnabled" -> callback.resolve(scope, method) { proxy.push.setUserNotificationsEnabled(arg.requireBoolean()) }
                "push#enableUserNotifications" -> callback.resolve(scope, method) { proxy.push.enableUserPushNotifications() }
                "push#isUserNotificationsEnabled" -> callback.resolve(scope, method) { proxy.push.isUserNotificationsEnabled() }
                "push#getNotificationStatus" -> callback.resolve(scope, method) { proxy.push.getNotificationStatus() }
                "push#getActiveNotifications" -> callback.resolve(scope, method) { proxy.push.getActiveNotifications() }
                "push#clearNotification" -> callback.resolve(scope, method) { proxy.push.clearNotification(arg.requireString()) }
                "push#clearNotifications" -> callback.resolve(scope, method) { proxy.push.clearNotifications() }
                "push#getPushToken" -> callback.resolve(scope, method) { proxy.push.getRegistrationToken() }
                "push#android#isNotificationChannelEnabled" -> callback.resolve(scope, method) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        proxy.push.isNotificationChannelEnabled(arg.requireString())
                    } else {
                        true
                    }
                }
                "push#android#setNotificationConfig" -> callback.resolve(scope, method) { proxy.push.setNotificationConfig(arg) }
                "push#android#setForegroundNotificationsEnabled" -> callback.resolve(scope, method) {
                    proxy.push.isForegroundNotificationsEnabled = arg.requireBoolean()
                    return@resolve Unit
                }
                "push#android#isForegroundNotificationsEnabled" -> callback.resolve(scope, method) {
                    proxy.push.isForegroundNotificationsEnabled
                }

                // In-App
                "inApp#setPaused" -> callback.resolve(scope, method) { proxy.inApp.setPaused(arg.getBoolean(false)) }
                "inApp#isPaused" -> callback.resolve(scope, method) { proxy.inApp.isPaused() }
                "inApp#setDisplayInterval" -> callback.resolve(scope, method) { proxy.inApp.setDisplayInterval(arg.getLong(0)) }
                "inApp#getDisplayInterval" -> callback.resolve(scope, method) { proxy.inApp.getDisplayInterval() }

                // Analytics
                "analytics#trackScreen" -> callback.resolve(scope, method) { proxy.analytics.trackScreen(arg.string) }
                "analytics#addCustomEvent" -> callback.resolve(scope, method) { proxy.analytics.addEvent(arg) }
                "analytics#associateIdentifier" -> {
                    val associatedIdentifierArgs = arg.requireStringList()
                    proxy.analytics.associateIdentifier(
                        associatedIdentifierArgs[0],
                        associatedIdentifierArgs.getOrNull(1)
                    )
                }

                // Message Center
                "messageCenter#getMessages" -> callback.resolve(scope, method) {
                    JsonValue.wrapOpt(proxy.messageCenter.getMessages())
                }
                "messageCenter#dismiss" -> callback.resolve(scope, method) { proxy.messageCenter.dismiss() }
                "messageCenter#display" -> callback.resolve(scope, method) { proxy.messageCenter.display(arg.string) }
                "messageCenter#showMessageView" -> callback.resolve(scope, method) { proxy.messageCenter.showMessageView(arg.requireString()) }
                "messageCenter#markMessageRead" -> callback.resolve(scope, method) { proxy.messageCenter.markMessageRead(arg.requireString()) }
                "messageCenter#deleteMessage" -> callback.resolve(scope, method) { proxy.messageCenter.deleteMessage(arg.requireString()) }
                "messageCenter#getUnreadMessageCount" -> callback.resolve(scope, method) { proxy.messageCenter.getUnreadMessagesCount() }
                "messageCenter#setAutoLaunchDefaultMessageCenter" -> callback.resolve(scope, method) { proxy.messageCenter.setAutoLaunchDefaultMessageCenter(arg.requireBoolean()) }
                "messageCenter#refreshMessages" -> callback.resolve(scope, method) {
                    if (!proxy.messageCenter.refreshInbox()) {
                        throw Exception("Failed to refresh")
                    }
                    return@resolve Unit
                }

                // Preference Center
                "preferenceCenter#display" -> callback.resolve(scope, method) { proxy.preferenceCenter.displayPreferenceCenter(arg.requireString()) }
                "preferenceCenter#getConfig" -> callback.resolve(scope, method) {
                    proxy.preferenceCenter.getPreferenceCenterConfig(
                        arg.requireString()
                    )
                }
                "preferenceCenter#setAutoLaunchPreferenceCenter" -> callback.resolve(scope, method) {
                    val autoLaunchArgs = arg.requireList()
                    proxy.preferenceCenter.setAutoLaunchPreferenceCenter(
                        autoLaunchArgs.get(0).requireString(),
                        autoLaunchArgs.get(1).getBoolean(false)
                    )
                }

                // Privacy Manager
                "privacyManager#setEnabledFeatures" -> callback.resolve(scope, method) { proxy.privacyManager.setEnabledFeatures(arg.requireStringList()) }
                "privacyManager#getEnabledFeatures" -> callback.resolve(scope, method) { proxy.privacyManager.getFeatureNames() }
                "privacyManager#enableFeatures" -> callback.resolve(scope, method) { proxy.privacyManager.enableFeatures(arg.requireStringList()) }
                "privacyManager#disableFeatures" -> callback.resolve(scope, method) { proxy.privacyManager.disableFeatures(arg.requireStringList()) }
                "privacyManager#isFeaturesEnabled" -> callback.resolve(scope, method) { proxy.privacyManager.isFeatureEnabled(arg.requireStringList()) }

                // Locale
                "locale#setLocaleOverride" -> callback.resolve(scope, method) { proxy.locale.setCurrentLocale(arg.requireString()) }
                "locale#getCurrentLocale" -> callback.resolve(scope, method) { proxy.locale.getCurrentLocale() }
                "locale#clearLocaleOverride" -> callback.resolve(scope, method) { proxy.locale.clearLocale() }

                // Actions
                "actions#run" -> callback.resolve(scope, method) {
                    val actionArgs = arg.requireList()
                    val name = actionArgs.get(0).requireString()
                    val value: JsonValue? = if (actionArgs.size() == 2) {
                        actionArgs.get(1)
                    } else {
                        null
                    }

                    val result = proxy.actions.runAction(name, value)
                    if (result.status == ActionResult.STATUS_COMPLETED) {
                        result.value
                    } else {
                        throw Exception("Action failed ${result.status}")
                    }
                }


                // Feature Flag
                "featureFlagManager#flag" -> callback.resolve(scope, method) {
                    proxy.featureFlagManager.flag(arg.requireString())
                }

                "featureFlagManager#trackInteraction" -> {
                    callback.resolve(scope, method) {
                        val featureFlagProxy = FeatureFlagProxy(arg)
                        proxy.featureFlagManager.trackInteraction(flag = featureFlagProxy)
                    }
                }

                else -> callback.error("Not implemented")
            }
        }

    }
}



internal fun CallbackContext.error(method: String, exception: java.lang.Exception) {
    this.error("AIRSHIP_ERROR(method=$method, exception=$exception)")
}

internal fun CallbackContext.resolve(scope: CoroutineScope, method: String, function: suspend () -> Any?) {
    scope.launch {
        try {
            when (val result = function()) {
                is Unit -> {
                    this@resolve.success()
                }

                is Int -> {
                    this@resolve.success(result)
                }

                is String -> {
                    this@resolve.success(result)
                }

                is Boolean -> {
                    sendPluginResult(
                        PluginResult(
                            PluginResult.Status.OK,
                            result
                        )
                    )
                }
                else -> {
                    sendPluginResult(
                        JsonValue.wrap(result).pluginResult()
                    )
                }
            }
        } catch (e: Exception) {
            this@resolve.error(method, e)
        }
    }
}

internal fun JsonValue.requireBoolean(): Boolean {
    require(this.isBoolean)
    return this.getBoolean(false)
}

internal fun JsonValue.requireStringList(): List<String> {
    return this.requireList().list.map { it.requireString() }
}

internal fun JsonValue.requireInt(): Int {
    require(this.isInteger)
    return this.getInt(0)
}

internal fun JsonSerializable.pluginResult(): PluginResult {
    val json = this.toJsonValue()

    return when {
        json.isNull -> PluginResult(PluginResult.Status.OK,  null as String?)
        json.isString -> PluginResult(PluginResult.Status.OK, json.requireString())
        json.isBoolean -> PluginResult(PluginResult.Status.OK, json.requireBoolean())
        json.isInteger -> PluginResult(PluginResult.Status.OK, json.getInt(0))
        json.isNumber -> PluginResult(PluginResult.Status.OK, json.getFloat(0F))
        json.isJsonList -> {
            PluginResult(PluginResult.Status.OK, json.requireList().toJSONArray())
        }
        json.isJsonMap -> {
            PluginResult(PluginResult.Status.OK, json.requireMap().toJSONObject())
        }

        else -> PluginResult(PluginResult.Status.OK, json.toString())
    }
}

internal fun JsonList.toJSONArray(): JSONArray {
    val array = JSONArray()
    this.forEach {
        array.put(it.toCordovaJSON())
    }
    return array
}

internal fun JsonMap.toJSONObject(): JSONObject {
    return JSONObject(map.mapValues { it.value.toCordovaJSON() })
}

internal fun JsonSerializable.toCordovaJSON(): Any? {
    val json = this.toJsonValue()
    return when {
        json.isNull -> null
        json.isJsonList -> json.requireList().toJSONArray()
        json.isJsonMap -> json.requireMap().toJSONObject()
        else -> json.value
    }
}
