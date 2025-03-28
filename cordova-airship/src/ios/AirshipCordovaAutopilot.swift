/* Copyright Airship and Contributors */

import AirshipKit
import AirshipFrameworkProxy

@objc(AirshipPluginLoader)
@MainActor
public class AirshipPluginLoader: NSObject, AirshipPluginLoaderProtocol {
    @objc
    public static var disabled: Bool = false

    public static func onApplicationDidFinishLaunching(
        launchOptions: [UIApplication.LaunchOptionsKey : Any]?
    ) {
        AirshipCordovaAutopilot.shared.onLoad(launchOptions: launchOptions)
    }
}

@MainActor
final class AirshipCordovaAutopilot {

    public static let shared: AirshipCordovaAutopilot = AirshipCordovaAutopilot()
    private var launchOptions: [UIApplication.LaunchOptionsKey : Any]?
    private var settings: AirshipCordovaPluginSettings?
    private var pluginInitialized: Bool = false
    private var onLoad: Bool = false

    func pluginInitialized(settings: AirshipCordovaPluginSettings?) {
        self.pluginInitialized = true
        self.settings = settings
        AirshipProxy.shared.delegate = self

        if pluginInitialized, onLoad {
            try? AirshipProxy.shared.attemptTakeOff(
                launchOptions: self.launchOptions
            )
        }
    }

    func onLoad(launchOptions: [UIApplication.LaunchOptionsKey : Any]?) {
        self.onLoad = true
        self.launchOptions = launchOptions
        if pluginInitialized, onLoad {
            try? AirshipProxy.shared.attemptTakeOff(
                launchOptions: self.launchOptions
            )
        }
    }

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
        var airshipConfig = (try? AirshipConfig.default()) ?? AirshipConfig()
        if let settings {
            airshipConfig.applyPluginSettings(settings)
        }
        return airshipConfig
    }
    
    @MainActor
    public func onAirshipReady() {
        Airship.analytics.registerSDKExtension(
            AirshipSDKExtension.cordova,
            version: AirshipCordovaVersion.version
        )

        if settings?.clearBadgeOnLaunch == true {
            Task {
                try? await Airship.push.resetBadge()
            }
        }

        if settings?.enablePushOnLaunch == true {
            Airship.push.userPushNotificationsEnabled = true
        }
    }
}


