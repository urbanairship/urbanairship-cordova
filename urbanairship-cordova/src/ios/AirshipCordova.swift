/* Copyright Airship and Contributors */

import Foundation
import AirshipKit
import AirshipFrameworkProxy

@objc
public class AirshipCordova: NSObject {
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
            let stream = await AirshipProxyEventEmitter.shared.pendingEventTypeAdded
            for await _ in stream {
                await self.eventNotifier.notifyPendingEvents()
            }
        }
    }
/*
    @objc
    public func onListenerAdded(eventName: String) {
        guard let type = try? AirshipProxyEventType.fromReactName(eventName) else {
            return
        }

        Task {
            if (await AirshipProxyEventEmitter.shared.hasEvent(type: type)) {
                await self.eventNotifier.notifyPendingEvents()
            }
        }
    }

    @objc
    public func takePendingEvents(eventName: String) async -> [Any] {
        guard let type = try? AirshipProxyEventType.fromReactName(eventName) else {
            return []
        }

        return await AirshipProxyEventEmitter.shared.takePendingEvents(
            type: type
        ).map { $0.body }
    }
*/

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
    
    @objc
    func channelAddTags(_ tags: [String]) throws {
        return try AirshipProxy.shared.channel.addTags(tags)
    }

    @objc
    func channelRemoveTag(_ tag: String) throws {
        return try AirshipProxy.shared.channel.removeTags([tag])
    }

    @objc
    func channelEnableChannelCreation() throws -> Void {
        try AirshipProxy.shared.channel.enableChannelCreation()
    }

    @objc
    func channelGetTags() throws -> [String] {
        return try AirshipProxy.shared.channel.getTags()
    }

    @objc
    func channelGetSubscriptionLists() async throws -> [String] {
        return try await AirshipProxy.shared.channel.getSubscriptionLists()
    }

    @objc
    func channelGetChannelIdOrEmpty() throws -> String {
        return try AirshipProxy.shared.channel.getChannelId() ?? ""
    }

    @objc
    func channelEditTagGroups(json: Any) throws {
        try AirshipProxy.shared.channel.editTagGroups(json: json)
    }

    @objc
    func channelEditAttributes(json: Any) throws {
        try AirshipProxy.shared.channel.editAttributes(json: json)
    }

    @objc
    func channelEditSubscriptionLists(json: Any) throws {
        try AirshipProxy.shared.channel.editSubscriptionLists(json: json)
    }
}

// Push
@objc
public extension AirshipCordova {
    @objc
    func pushSetUserNotificationsEnabled(
        _ enabled: Bool
    ) throws -> Void {
        try AirshipProxy.shared.push.setUserNotificationsEnabled(enabled)
    }

    @objc
    func pushIsUserNotificationsEnabled() throws -> NSNumber {
        return try NSNumber(
            value: AirshipProxy.shared.push.isUserNotificationsEnabled()
        )
    }

    @objc
    func pushEnableUserNotifications() async throws -> Bool {
        return try await AirshipProxy.shared.push.enableUserPushNotifications()
    }

    @objc
    func pushGetRegistrationTokenOrEmpty() throws -> String {
        return try AirshipProxy.shared.push.getRegistrationToken() ?? ""
    }

    @objc
    func pushSetNotificationOptions(
        names:[String]
    ) throws {
        try AirshipProxy.shared.push.setNotificationOptions(names: names)
    }

    @objc
    func pushSetForegroundPresentationOptions(
        names:[String]
    ) throws {
        try AirshipProxy.shared.push.setForegroundPresentationOptions(
            names: names
        )
    }

    @objc
    func pushGetNotificationStatus() async throws -> [String: Any] {
        return try AirshipProxy.shared.push.getNotificationStatus()
    }

    @objc
    func pushSetAutobadgeEnabled(_ enabled: Bool) throws {
        try AirshipProxy.shared.push.setAutobadgeEnabled(enabled)
    }

    @objc
    func pushIsAutobadgeEnabled() throws -> NSNumber {
        return try NSNumber(
            value: AirshipProxy.shared.push.isAutobadgeEnabled()
        )
    }

    @objc
    @MainActor
    func pushSetBadgeNumber(_ badgeNumber: Double) throws {
        try AirshipProxy.shared.push.setBadgeNumber(Int(badgeNumber))
    }

    @objc
    @MainActor
    func pushGetBadgeNumber() throws -> NSNumber {
        return try NSNumber(
            value: AirshipProxy.shared.push.getBadgeNumber()
        )
    }

    @objc
    func pushGetAuthorizedNotificationStatus() throws -> [String : Any] {
        return try AirshipProxy.shared.push.getNotificationStatus()
    }

    @objc
    func pushClearNotifications() {
        AirshipProxy.shared.push.clearNotifications()
    }

    @objc
    func pushClearNotification(_ identifier: String) {
        AirshipProxy.shared.push.clearNotification(identifier)
    }

    @objc
    func pushGetActiveNotifications() async -> [[String: Any]] {
        return await AirshipProxy.shared.push.getActiveNotifications()
    }
}

// Actions
@objc
public extension AirshipCordova {
    func actionsRun(actionName: String, actionValue: Any?) async throws-> Any? {
        return try await AirshipProxy.shared.action.runAction(
            actionName,
            actionValue: try AirshipJSON.wrap(actionValue)
        )
    }
}

// Analytics
@objc
public extension AirshipCordova {

    func analyticsTrackScreen(_ screen: String?) throws {
        try AirshipProxy.shared.analytics.trackScreen(screen)
    }

    func analyticsAssociateIdentifier(_ identifier: String?, key: String) throws {
        try AirshipProxy.shared.analytics.associateIdentifier(
            identifier: identifier,
            key: key
        )
    }
}

// Contact
@objc
public extension AirshipCordova {

    @objc
    func contactIdentify(_ namedUser: String?) throws {
        try AirshipProxy.shared.contact.identify(namedUser ?? "")
    }

    @objc
    func contactReset() throws {
        try AirshipProxy.shared.contact.reset()
    }

    @objc
    func contactGetNamedUserIdOrEmtpy() async throws -> String {
        return try AirshipProxy.shared.contact.getNamedUser() ?? ""
    }

    @objc
    func contactGetSubscriptionLists() async throws -> [String: [String]] {
        return try await AirshipProxy.shared.contact.getSubscriptionLists()
    }

    @objc
    func contactEditTagGroups(json: Any) throws {
        try AirshipProxy.shared.contact.editTagGroups(json: json)
    }

    @objc
    func contactEditAttributes(json: Any) throws {
        try AirshipProxy.shared.contact.editAttributes(json: json)
    }

    @objc
    func contactEditSubscriptionLists(json: Any) throws {
        try AirshipProxy.shared.contact.editSubscriptionLists(json: json)
    }
}

// InApp
@objc
public extension AirshipCordova {

    func inAppIsPaused() throws -> NSNumber {
        return try NSNumber(
            value: AirshipProxy.shared.inApp.isPaused()
        )
    }

    func inAppSetPaused(_ paused: Bool) throws {
        try AirshipProxy.shared.inApp.setPaused(paused)
    }

    func inAppSetDisplayInterval(milliseconds: Double) throws {
        try AirshipProxy.shared.inApp.setDisplayInterval(Int(milliseconds))
    }

    func inAppGetDisplayInterval() throws -> NSNumber {
        return try NSNumber(
            value: AirshipProxy.shared.inApp.getDisplayInterval()
        )
    }
}

// Locale
@objc
public extension AirshipCordova {
    @objc
    func localeSetLocaleOverride(localeIdentifier: String?) throws {
        try AirshipProxy.shared.locale.setCurrentLocale(localeIdentifier)
    }

    @objc
    func localeClearLocaleOverride() throws {
        try AirshipProxy.shared.locale.clearLocale()
    }

    @objc
    func localeGetLocale() throws -> String {
        return try AirshipProxy.shared.locale.getCurrentLocale()
    }
}

// Message Center
@objc
public extension AirshipCordova {
    @objc
    func messageCenterGetUnreadCount() async throws -> Double {
        return try await Double(AirshipProxy.shared.messageCenter.getUnreadCount())
    }

    @objc
    func messageCenterGetMessages() async throws -> [Any] {
        return try AirshipProxy.shared.messageCenter.getMessagesJSON()
    }

    @objc
    func messageCenterMarkMessageRead(messageId: String) async throws  {
        try await AirshipProxy.shared.messageCenter.markMessageRead(
            messageID: messageId
        )
    }

    @objc
    func messageCenterDeleteMessage(messageId: String) async throws  {
        try await AirshipProxy.shared.messageCenter.deleteMessage(
            messageID: messageId
        )
    }

    @objc
    func messageCenterDismiss() throws  {
        return try AirshipProxy.shared.messageCenter.dismiss()
    }

    @objc
    func messageCenterDisplay(messageId: String?) throws  {
        try AirshipProxy.shared.messageCenter.display(messageID: messageId)
    }

    @objc
    func messageCenterRefresh() async throws  {
        try await AirshipProxy.shared.messageCenter.refresh()
    }

    @objc
    func messageCenterSetAutoLaunchDefaultMessageCenter(autoLaunch: Bool) {
        AirshipProxy.shared.messageCenter.setAutoLaunchDefaultMessageCenter(autoLaunch)
    }
}

// Preference Center
@objc
public extension AirshipCordova {
    @objc
    func preferenceCenterDisplay(preferenceCenterId: String) throws  {
        try AirshipProxy.shared.preferenceCenter.displayPreferenceCenter(
            preferenceCenterID: preferenceCenterId
        )
    }

    @objc
    func preferenceCenterGetConfig(preferenceCenterId: String) async throws -> Any {
        return try await AirshipProxy.shared.preferenceCenter.getPreferenceCenterConfig(
            preferenceCenterID: preferenceCenterId
        )
    }

    @objc
    func preferenceCenterAutoLaunchDefaultPreferenceCenter(
        preferenceCenterId: String,
        autoLaunch: Bool
    ) {
        AirshipProxy.shared.preferenceCenter.setAutoLaunchPreferenceCenter(
            autoLaunch,
            preferenceCenterID: preferenceCenterId
        )
    }
}

// Privacy Manager
@objc
public extension AirshipCordova {
    @objc
    func privacyManagerSetEnabledFeatures(features: [String]) throws  {
        try AirshipProxy.shared.privacyManager.setEnabled(featureNames: features)
    }

    @objc
    func privacyManagerGetEnabledFeatures() throws -> [String] {
        return try AirshipProxy.shared.privacyManager.getEnabledNames()
    }

    @objc
    func privacyManagerEnableFeature(features: [String]) throws  {
        try AirshipProxy.shared.privacyManager.enable(featureNames: features)
    }

    @objc
    func privacyManagerDisableFeature(features: [String]) throws  {
        try AirshipProxy.shared.privacyManager.disable(featureNames: features)
    }

    @objc
    func privacyManagerIsFeatureEnabled(features: [String]) throws -> NSNumber  {
        return try NSNumber(
            value: AirshipProxy.shared.privacyManager.isEnabled(featuresNames: features)
        )
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

/*
 extension AirshipProxyEventType {
 private static let nameMap: [String: AirshipProxyEventType] = [
 "com.airship.deep_link": .deepLinkReceived,
 "com.airship.channel_created": .channelCreated,
 "com.airship.push_token_received": .pushTokenReceived,
 "com.airship.message_center_updated": .messageCenterUpdated,
 "com.airship.display_message_center": .displayMessageCenter,
 "com.airship.display_preference_center": .displayPreferenceCenter,
 "com.airship.notification_response": .notificationResponseReceived,
 "com.airship.push_received": .pushReceived,
 "com.airship.notification_status_changed": .notificationStatusChanged,
 "com.airship.authorized_notification_settings_changed": .authorizedNotificationSettingsChanged
 ]
 
 public static func fromReactName(_ name: String) throws -> AirshipProxyEventType {
 guard let type = AirshipProxyEventType.nameMap[name] else {
 throw AirshipErrors.error("Invalid type: \(self)")
 }
 
 return type
 }
 }
 */

extension AirshipCordova: AirshipProxyDelegate {
    public func migrateData(store: ProxyStore) {
        //ProxyDataMigrator().migrateData(store: store)
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
