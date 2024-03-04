/* Copyright Airship and Contributors */

import AirshipKit
import AirshipFrameworkProxy

final class AirshipCordovaAutopilot {

    public static let shared: AirshipCordovaAutopilot = AirshipCordovaAutopilot()
    private var launchOptions: [UIApplication.LaunchOptionsKey : Any]?
    private var settings: AirshipCordovaPluginSettings?

    @MainActor
    func pluginInitialized(settings: AirshipCordovaPluginSettings?) {
        self.settings = settings
        AirshipProxy.shared.delegate = self
        AirshipCordovaBootstrap.onLaunch = { launchOptions in
            self.launchOptions = launchOptions as? [UIApplication.LaunchOptionsKey : Any]
            try? AirshipProxy.shared.attemptTakeOff(launchOptions: self.launchOptions)
        }
    }

    @MainActor
    func attemptTakeOff(json: Any) throws -> Bool {
        return try AirshipProxy.shared.takeOff(
            json: json,
            launchOptions: self.launchOptions
        )
    }
    
}

extension AirshipCordovaAutopilot: AirshipProxyDelegate {
    public func migrateData(store: AirshipFrameworkProxy.ProxyStore) {
        AirshipCordovaProxyDataMigrator().migrateData(store: store)
        store.defaultPresentationOptions = settings?.presentationOptions ?? []
        store.defaultAutoDisplayMessageCenter = settings?.autoLaunchMessageCenter ?? true
    }
    
    public func loadDefaultConfig() -> AirshipConfig {
        let config = AirshipConfig.default()
        settings?.apply(config: config)
        return config
    }
    
    @MainActor
    public func onAirshipReady() {
        Airship.analytics.registerSDKExtension(
            AirshipSDKExtension.cordova,
            version: AirshipCordovaVersion.version
        )

        if settings?.clearBadgeOnLaunch == true {
            Airship.push.resetBadge()
        }

        if settings?.enablePushOnLaunch == true {
            Airship.push.userPushNotificationsEnabled = true
        }
    }
}


