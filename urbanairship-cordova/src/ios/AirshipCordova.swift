/* Copyright Airship and Contributors */

import Foundation
import AirshipKit
import AirshipFrameworkProxy

@objc
public class AirshipCordova: CDVPlugin {
    @objc
    public static let pendingEventsEventName = "com.airship.pending_events"

    @objc
    public static let overridePresentationOptionsEventName = "com.airship.ios.override_presentation_options"

    
    var lock = AirshipLock()
    var pendingPresentationRequests: [String: PresentationOptionsOverridesRequest] = [:]
    
    @objc
    public var overridePresentationOptionsEnabled: Bool = false {
        didSet {
            if (!overridePresentationOptionsEnabled) {
                lock.sync {
                    self.pendingPresentationRequests.values.forEach { request in
                        request.result(options: nil)
                    }
                    self.pendingPresentationRequests.removeAll()
                }
            }
        }
    }

    public static var proxy: AirshipProxy {
        AirshipProxy.shared
    }

    public static let version: String = "15.2.2"

    private let eventNotifier = EventNotifier()

    @objc
    public static let shared: AirshipCordova = AirshipCordova()

    @objc
    public func setNotifier(_ notifier: ((String, [String: Any]) -> Void)?) {
        Task {
            if let notifier = notifier {
                await eventNotifier.setNotifier({
                    notifier(AirshipCordova.pendingEventsEventName, [:])
                })
                
                if await AirshipProxyEventEmitter.shared.hasAnyEvents() {
                    await eventNotifier.notifyPendingEvents()
                }
                
                
                AirshipProxy.shared.push.presentationOptionOverrides = { request in
                    guard self.overridePresentationOptionsEnabled else {
                        request.result(options: nil)
                        return
                    }
                    
                    let requestID = UUID().uuidString
                    self.lock.sync {
                        self.pendingPresentationRequests[requestID] = request
                    }
                    notifier(
                        AirshipCordova.overridePresentationOptionsEventName,
                        [
                            "pushPayload": request.pushPayload,
                            "requestId": requestID
                        ]
                    )
                }
            } else {
                await eventNotifier.setNotifier(nil)
                AirshipProxy.shared.push.presentationOptionOverrides = nil
                
                lock.sync {
                    self.pendingPresentationRequests.values.forEach { request in
                        request.result(options: nil)
                    }
                    self.pendingPresentationRequests.removeAll()
                }
            }
        }
    }
    
    @objc
    public func presentationOptionOverridesResult(requestID: String, presentationOptions: [String]?) {
        lock.sync {
            pendingPresentationRequests[requestID]?.result(optionNames: presentationOptions)
            pendingPresentationRequests[requestID] = nil
        }
    }
    

    @objc
    @MainActor
    public func onLoad(
        launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) {
        AirshipProxy.shared.delegate = self
        try? AirshipProxy.shared.attemptTakeOff(launchOptions: launchOptions)

        Task {
            let stream = await AirshipProxyEventEmitter.shared.pendingEventAdded
            for await _ in stream {
                await self.eventNotifier.notifyPendingEvents()
            }
        }
    }

    @objc
    @MainActor
    public func attemptTakeOff(
        launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    )  {
        try? AirshipProxy.shared.attemptTakeOff(launchOptions: launchOptions)
    }

}

// Airship
public extension AirshipCordova {
    @objc
    @MainActor
    func takeOff(
        json: Any,
        launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) throws -> NSNumber {
        return try NSNumber(
            value: AirshipProxy.shared.takeOff(
                json: json,
                launchOptions: launchOptions
            )
        )
    }

    @objc
    func isFlying() -> Bool {
        return Airship.isFlying
    }

}

// Channel
@objc
public extension AirshipCordova {
    
    func setTags(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let tags: [String] = args[0]
        
        AirshipProxy.shared.channel.addTags(tags)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelRemoveTag(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let tag: String = args[0]
        AirshipProxy.shared.channel.removeTags([tag])
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelEnableChannelCreation(command: CDVInvokedUrlCommand) {
        AirshipProxy.shared.channel.enableChannelCreation()
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelGetTags(command: CDVInvokedUrlCommand) {
        let tags = AirshipProxy.shared.channel.getTags()
        
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: tags
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelGetSubscriptionLists(command: CDVInvokedUrlCommand) {
        let lists = await AirshipProxy.shared.channel.getSubscriptionLists()
        
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: lists
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelGetChannelIdOrEmpty(command: CDVInvokedUrlCommand) {
        let channelID = AirshipProxy.shared.channel.getChannelId() ?? ""
        
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: channelID
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelEditTagGroups(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let json: Any = args[0]
        
        AirshipProxy.shared.channel.editTagGroups(json: json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelEditAttributes(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let json: Any = args[0]
        
        AirshipProxy.shared.channel.editAttributes(json: json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func channelEditSubscriptionLists(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let json: Any = args[0]
        
        AirshipProxy.shared.channel.editSubscriptionLists(json: json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Push
@objc
public extension AirshipCordova {
    @objc
    func pushSetUserNotificationsEnabled(command: CDVInvokedUrlCommand) -> Void {
        let args = command.arguments
        let enabled: Bool = args[0]
        
        AirshipProxy.shared.push.setUserNotificationsEnabled(enabled)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushIsUserNotificationsEnabled(command: CDVInvokedUrlCommand) -> NSNumber {
        let isEnabled = NSNumber(
                value: AirshipProxy.shared.push.isUserNotificationsEnabled()
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: isEnabled
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushEnableUserNotifications(command: CDVInvokedUrlCommand) -> Bool {
        await AirshipProxy.shared.push.enableUserPushNotifications()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushGetRegistrationTokenOrEmpty(command: CDVInvokedUrlCommand) -> String {
        let token = AirshipProxy.shared.push.getRegistrationToken() ?? ""
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: token
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushSetNotificationOptions(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let names: [String] = args[0]
        AirshipProxy.shared.push.setNotificationOptions(names: names)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushSetForegroundPresentationOptions(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let names: [String] = args[0]
        try AirshipProxy.shared.push.setForegroundPresentationOptions(
            names: names
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushGetNotificationStatus(command: CDVInvokedUrlCommand) async throws -> [String: Any] {
        let status = try await AirshipProxy.shared.push.getNotificationStatus()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: status
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushSetAutobadgeEnabled(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let enabled: Bool = args[0]
        try AirshipProxy.shared.push.setAutobadgeEnabled(enabled)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushIsAutobadgeEnabled(command: CDVInvokedUrlCommand) throws -> NSNumber {
        let isEnabled = try NSNumber(
            value: AirshipProxy.shared.push.isAutobadgeEnabled()
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: isEnabled
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    @MainActor
    func pushSetBadgeNumber(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let badgeNumber: Double = args[0]
        try AirshipProxy.shared.push.setBadgeNumber(Int(badgeNumber))
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    @MainActor
    func pushGetBadgeNumber(command: CDVInvokedUrlCommand) throws -> NSNumber {
        let badgeNumber = try NSNumber(
            value: AirshipProxy.shared.push.getBadgeNumber()
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: badgeNumber
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushGetAuthorizedNotificationStatus(command: CDVInvokedUrlCommand) async throws -> [String : Any] {
        let status = try await AirshipProxy.shared.push.getNotificationStatus()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: status
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushClearNotifications(command: CDVInvokedUrlCommand) {
        AirshipProxy.shared.push.clearNotifications()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushClearNotification(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let identifier: String = args[0]
        AirshipProxy.shared.push.clearNotification(identifier)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func pushGetActiveNotifications(command: CDVInvokedUrlCommand) async -> [[String: Any]] {
        let activeNotifications =  await AirshipProxy.shared.push.getActiveNotifications()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: activeNotifications
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Actions
@objc
    
public extension AirshipCordova {
    func actionsRun(command: CDVInvokedUrlCommand) async throws-> Any? {
        let args = command.arguments
        
        guard let args.count >=2 else (
            return
        )
        
        let actionName: String = args[0]
        let actionValue = args[1]
        
        try await AirshipProxy.shared.action.runAction(
            actionName,
            value: try AirshipJSON.wrap(actionValue)
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Analytics
@objc
public extension AirshipCordova {

    func analyticsTrackScreen(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let screen: String? = args[0]
        try AirshipProxy.shared.analytics.trackScreen(screen)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
    
    func analyticsAssociateIdentifier(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let key: String? = args[0]
        let identifier: String? = args[1]
        try AirshipProxy.shared.analytics.associateIdentifier(
            identifier: identifier,
            key: key
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Contact
@objc
public extension AirshipCordova {

    @objc
    func contactIdentify(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let namedUser: String? = args[0]
        try AirshipProxy.shared.contact.identify(namedUser ?? "")
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func contactReset(command: CDVInvokedUrlCommand) throws {
        try AirshipProxy.shared.contact.reset()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func contactGetNamedUserIdOrEmtpy(command: CDVInvokedUrlCommand) async throws -> String {
        let namedUser = try await AirshipProxy.shared.contact.getNamedUser() ?? ""
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: namedUser
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func contactGetSubscriptionLists(command: CDVInvokedUrlCommand) async throws -> [String: [String]] {
        let lists = try await AirshipProxy.shared.contact.getSubscriptionLists()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: lists
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func contactEditTagGroups(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let json: any = args[0]
        try AirshipProxy.shared.contact.editTagGroups(json: json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
    }

    @objc
    func contactEditAttributes(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let json: any = args[0]
        try AirshipProxy.shared.contact.editAttributes(json: json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func contactEditSubscriptionLists(command: CDVInvokedUrlCommand)  throws {
        let args = command.arguments
        let json: any = args[0]
        try AirshipProxy.shared.contact.editSubscriptionLists(json: json)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
    }
}

// InApp
@objc
public extension AirshipCordova {

    func inAppIsPaused(command: CDVInvokedUrlCommand) throws -> NSNumber {
        let isPaused = try NSNumber(
            value: AirshipProxy.shared.inApp.isPaused()
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: isPaused
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    func inAppSetPaused(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let paused: Bool = args[0]
        try AirshipProxy.shared.inApp.setPaused(paused)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    func inAppSetDisplayInterval(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let milliseconds: Double = args[0]
        try AirshipProxy.shared.inApp.setDisplayInterval(Int(milliseconds))
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    func inAppGetDisplayInterval(command: CDVInvokedUrlCommand) throws -> NSNumber {
        let interval = try NSNumber(
            value: AirshipProxy.shared.inApp.getDisplayInterval()
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: interval
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Locale
@objc
public extension AirshipCordova {
    @objc
    func localeSetLocaleOverride(command: CDVInvokedUrlCommand) throws {
        let args = command.arguments
        let localeIdentifier: String? = args[0]
        try AirshipProxy.shared.locale.setCurrentLocale(localeIdentifier)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func localeClearLocaleOverride(command: CDVInvokedUrlCommand) throws {
        try AirshipProxy.shared.locale.clearLocale()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func localeGetLocale(command: CDVInvokedUrlCommand) throws -> String {
        let locale = try AirshipProxy.shared.locale.getCurrentLocale()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: locale
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Message Center
@objc
public extension AirshipCordova {
    @objc
    func messageCenterGetUnreadCount(command: CDVInvokedUrlCommand) async throws -> Double {
        let count = try await Double(AirshipProxy.shared.messageCenter.getUnreadCount())
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: count
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterGetMessages(command: CDVInvokedUrlCommand) async throws -> [Any] {
        let messages = try await AirshipProxy.shared.messageCenter.getMessagesJSON()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: messages
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterMarkMessageRead(command: CDVInvokedUrlCommand) async throws  {
        let args = command.arguments
        let messageId: String = args[0]
        try await AirshipProxy.shared.messageCenter.markMessageRead(
            messageID: messageId
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterDeleteMessage(command: CDVInvokedUrlCommand) async throws  {
        let args = command.arguments
        let messageId: String = args[0]
        try await AirshipProxy.shared.messageCenter.deleteMessage(
            messageID: messageId
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterDismiss(command: CDVInvokedUrlCommand) throws  {
        try AirshipProxy.shared.messageCenter.dismiss()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterDisplay(command: CDVInvokedUrlCommand) throws  {
        let args = command.arguments
        let messageId: String = args[0]
        try AirshipProxy.shared.messageCenter.display(messageID: messageId)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterRefresh(command: CDVInvokedUrlCommand) async throws  {
        try await AirshipProxy.shared.messageCenter.refresh()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func messageCenterSetAutoLaunchDefaultMessageCenter(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let autoLaunch: Bool = args[0]
        AirshipProxy.shared.messageCenter.setAutoLaunchDefaultMessageCenter(autoLaunch)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Preference Center
@objc
public extension AirshipCordova {
    @objc
    func preferenceCenterDisplay(command: CDVInvokedUrlCommand) throws  {
        let args = command.arguments
        let preferenceCenterId: String = args[0]
        try AirshipProxy.shared.preferenceCenter.displayPreferenceCenter(
            preferenceCenterID: preferenceCenterId
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func preferenceCenterGetConfig(command: CDVInvokedUrlCommand) async throws -> Any {
        let args = command.arguments
        let preferenceCenterId: String = args[0]
        let config = try await AirshipProxy.shared.preferenceCenter.getPreferenceCenterConfig(
            preferenceCenterID: preferenceCenterId
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: config
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func preferenceCenterAutoLaunchDefaultPreferenceCenter(command: CDVInvokedUrlCommand) {
        let args = command.arguments
        let preferenceCenterId: String = args[0]
        let autoLaunch: Bool = args[1]
        AirshipProxy.shared.preferenceCenter.setAutoLaunchPreferenceCenter(
            autoLaunch,
            preferenceCenterID: preferenceCenterId
        )
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}

// Privacy Manager
@objc
public extension AirshipCordova {
    @objc
    func privacyManagerSetEnabledFeatures(command: CDVInvokedUrlCommand) throws  {
        let args = command.arguments
        let features: [String] = args[0]
        try AirshipProxy.shared.privacyManager.setEnabled(featureNames: features)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func privacyManagerGetEnabledFeatures(command: CDVInvokedUrlCommand) throws -> [String] {
        let features = try AirshipProxy.shared.privacyManager.getEnabledNames()
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: features
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func privacyManagerEnableFeature(command: CDVInvokedUrlCommand) throws  {
        let args = command.arguments
        let features: [String] = args[0]
        try AirshipProxy.shared.privacyManager.enable(featureNames: features)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func privacyManagerDisableFeature(command: CDVInvokedUrlCommand) throws  {
        let args = command.arguments
        let features: [String] = args[0]
        try AirshipProxy.shared.privacyManager.disable(featureNames: features)
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }

    @objc
    func privacyManagerIsFeatureEnabled(command: CDVInvokedUrlCommand) throws -> NSNumber  {
        let args = command.arguments
        let features: [String] = args[0]
        let isEnabled = try NSNumber(
            value: AirshipProxy.shared.privacyManager.isEnabled(featuresNames: features)
        )
        let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: isEnabled
            )
        self.commandDelegate!.sendPluginResult(pluginResult, callbackId: command.callbackId)
    }
}


private actor EventNotifier {
    private var notifier: (() -> Void)?
    func setNotifier(_ notifier: (() -> Void)?) {
        self.notifier = notifier
    }

    func notifyPendingEvents() {
        self.notifier?()
    }
}

extension AirshipCordova: AirshipProxyDelegate {
    public func migrateData(store: ProxyStore) {
       
    }

    public func loadDefaultConfig() -> AirshipConfig {
        let config = AirshipConfig.default()
        config.requireInitialRemoteConfigEnabled = true
        return config
    }

    public func onAirshipReady() {
        Airship.analytics.registerSDKExtension(
            .reactNative,
            version: AirshipCordova.version
        )
    }
}
