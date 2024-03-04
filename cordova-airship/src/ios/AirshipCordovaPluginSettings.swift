/* Copyright Airship and Contributors */

import Foundation
import AirshipKit
import AirshipFrameworkProxy

enum AirshipCordovaSite: String, Decodable, Sendable {
    case us = "US"
    case eu = "EU"
    
    var airshipValue: CloudSite {
        switch(self) {
        case .us:
            return .us
        case .eu:
            return .eu
        }
    }

    var proxyValue: ProxyConfig.Site {
        switch(self) {
        case .us:
            return .us
        case .eu:
            return .eu
        }
    }
}

struct AirshipCordovaPluginSettings: Decodable, Sendable {

    enum LogLevel: String, Decodable, Sendable {
        case none
        case error
        case warn
        case info
        case debug
        case verbose
    }

    var productionAppKey: String?
    var productionAppSecret: String?
    var productionLogLevel: LogLevel?

    var developmentAppKey: String?
    var developmentAppSecret: String?
    var developmentLogLevel: LogLevel?

    var inProduction: Bool?
    var site: AirshipCordovaSite?

    var clearBadgeOnLaunch: Bool?
    var enablePushOnLaunch: Bool?
    var analyticsEnabled: Bool?

    var initialConfigURL: String?
    var presentationOptions: UNNotificationPresentationOptions
    var autoLaunchMessageCenter: Bool?
    var messageCenterStyleConfig: String?

    enum CodingKeys: String, CodingKey, CaseIterable {
        case productionAppKey = "com.urbanairship.production_app_key"
        case productionAppSecret = "com.urbanairship.production_app_secret"
        case productionLogLevel = "com.urbanairship.production_log_level"
        case developmentAppKey = "com.urbanairship.development_app_key"
        case developmentAppSecret = "com.urbanairship.development_app_secret"
        case developmentLogLevel = "com.urbanairship.development_log_level"
        case inProduction = "com.urbanairship.in_production"
        case site = "com.urbanairship.site"
        case clearBadgeOnLaunch = "com.urbanairship.clear_badge_onlaunch"
        case enablePushOnLaunch = "com.urbanairship.enable_push_onlaunch"
        case initialConfigURL = "com.urbanairship.initial_config_url"
        case analyticsEnabled = "com.urbanairship.enable_analytic"
        case alertPresentationOption = "com.urbanairship.ios_foreground_notification_presentation_alert"
        case badgePresentationOption = "com.urbanairship.ios_foreground_notification_presentation_badge"
        case soundPresentationOption = "com.urbanairship.ios_foreground_notification_presentation_sound"
        case autoLaunchMessageCenter = "com.urbanairship.auto_launch_message_center"
        case messageCenterStyleConfig = "com.urbanairship.message.center.style.file"
    }

    static func from(settings: [AnyHashable: Any]?) -> AirshipCordovaPluginSettings? {
        guard let settings = settings else { return nil }
        let knownKeys = CodingKeys.allCases.map { $0.rawValue }
        let filtered = settings.filter { key, value in
            guard let key = key as? String else { return false }
            guard (value as? String) != nil else { return false }
            return knownKeys.contains(key)
        }

        do {
            return try AirshipJSON.wrap(filtered).decode()
        } catch {
            AirshipLogger.error("Failed to parse cordova settings \(filtered) error \(error)")
            return nil
        }
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.productionAppKey = try container.decodeIfPresent(String.self, forKey: .productionAppKey)
        self.productionAppSecret = try container.decodeIfPresent(String.self, forKey: .productionAppSecret)
        self.productionLogLevel = try container.decodeIfPresent(AirshipCordovaPluginSettings.LogLevel.self, forKey: .productionLogLevel)
        self.developmentAppKey = try container.decodeIfPresent(String.self, forKey: .developmentAppKey)
        self.developmentAppSecret = try container.decodeIfPresent(String.self, forKey: .developmentAppSecret)
        self.developmentLogLevel = try container.decodeIfPresent(AirshipCordovaPluginSettings.LogLevel.self, forKey: .developmentLogLevel)
        self.inProduction = try container.decodeIfPresent(String.self, forKey: .inProduction)?.asBool
        self.site = try container.decodeIfPresent(AirshipCordovaSite.self, forKey: .site)
        self.clearBadgeOnLaunch = try container.decodeIfPresent(String.self, forKey: .clearBadgeOnLaunch)?.asBool
        self.enablePushOnLaunch = try container.decodeIfPresent(String.self, forKey: .enablePushOnLaunch)?.asBool
        self.initialConfigURL = try container.decodeIfPresent(String.self, forKey: .initialConfigURL)
        self.analyticsEnabled = try container.decodeIfPresent(String.self, forKey: .analyticsEnabled)?.asBool
        self.autoLaunchMessageCenter = try container.decodeIfPresent(String.self, forKey: .autoLaunchMessageCenter)?.asBool
        self.messageCenterStyleConfig = try container.decodeIfPresent(String.self, forKey: .messageCenterStyleConfig)


        let alert = try container.decodeIfPresent(String.self, forKey: .alertPresentationOption)?.asBool
        let badge = try container.decodeIfPresent(String.self, forKey: .badgePresentationOption)?.asBool
        let sound = try container.decodeIfPresent(String.self, forKey: .soundPresentationOption)?.asBool

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

        self.presentationOptions = presentationOptions
    }

    func apply(config: AirshipConfig) {
        if let appSecret = self.developmentAppSecret {
            config.developmentAppSecret = appSecret
        }

        if let appKey = self.developmentAppKey {
            config.developmentAppKey = appKey
        }

        if let logLevel = self.developmentLogLevel {
            config.developmentLogLevel = logLevel.airshipValue
        }

        if let appSecret = self.productionAppSecret {
            config.productionAppSecret = appSecret
        }

        if let appKey = self.productionAppKey {
            config.productionAppKey = appKey
        }

        if let logLevel = self.productionLogLevel {
            config.productionLogLevel = logLevel.airshipValue
        }

        if let site = self.site {
            config.site = site.airshipValue
        }

        if let inProduction = self.inProduction {
            config.inProduction = inProduction
        }

        if let initialConfigURL = self.initialConfigURL {
            config.initialConfigURL = initialConfigURL
        }

        if let analyticsEnabled = self.analyticsEnabled {
            config.isAnalyticsEnabled = analyticsEnabled
        }

        if let messageCenterStyleConfig = self.messageCenterStyleConfig {
            config.messageCenterStyleConfig = messageCenterStyleConfig
        }
    }
}

extension String {
    var asBool: Bool {
        get throws {
            guard let bool = Bool(self.lowercased()) else {
                throw AirshipErrors.error("Failed to parse bool \(self)")
            }
            return bool
        }
    }
}


extension AirshipCordovaPluginSettings.LogLevel {
    var airshipValue: AirshipLogLevel {
        switch(self) {
        case .none:
            return .none
        case .error:
            return .error
        case .warn:
            return .warn
        case .info:
            return .info
        case .debug:
            return .debug
        case .verbose:
            return .verbose
        }
    }

    var proxyValue: ProxyConfig.LogLevel {
        switch(self) {
        case .none:
            return .none
        case .error:
            return .error
        case .warn:
            return .warning
        case .info:
            return .info
        case .debug:
            return .debug
        case .verbose:
            return .verbose
        }
    }
}
