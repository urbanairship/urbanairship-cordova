/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import com.urbanairship.Autopilot
import com.urbanairship.PendingResult
import com.urbanairship.UALog
import com.urbanairship.actions.ActionResult
import com.urbanairship.android.framework.proxy.EventType
import com.urbanairship.android.framework.proxy.Utils
import com.urbanairship.android.framework.proxy.events.EventEmitter
import com.urbanairship.android.framework.proxy.proxies.AirshipProxy
import com.urbanairship.android.framework.proxy.proxies.FeatureFlagProxy
import com.urbanairship.json.JsonList
import com.urbanairship.json.JsonMap
import com.urbanairship.json.JsonSerializable
import com.urbanairship.json.JsonValue
import com.urbanairship.json.jsonMapOf
import com.urbanairship.push.NotificationInfo
import com.urbanairship.push.PushManager
import com.urbanairship.push.PushMessage
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

    private var listeners: MutableMap<EventType, MutableList<Listener>> = mutableMapOf()
    private val notificationStack: MutableMap<EventType, MutableList<NotificationInfo>> = mutableMapOf()

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
            EventType.BACKGROUND_PUSH_RECEIVED to "airship.event.background_push_received",
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

        cordova.getThreadPool().execute {
            try {
                notifyListeners(
                    EventType.BACKGROUND_NOTIFICATION_RESPONSE_RECEIVED,
                    cordova.getActivity().intent
                )
            } catch (e: java.lang.Exception) {
                val msg = e.toString()
                UALog.e { msg }
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        try {
            super.onNewIntent(intent)
            notifyListeners(
                EventType.BACKGROUND_NOTIFICATION_RESPONSE_RECEIVED,
                intent!!
            )
        } catch (e: java.lang.Exception) {
            val msg = e.toString()
            UALog.e { msg }

        }
    }

    override fun onReset() {
        super.onReset()
        this.listeners.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.listeners.clear()
        this.notificationStack.clear()
    }

    @SuppressLint("RestrictedApi")
    private fun notifyListeners(eventType: EventType, intent: Intent) {
        val data = intent.extras
        if (data != null && data.containsKey(PushManager.EXTRA_PUSH_MESSAGE_BUNDLE)) {
            val dataString = data.toString();
            UALog.i { "Notification message on new intent: $dataString" }
            val message = PushMessage.fromIntent(intent) ?: return
            val id: Int = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, -1)
            val tag: String? = intent.getStringExtra(PushManager.EXTRA_NOTIFICATION_TAG)
            val notificationInfo = NotificationInfo(message, id, tag)
            val listeners = this.listeners[eventType];
            val pluginResult = jsonMapOf(
                "pushPayload" to Utils.notificationMap(
                    notificationInfo.message,
                ),
            ).pluginResult()
            if (listeners?.isNotEmpty() == true) {
                listeners.forEach { listener ->
                    pluginResult.keepCallback = true
                    listener.callbackContext.sendPluginResult(pluginResult)
                }
            } else {
                this.notificationStack.getOrPut(eventType) { mutableListOf() }.add(notificationInfo)
            }
        }
    }

    private fun addListener(args: JSONArray, callbackContext: CallbackContext) {
        val jsonArgs = JsonValue.wrap(args).requireList()

        val eventName = jsonArgs.get(0).requireString()
        val event: EventType = EVENT_NAME_MAP.firstNotNullOf {
            if (it.value == eventName) {
                it.key
            } else {
                null
            }
        }

        val listener = Listener(
            listenerId = jsonArgs.get(1).requireInt(),
            callbackContext = callbackContext
        )

        this.listeners.getOrPut(event) { mutableListOf() }.add(listener)

        this.notificationStack[event]?.forEach { notificationInfo ->
            val pluginResult = jsonMapOf(
                "pushPayload" to Utils.notificationMap(
                    notificationInfo.message,
                ),
            ).pluginResult()
            pluginResult.keepCallback = true
            listener.callbackContext.sendPluginResult(pluginResult)
            this.notificationStack[event]?.remove(notificationInfo)
        }

        notifyPendingEvents()
    }

    private fun removeListener(args: JSONArray) {
        val jsonArgs = JsonValue.wrap(args).requireList()

        val eventName = jsonArgs.get(0).requireString()
        val event: EventType = EVENT_NAME_MAP.firstNotNullOf {
            if (it.value == eventName) {
                it.key
            } else {
                null
            }
        }

        val listenerId = jsonArgs.get(1).requireInt()
        this.listeners[event]?.removeAll {
            it.listenerId == listenerId
        }
    }

    private fun notifyPendingEvents() {
        EventType.values().forEach { eventType ->
            val listeners = this.listeners[eventType]
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

        when (method) {
            // Airship
            "takeOff" -> callback.resolveResult(method) { proxy.takeOff(arg) }
            "isFlying" -> callback.resolveResult(method) { proxy.isFlying() }

            // Channel
            "channel#getChannelId" -> callback.resolveResult(method) { proxy.channel.getChannelId() }

            "channel#editTags" -> callback.resolveResult(method) { proxy.channel.editTags(arg) }
            "channel#getTags" -> callback.resolveResult(method) { proxy.channel.getTags().toList() }
            "channel#editTagGroups" -> callback.resolveResult(method) { proxy.channel.editTagGroups(arg) }
            "channel#editSubscriptionLists" -> callback.resolveResult(method) { proxy.channel.editSubscriptionLists(arg) }
            "channel#editAttributes" -> callback.resolveResult(method) { proxy.channel.editAttributes(arg) }
            "channel#getSubscriptionLists" -> callback.resolvePending(method) { proxy.channel.getSubscriptionLists() }
            "channel#enableChannelCreation" -> callback.resolveResult(method) { proxy.channel.enableChannelCreation() }

            // Contact
            "contact#reset" -> callback.resolveResult(method) { proxy.contact.reset() }
            "contact#notifyRemoteLogin" -> callback.resolveResult(method) { proxy.contact.notifyRemoteLogin() }
            "contact#identify" -> callback.resolveResult(method) { proxy.contact.identify(arg.requireString()) }
            "contact#getNamedUserId" -> callback.resolveResult(method) { proxy.contact.getNamedUserId() }
            "contact#editTagGroups" -> callback.resolveResult(method) { proxy.contact.editTagGroups(arg) }
            "contact#editSubscriptionLists" -> callback.resolveResult(method) { proxy.contact.editSubscriptionLists(arg) }
            "contact#editAttributes" -> callback.resolveResult(method) { proxy.contact.editAttributes(arg) }
            "contact#getSubscriptionLists" -> callback.resolvePending(method) { proxy.contact.getSubscriptionLists() }

            // Push
            "push#setUserNotificationsEnabled" -> callback.resolveResult(method) { proxy.push.setUserNotificationsEnabled(arg.requireBoolean()) }
            "push#enableUserNotifications" -> callback.resolvePending(method) { proxy.push.enableUserPushNotifications() }
            "push#isUserNotificationsEnabled" -> callback.resolveResult(method) { proxy.push.isUserNotificationsEnabled() }
            "push#getNotificationStatus" -> callback.resolveResult(method) { proxy.push.getNotificationStatus() }
            "push#getActiveNotifications" -> callback.resolveResult(method) { proxy.push.getActiveNotifications() }
            "push#clearNotification" -> callback.resolveResult(method) { proxy.push.clearNotification(arg.requireString()) }
            "push#clearNotifications" -> callback.resolveResult(method) { proxy.push.clearNotifications() }
            "push#getPushToken" -> callback.resolveResult(method) { proxy.push.getRegistrationToken() }
            "push#android#isNotificationChannelEnabled" -> callback.resolveResult(method) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    proxy.push.isNotificationChannelEnabled(arg.requireString())
                } else {
                    true
                }
            }
            "push#android#setNotificationConfig" -> callback.resolveResult(method) { proxy.push.setNotificationConfig(arg) }
            "push#android#setForegroundNotificationsEnabled" -> callback.resolveResult(method) {
                proxy.push.isForegroundNotificationsEnabled = arg.requireBoolean()
                return@resolveResult Unit
            }
            "push#android#isForegroundNotificationsEnabled" -> callback.resolveResult(method) {
                proxy.push.isForegroundNotificationsEnabled
            }

            // In-App
            "inApp#setPaused" -> callback.resolveResult(method) { proxy.inApp.setPaused(arg.getBoolean(false)) }
            "inApp#isPaused" -> callback.resolveResult(method) { proxy.inApp.isPaused() }
            "inApp#setDisplayInterval" -> callback.resolveResult(method) { proxy.inApp.setDisplayInterval(arg.getLong(0)) }
            "inApp#getDisplayInterval" -> callback.resolveResult(method) { proxy.inApp.getDisplayInterval() }

            // Analytics
            "analytics#trackScreen" -> callback.resolveResult(method) { proxy.analytics.trackScreen(arg.optString()) }
            "analytics#addCustomEvent" -> callback.resolveResult(method) { proxy.analytics.addEvent(arg) }
            "analytics#associateIdentifier" -> {
                val associatedIdentifierArgs = arg.requireStringList()
                proxy.analytics.associateIdentifier(
                    associatedIdentifierArgs[0],
                    associatedIdentifierArgs.getOrNull(1)
                )
            }

            // Message Center
            "messageCenter#getMessages" -> callback.resolveResult(method) {
                JsonValue.wrapOpt(proxy.messageCenter.getMessages())
            }
            "messageCenter#dismiss" -> callback.resolveResult(method) { proxy.messageCenter.dismiss() }
            "messageCenter#display" -> callback.resolveResult(method) { proxy.messageCenter.display(arg.optString()) }
            "messageCenter#showMessageView" -> callback.resolveResult(method) { proxy.messageCenter.showMessageView(arg.requireString()) }
            "messageCenter#markMessageRead" -> callback.resolveResult(method) { proxy.messageCenter.markMessageRead(arg.requireString()) }
            "messageCenter#deleteMessage" -> callback.resolveResult(method) { proxy.messageCenter.deleteMessage(arg.requireString()) }
            "messageCenter#getUnreadMessageCount" -> callback.resolveResult(method) { proxy.messageCenter.getUnreadMessagesCount() }
            "messageCenter#setAutoLaunchDefaultMessageCenter" -> callback.resolveResult(method) { proxy.messageCenter.setAutoLaunchDefaultMessageCenter(arg.requireBoolean()) }
            "messageCenter#refreshMessages" -> callback.resolveDeferred(method) { resolveCallback ->
                proxy.messageCenter.refreshInbox().addResultCallback {
                    if (it == true) {
                        resolveCallback(null, null)
                    } else {
                        resolveCallback(null, Exception("Failed to refresh"))
                    }
                }
            }

            // Preference Center
            "preferenceCenter#display" -> callback.resolveResult(method) { proxy.preferenceCenter.displayPreferenceCenter(arg.requireString()) }
            "preferenceCenter#getConfig" -> callback.resolvePending(method) { proxy.preferenceCenter.getPreferenceCenterConfig(arg.requireString()) }
            "preferenceCenter#setAutoLaunchPreferenceCenter" -> callback.resolveResult(method) {
                val autoLaunchArgs = arg.requireList()
                proxy.preferenceCenter.setAutoLaunchPreferenceCenter(
                    autoLaunchArgs.get(0).requireString(),
                    autoLaunchArgs.get(1).getBoolean(false)
                )
            }

            // Privacy Manager
            "privacyManager#setEnabledFeatures" -> callback.resolveResult(method) { proxy.privacyManager.setEnabledFeatures(arg.requireStringList()) }
            "privacyManager#getEnabledFeatures" -> callback.resolveResult(method) { proxy.privacyManager.getFeatureNames() }
            "privacyManager#enableFeatures" -> callback.resolveResult(method) { proxy.privacyManager.enableFeatures(arg.requireStringList()) }
            "privacyManager#disableFeatures" -> callback.resolveResult(method) { proxy.privacyManager.disableFeatures(arg.requireStringList()) }
            "privacyManager#isFeaturesEnabled" -> callback.resolveResult(method) { proxy.privacyManager.isFeatureEnabled(arg.requireStringList()) }

            // Locale
            "locale#setLocaleOverride" -> callback.resolveResult(method) { proxy.locale.setCurrentLocale(arg.requireString()) }
            "locale#getCurrentLocale" -> callback.resolveResult(method) { proxy.locale.getCurrentLocale() }
            "locale#clearLocaleOverride" -> callback.resolveResult(method) { proxy.locale.clearLocale() }

            // Actions
            "actions#run" -> callback.resolveDeferred(method) { resolveCallback ->
                val actionArgs = arg.requireList()
                val name= actionArgs.get(0).requireString()
                val value: JsonValue? = if (actionArgs.size() == 2) { actionArgs.get(1) } else { null }

                proxy.actions.runAction(name, value)
                    .addResultCallback { actionResult ->
                        if (actionResult != null && actionResult.status == ActionResult.STATUS_COMPLETED) {
                            resolveCallback(actionResult.value, null)
                        } else {
                            resolveCallback(null, Exception("Action failed ${actionResult?.status}"))
                        }
                    }
            }

            // Feature Flag
            "featureFlagManager#flag" -> callback.resolveDeferred(method) { resolveCallback ->
                scope.launch {
                    try {
                        val flag = proxy.featureFlagManager.flag(arg.requireString())
                        resolveCallback(flag, null)
                    } catch (e: Exception) {
                        resolveCallback(null, e)
                    }
                }
            }

            "featureFlagManager#trackInteraction" -> {
                callback.resolveDeferred(method) { resolveCallback ->
                    scope.launch {
                        try {
                            val featureFlagProxy = FeatureFlagProxy(arg)
                            proxy.featureFlagManager.trackInteraction(flag = featureFlagProxy)
                            resolveCallback(null, null)
                        } catch (e: Exception) {
                            resolveCallback(null, e)
                        }
                    }
                }
            }

            else -> callback.error("Not implemented")
        }
    }
}


internal fun CallbackContext.resolveResult(method: String, function: () -> Any?) {
    resolveDeferred(method) { callback -> callback(function(), null) }
}

internal fun CallbackContext.error(method: String, exception: java.lang.Exception) {
    this.error("AIRSHIP_ERROR(method=$method, exception=$exception)")
}

internal fun <T> CallbackContext.resolveDeferred(method: String, function: ((T?, Exception?) -> Unit) -> Unit) {
    try {
        function { result, error ->
            if (error != null) {
                this.error(method, error)
            } else {
                try {
                    when (result) {
                        is Unit -> {
                            this.success()
                        }
                        is Int -> {
                            this.success(result)
                        }
                        is String -> {
                            this.success(result)
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
                    this.error(method, e)
                }
            }
        }
    } catch (e: Exception) {
        this.error(method, e)
    }
}

internal fun <T> CallbackContext.resolvePending(method: String, function: () -> PendingResult<T>) {
    resolveDeferred(method) { callback ->
        function().addResultCallback {
            callback(it, null)
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
        json.isNull -> PluginResult(PluginResult.Status.OK)
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
