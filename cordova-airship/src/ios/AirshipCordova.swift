/* Copyright Airship and Contributors */

import Foundation
import AirshipKit
import AirshipFrameworkProxy

@objc(AirshipCordova)
public final class AirshipCordova: CDVPlugin {

    struct Listener {
        let callbackID: String
        let listenerID: Int
    }

    @MainActor
    private var eventListeners: [AirshipProxyEventType: [Listener]] = [:]

    private static let eventNames: [AirshipProxyEventType: String] = [
        .authorizedNotificationSettingsChanged: "airship.event.ios_authorized_notification_settings_changed",
        .pushTokenReceived: "airship.event.push_token_received",
        .deepLinkReceived: "airship.event.deep_link_received",
        .channelCreated: "airship.event.channel_created",
        .messageCenterUpdated: "airship.event.message_center_updated",
        .displayMessageCenter: "airship.event.display_message_center",
        .displayPreferenceCenter: "airship.event.display_preference_center",
        .notificationResponseReceived: "airship.event.notification_response",
        .pushReceived: "airship.event.push_received",
        .notificationStatusChanged: "airship.event.notification_status_changed"
    ]

    @MainActor
    public override func pluginInitialize() {
        let settings = AirshipCordovaPluginSettings.from(
            settings: self.commandDelegate.settings
        )

        AirshipCordovaAutopilot.shared.pluginInitialized(settings: settings)

        Task {
            for await _ in await AirshipProxyEventEmitter.shared.pendingEventAdded {
                await self.notifyPendingEvents()
            }
        }
    }

    @objc
    @MainActor
    func removeListener(_ command: CDVInvokedUrlCommand) {
        guard
            command.arguments.count == 2,
            let listenerID = command.arguments.last as? NSNumber,
            let eventName = command.arguments.first as? String
        else {
            AirshipLogger.error("Failed to add listener, invalid command \(command)")
            return
        }

        guard
            let eventType = Self.eventNames.first(where: { key, value in
                value == eventName
            })?.key
        else {
            AirshipLogger.error("Failed to add listener, invalid name \(eventName)")
            return
        }

        self.eventListeners[eventType]?.removeAll(where: { listener in
            listener.listenerID == listenerID.intValue
        })
    }

    @objc
    @MainActor
    func addListener(_ command: CDVInvokedUrlCommand) {
        guard
            let callbackID = command.callbackId,
            command.arguments.count == 2,
            let listenerID = command.arguments.last as? NSNumber,
            let eventName = command.arguments.first as? String
        else {
            AirshipLogger.error("Failed to add listener, invalid command \(command)")
            return
        }

        guard
            let eventType = Self.eventNames.first(where: { key, value in
                value == eventName
            })?.key
        else {
            AirshipLogger.error("Failed to add listener, invalid name \(eventName)")
            return
        }

        if self.eventListeners[eventType] == nil {
            self.eventListeners[eventType] = []
        }

        self.eventListeners[eventType]?.append(
            Listener(callbackID: callbackID, listenerID: listenerID.intValue)
        )

        Task {
            await notifyPendingEvents()
        }
    }

    @MainActor
    private func notifyPendingEvents() async {
        let listeners = self.eventListeners

        for eventType in AirshipProxyEventType.allCases {
            await AirshipProxyEventEmitter.shared.sendPendingEvents(
                eventType: eventType,
                listeners: listeners[eventType],
                commandDelegate: self.commandDelegate
            )
        }
    }

    @objc
    func perform(_ command: CDVInvokedUrlCommand) {
        Task {
            do {
                let result = try await self.handle(command: command)
                let pluginResult = try self.successResult(value: result)
                self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
            } catch {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription)
                self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
            }
        }
    }

    private func successResult(value: Any?, fallbackJSON: Bool = true) throws -> CDVPluginResult {
        guard let value = value else {
            return CDVPluginResult(status: CDVCommandStatus_OK)
        }

        if let string = value as? String {
            return CDVPluginResult(status: CDVCommandStatus_OK, messageAs: string)
        }

        if let bool = value as? Bool {
            return CDVPluginResult(status: CDVCommandStatus_OK, messageAs: bool)
        }

        if let int = value as? Int {
            return CDVPluginResult(status: CDVCommandStatus_OK, messageAs: int)
        }

        if let int = value as? UInt {
            return CDVPluginResult(status: CDVCommandStatus_OK, messageAs: int)
        }

        if let number = value as? NSNumber {
            return CDVPluginResult(status: CDVCommandStatus_OK, messageAs: number.doubleValue)
        }

        if let array = value as? Array<Any> {
            return CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: try AirshipJSON.wrap(array).unWrap() as? Array<Any>
            )
        }

        if let dictionary = value as? [String: Any] {
            return CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: try AirshipJSON.wrap(dictionary).unWrap() as? [String: Any]
            )
        }

        if fallbackJSON {
            return try successResult(value: try AirshipJSON.wrap(value).unWrap(), fallbackJSON: false)
        } else {
            throw AirshipErrors.error("Invalid result \(value)")
        }
    }

    @MainActor
    private func handle(command: CDVInvokedUrlCommand) async throws -> Any? {
        guard let method = command.arguments[0] as? String else {
            throw AirshipErrors.error("Invalid command \(command)")
        }

        switch method {

        // Airship
        case "takeOff":
            return try AirshipCordovaAutopilot.shared.attemptTakeOff(
                json: try command.requireAnyArg()
            )

        case "isFlying":
            return AirshipProxy.shared.isFlying()

        // Channel
        case "channel#getChannelId":
            return try AirshipProxy.shared.channel.getChannelId()

        case "channel#editTags":
            try AirshipProxy.shared.channel.editTags(
                json: try command.requireAnyArg()
            )
            return nil

        case "channel#getTags":
            return try AirshipProxy.shared.channel.getTags()

        case "channel#editTagGroups":
            try AirshipProxy.shared.channel.editTagGroups(
                json: try command.requireAnyArg()
            )
            return nil

        case "channel#editSubscriptionLists":
            try AirshipProxy.shared.channel.editSubscriptionLists(
                json: try command.requireAnyArg()
            )
            return nil

        case "channel#editAttributes":
            try AirshipProxy.shared.channel.editAttributes(
                json: try command.requireAnyArg()
            )
            return nil

        case "channel#getSubscriptionLists":
            return try await AirshipProxy.shared.channel.getSubscriptionLists()

        case "channel#enableChannelCreation": 
            return try AirshipProxy.shared.channel.enableChannelCreation()

        // Contact
        case "contact#editTagGroups":
            try AirshipProxy.shared.contact.editTagGroups(
                json: try command.requireAnyArg()
            )
            return nil

        case "contact#editSubscriptionLists":
            try AirshipProxy.shared.contact.editSubscriptionLists(
                json: try command.requireAnyArg()
            )
            return nil

        case "contact#editAttributes":
            try AirshipProxy.shared.contact.editAttributes(
                json: try command.requireAnyArg()
            )
            return nil

        case "contact#getSubscriptionLists":
            return try await AirshipProxy.shared.contact.getSubscriptionLists()

        case "contact#identify":
            try AirshipProxy.shared.contact.identify(
                try command.requireStringArg()
            )
            return nil

        case "contact#reset":
            try AirshipProxy.shared.contact.reset()
            return nil

        case "contact#notifyRemoteLogin":
            try AirshipProxy.shared.contact.notifyRemoteLogin()
            return nil

        case "contact#getNamedUserId":
            return try await AirshipProxy.shared.contact.getNamedUser()


        // Push
        case "push#getPushToken":
            return try AirshipProxy.shared.push.getRegistrationToken()

        case "push#setUserNotificationsEnabled":
            try AirshipProxy.shared.push.setUserNotificationsEnabled(
                try command.requireBooleanArg()
            )
            return nil

        case "push#enableUserNotifications":
            return try await AirshipProxy.shared.push.enableUserPushNotifications()

        case "push#isUserNotificationsEnabled":
            return try AirshipProxy.shared.push.isUserNotificationsEnabled()

        case "push#getNotificationStatus":
            return try await AirshipProxy.shared.push.getNotificationStatus()

        case "push#getActiveNotifications":
            return await AirshipProxy.shared.push.getActiveNotifications()

        case "push#clearNotification":
            AirshipProxy.shared.push.clearNotification(
                try command.requireStringArg()
            )
            return nil

        case "push#clearNotifications":
            AirshipProxy.shared.push.clearNotifications()
            return nil

        case "push#ios#getBadgeNumber":
            return try AirshipProxy.shared.push.getBadgeNumber()

        case "push#ios#setBadgeNumber":
            try await AirshipProxy.shared.push.setBadgeNumber(
                try command.requireIntArg()
            )
            return nil

        case "push#ios#setAutobadgeEnabled":
            try AirshipProxy.shared.push.setAutobadgeEnabled(
                try command.requireBooleanArg()
            )
            return nil

        case "push#ios#isAutobadgeEnabled":
            return try AirshipProxy.shared.push.isAutobadgeEnabled()

        case "push#ios#resetBadge":
            try await AirshipProxy.shared.push.setBadgeNumber(0)
            return nil

        case "push#ios#setNotificationOptions":
            try AirshipProxy.shared.push.setNotificationOptions(
                names: try command.requireStringArrayArg()
            )
            return nil

        case "push#ios#setForegroundPresentationOptions":
            try AirshipProxy.shared.push.setForegroundPresentationOptions(
                names: try command.requireStringArrayArg()
            )
            return nil

        case "push#ios#getAuthorizedNotificationStatus":
            return try AirshipProxy.shared.push.getAuthroizedNotificationStatus()

        case "push#ios#getAuthorizedNotificationSettings":
            return try AirshipProxy.shared.push.getAuthorizedNotificationSettings()

        case "push#ios#setQuietTimeEnabled":
            try AirshipProxy.shared.push.setQuietTimeEnabled(
                try command.requireBooleanArg()
            )
            return nil

        case "push#ios#isQuietTimeEnabled":
            return try AirshipProxy.shared.push.isQuietTimeEnabled()

        case "push#ios#setQuietTime":
            let proxySettings: CodableQuietTimeSettings = try command.requireCodableArg()
            try AirshipProxy.shared.push.setQuietTime(
                QuietTimeSettings(
                    startHour: proxySettings.startHour,
                    startMinute: proxySettings.startMinute,
                    endHour: proxySettings.endHour,
                    endMinute: proxySettings.endMinute
                )
            )
            return nil

        case "push#ios#getQuietTime":
            return try AirshipProxy.shared.push.getQuietTime()

        // In-App
        case "inApp#setPaused":
            try AirshipProxy.shared.inApp.setPaused(
                try command.requireBooleanArg()
            )
            return nil

        case "inApp#isPaused":
            return try AirshipProxy.shared.inApp.isPaused()

        case "inApp#setDisplayInterval":
            try AirshipProxy.shared.inApp.setDisplayInterval(
                try command.requireIntArg()
            )
            return nil

        case "inApp#getDisplayInterval":
            return try AirshipProxy.shared.inApp.getDisplayInterval()

        // Analytics
        case "analytics#trackScreen":
            try AirshipProxy.shared.analytics.trackScreen(
                try? command.requireStringArg()
            )
            return nil

        case "analytics#addCustomEvent":
            try AirshipProxy.shared.analytics.addEvent(
                command.requireAnyArg()
            )
            return nil

        case "analytics#associateIdentifier":
            let args = try command.requireStringArrayArg()
            guard args.count == 1 || args.count == 2 else {
                throw AirshipErrors.error("Call requires 1 to 2 strings.")
            }
            try AirshipProxy.shared.analytics.associateIdentifier(
                identifier: args.count == 2 ? args[1] : nil,
                key: args[0]
            )
            return nil

        // Message Center
        case "messageCenter#getMessages":
            return try? await AirshipProxy.shared.messageCenter.getMessages()

        case "messageCenter#display":
            try AirshipProxy.shared.messageCenter.display(
                messageID: try? command.requireStringArg()
            )
            return nil

        case "messageCenter#showMessageView":
            try AirshipProxy.shared.messageCenter.showMessageView(
                messageID: try command.requireStringArg()
            )
            return nil

        case "messageCenter#dismiss":
            try AirshipProxy.shared.messageCenter.dismiss()
            return nil

        case "messageCenter#markMessageRead":
            try await AirshipProxy.shared.messageCenter.markMessageRead(
                messageID: command.requireStringArg()
            )
            return nil

        case "messageCenter#deleteMessage":
            try await AirshipProxy.shared.messageCenter.deleteMessage(
                messageID: command.requireStringArg()
            )
            return nil

        case "messageCenter#getUnreadMessageCount":
            return try await AirshipProxy.shared.messageCenter.getUnreadCount()

        case "messageCenter#refreshMessages":
            try await AirshipProxy.shared.messageCenter.refresh()
            return nil

        case "messageCenter#setAutoLaunchDefaultMessageCenter":
            AirshipProxy.shared.messageCenter.setAutoLaunchDefaultMessageCenter(
                try command.requireBooleanArg()
            )
            return nil

        // Preference Center
        case "preferenceCenter#display":
            try AirshipProxy.shared.preferenceCenter.displayPreferenceCenter(
                preferenceCenterID: try command.requireStringArg()
            )
            return nil

        case "preferenceCenter#getConfig":
            return try await AirshipProxy.shared.preferenceCenter.getPreferenceCenterConfig(
                preferenceCenterID: try command.requireStringArg()
            )

        case "preferenceCenter#setAutoLaunchPreferenceCenter":
            let args = try command.requireArrayArg()
            guard
                args.count == 2,
                let identifier = args[0] as? String,
                let autoLaunch = args[1] as? Bool
            else {
                throw AirshipErrors.error("Call requires [String, Bool]")
            }

            AirshipProxy.shared.preferenceCenter.setAutoLaunchPreferenceCenter(
                autoLaunch,
                preferenceCenterID: identifier
            )
            return nil

        // Privacy Manager
        case "privacyManager#setEnabledFeatures":
            try AirshipProxy.shared.privacyManager.setEnabled(
                featureNames: try command.requireStringArrayArg()
            )
            return nil

        case "privacyManager#getEnabledFeatures":
            return try AirshipProxy.shared.privacyManager.getEnabledNames()

        case "privacyManager#enableFeatures":
            try AirshipProxy.shared.privacyManager.enable(
                featureNames: try command.requireStringArrayArg()
            )
            return nil

        case "privacyManager#disableFeatures":
            try AirshipProxy.shared.privacyManager.disable(
                featureNames: try command.requireStringArrayArg()
            )
            return nil

        case "privacyManager#isFeaturesEnabled":
            return try AirshipProxy.shared.privacyManager.isEnabled(
                featuresNames: try command.requireStringArrayArg()
            )

        // Locale
        case "locale#setLocaleOverride":
            try AirshipProxy.shared.locale.setCurrentLocale(
                try command.requireStringArg()
            )
            return nil

        case "locale#clearLocaleOverride":
            try AirshipProxy.shared.locale.clearLocale()
            return nil

        case "locale#getCurrentLocale":
            return try AirshipProxy.shared.locale.getCurrentLocale()

        // Actions
        case "actions#run":
            let args = try command.requireArrayArg()
            guard
                args.count == 1 || args.count == 2,
                let actionName = args[0] as? String
            else {
                throw AirshipErrors.error("Call requires [String, Any?]")
            }

            let arg = try? AirshipJSON.wrap(args[1])
            let result = try await AirshipProxy.shared.action.runAction(
                actionName,
                value: args.count == 2 ? arg : nil
            ) as? AirshipJSON
            return result?.unWrap()

        // Feature Flag
        case "featureFlagManager#flag":
            return try await AirshipProxy.shared.featureFlagManager.flag(
                name: try command.requireStringArg()
            )

        case "featureFlagManager#trackInteraction":
            try AirshipProxy.shared.featureFlagManager.trackInteraction(
                flag: command.requireCodableArg()
            )

            return nil
        default:
            throw AirshipErrors.error("Unavailable command \(method)")
        }
    }
}


extension CDVInvokedUrlCommand {

    func requireCodableArg<T: Codable>() throws -> T  {
        guard
            self.arguments.count >= 2
        else {
            throw AirshipErrors.error("Missing argument")
        }

        return try AirshipJSON.wrap(self.arguments[1]).decode()
    }

    func requireArrayArg() throws -> [Any] {
        guard
            self.arguments.count >= 2,
            let args = self.arguments[1] as? [Any]
        else {
            throw AirshipErrors.error("Argument must be an array")
        }

        return args
    }
    
    func requireArrayArg<T>(count: UInt, parse: (Any) throws -> T) throws -> [T] {
        guard
            self.arguments.count >= 2,
            let args = self.arguments[1] as? [Any],
            args.count == count
        else {
            throw AirshipErrors.error("Invalid argument array")
        }

        return try args.map { try parse($0) }
    }


    func requireStringArrayArg() throws -> [String] {
        guard
            self.arguments.count >= 2,
            let args = self.arguments[1] as? [String] 
        else {
            throw AirshipErrors.error("Argument must be a string array")
        }

        return args
    }

    func requireAnyArg() throws -> Any {
        guard
            self.arguments.count >= 2
        else {
            throw AirshipErrors.error("Argument must not be null")
        }

        return self.arguments[1]
    }

    func requireBooleanArg() throws -> Bool {
        guard
            self.arguments.count >= 2,
            let args = self.arguments[1] as? Bool
        else {
            throw AirshipErrors.error("Argument must be a boolean")
        }

        return args
    }

    func requireStringArg() throws -> String {
        
        guard
            self.arguments.count >= 2,
            let args = self.arguments[1] as? String
        else {
            throw AirshipErrors.error("Argument must be a string")
        }

        return args
    }

    func requireIntArg() throws -> Int {
        let value = try requireAnyArg()

        if let int = value as? Int {
            return int
        }

        if let double = value as? Double {
            return Int(double)
        }

        if let number = value as? NSNumber {
            return number.intValue
        }

        throw AirshipErrors.error("Argument must be an int")
    }

    func requireDoubleArg() throws -> Double {
        let value = try requireAnyArg()


        if let double = value as? Double {
            return double
        }

        if let int = value as? Int {
            return Double(int)
        }

        if let number = value as? NSNumber {
            return number.doubleValue
        }


        throw AirshipErrors.error("Argument must be a double")
    }
}

extension AirshipProxyEventEmitter {
    func sendPendingEvents(
        eventType: AirshipProxyEventType,
        listeners: [AirshipCordova.Listener]?,
        commandDelegate: CDVCommandDelegate?
    ) {
        guard
            let commandDelegate = commandDelegate,
            let listeners = listeners,
            listeners.count > 0
        else {
            return
        }

        self.processPendingEvents(type: eventType) { event in
            let result = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: event.body
            )
            result?.keepCallback = true

            listeners.forEach { listener in
                commandDelegate.send(result, callbackId: listener.callbackID)
            }

            return true
        }
    }
}

public struct CodableQuietTimeSettings: Codable {
    let startHour: UInt
    let startMinute: UInt
    let endHour: UInt
    let endMinute: UInt
}
