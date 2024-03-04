/* Copyright Airship and Contributors */

import Foundation
import AirshipFrameworkProxy
import UserNotifications
import AirshipKit

struct AirshipCordovaProxyDataMigrator {

    private let defaults = UserDefaults.standard

    private static let productionAppKey = "com.urbanairship.production_app_key"
    private static let productionAppSecret = "com.urbanairship.production_app_secret"
    private static let site = "com.urbanairship.site"
    private static let developmentAppKey = "com.urbanairship.development_app_key"
    private static let developmentAppSecret = "com.urbanairship.development_app_secret"

    private static let messageCenterStyleConfigKey = "com.urbanairship.message.center.style.file"
    private static let autoLaunchMessageCenterKey = "com.urbanairship.auto_launch_message_center"

    private static let autoLaunchPreferenceCenterPrefix = "com.urbanairship.preference_"
    private static let autoLaunchPreferenceCenterSuffix = "_custom_ui"

    private static let notificationPresentationAlertKey = "com.urbanairship.ios_foreground_notification_presentation_alert"
    private static let notificationPresentationBadgeKey = "com.urbanairship.ios_foreground_notification_presentation_badge"
    private static let notificationPresentationSoundKey = "com.urbanairship.ios_foreground_notification_presentation_sound"


    func migrateData(store: ProxyStore) {
        migrateConfig(store: store)
        migrateAutoLaunchMessageCenter(store: store)
        migrateAutoLaunchPreferenceCenter(store: store)
        migratePresentationOptions(store: store)
    }

    private func migrateConfig(store: ProxyStore) {

        let productionAppKey = defaults.string(forKey: Self.productionAppKey)
        let productionAppSecret = defaults.string(forKey: Self.productionAppSecret)
        let developmentAppKey = defaults.string(forKey: Self.developmentAppKey)
        let developmentAppSecret = defaults.string(forKey: Self.developmentAppSecret)
        let messageCenterStyleConfig = defaults.string(forKey: Self.messageCenterStyleConfigKey)

        let site: ProxyConfig.Site? = if let string = defaults.string(forKey: Self.site) {
            AirshipCordovaSite(rawValue: string)?.proxyValue
        } else {
            nil
        }

        var production: ProxyConfig.Environment?
        if let productionAppKey, let productionAppSecret {
            production = ProxyConfig.Environment(
                logLevel: nil,
                appKey: productionAppKey,
                appSecret: productionAppSecret
            )
        }

        var development: ProxyConfig.Environment?
        if let developmentAppKey, let developmentAppSecret {
            development = ProxyConfig.Environment(
                logLevel: nil,
                appKey: developmentAppKey,
                appSecret: developmentAppSecret
            )
        }

        if (production != nil || development != nil ) {
            store.config = ProxyConfig(
                productionEnvironment: production,
                developmentEnvironment: development,
                ios: ProxyConfig.PlatformConfig(
                    messageCenterStyleConfig: messageCenterStyleConfig
                ),
                site: site
            )
        }

        [
            Self.messageCenterStyleConfigKey,
            Self.productionAppKey,
            Self.productionAppSecret,
            Self.developmentAppKey,
            Self.developmentAppSecret,
            Self.site,
        ].forEach {
            defaults.removeObject(forKey: $0)
        }
    }

    private func migrateAutoLaunchMessageCenter(store: ProxyStore) {
        guard
            let autoLaunchMessageCenter = defaults.object(
                forKey: Self.autoLaunchMessageCenterKey
            ) as? Bool
        else {
            return
        }

        store.autoDisplayMessageCenter = autoLaunchMessageCenter
        defaults.removeObject(forKey: Self.autoLaunchMessageCenterKey)
    }

    private func migrateAutoLaunchPreferenceCenter(store: ProxyStore) {
        // Preference center
        defaults.dictionaryRepresentation().keys.forEach { key in
            if key.hasPrefix(Self.autoLaunchPreferenceCenterPrefix),
               key.hasSuffix(Self.autoLaunchPreferenceCenterSuffix)
            {
                var preferenceCenterID = String(
                    key.dropFirst("Self.autoLaunchPreferenceCenterPrefix".count)
                )

                preferenceCenterID = String(
                    preferenceCenterID.dropLast(Self.autoLaunchPreferenceCenterSuffix.count)
                )

                store.setAutoLaunchPreferenceCenter(
                    preferenceCenterID,
                    autoLaunch: defaults.bool(forKey: key)
                )

                defaults.removeObject(
                    forKey: key
                )
            }
        }
    }

    private func migratePresentationOptions(store: ProxyStore) {
        let alert = defaults.object(forKey: Self.notificationPresentationAlertKey) as? Bool
        let badge = defaults.object(forKey: Self.notificationPresentationBadgeKey) as? Bool
        let sound = defaults.object(forKey: Self.notificationPresentationSoundKey) as? Bool

        guard alert != nil || badge != nil || sound != nil else { return }

        var presentationOptions: UNNotificationPresentationOptions = []

        if alert == true {
            presentationOptions.insert(.list)
            presentationOptions.insert(.banner)
        }

        if badge == true {
            presentationOptions.insert(.badge)
        }

        if sound == true {
            presentationOptions.insert(.sound)
        }

        store.foregroundPresentationOptions = presentationOptions

        [
            Self.notificationPresentationAlertKey,
            Self.notificationPresentationBadgeKey,
            Self.notificationPresentationSoundKey
        ].forEach {
            defaults.removeObject(forKey: $0)
        }
    }
}
