/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import com.urbanairship.AirshipConfigOptions;
import android.content.Context;
import android.content.res.XmlResourceParser;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Config Utils.
 */
class ConfigUtils {
  private static final String UA_PREFIX = "com.urbanairship";
  private static final String SENDER_PREFIX = "sender:";

  /**
   * Convert the log level string to an int.
   *
   * @param logLevel        The log level as a string.
   * @param defaultLogLevel Default log level.
   * @return The log level.
   */
  public static int parseLogLevel(@Nullable String logLevel, int defaultLogLevel) {
    if (logLevel == null || logLevel.length() == 0) {
      return defaultLogLevel;
    }
    String logString = logLevel.trim().toLowerCase();
    if (logString.equals("verbose")) {
      return Log.VERBOSE;
    } else if (logString.equals("debug")) {
      return Log.DEBUG;
    } else if (logString.equals("info")) {
      return Log.INFO;
    } else if (logString.equals("warn")) {
      return Log.WARN;
    } else if (logString.equals("error")) {
      return Log.ERROR;
    } else if (logString.equals("none")) {
      return Log.ASSERT;
    }
    return defaultLogLevel;
  }

  /**
   * Parses the sender ID.
   *
   * @param value The value from config.
   * @return The sender ID.
   */
  @Nullable
  public static String parseSender(@Nullable String value) {
    if (value == null) {
      return null;
    }

    if (value.startsWith("sender:")) {
      return value.substring(SENDER_PREFIX.length());
    }

    return value;
  }

  /**
   * Parses a cloud site from a String.
   *
   * @param value The value to parse.
   * @return The parsed site value. Defaults to US if site is null or does not match EU.
   */
  @NonNull
  @AirshipConfigOptions.Site
  public static String parseCloudSite(@Nullable String value) {
    if (AirshipConfigOptions.SITE_EU.equalsIgnoreCase(value)) {
      return AirshipConfigOptions.SITE_EU;
    }
    return AirshipConfigOptions.SITE_US;
  }

  /**
   * Parses the config.xml file for any Urban Airship config.
   *
   * @param context The application context.
   */
  @NonNull
  public static Map<String, String> parseConfigXml(@NonNull Context context) {
    Map<String, String> config = new HashMap<String, String>();
    int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
    if (id == 0) {
      return config;
    }

    XmlResourceParser xml = context.getResources().getXml(id);

    int eventType = -1;
    while (eventType != XmlResourceParser.END_DOCUMENT) {

      if (eventType == XmlResourceParser.START_TAG) {
        if (xml.getName().equals("preference")) {
          String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.US);
          String value = xml.getAttributeValue(null, "value");

          if (name.startsWith(UA_PREFIX) && value != null) {
            config.put(name, value);
            PluginLogger.verbose("Found %s in config.xml with value: %s", name, value);
          }
        }
      }

      try {
        eventType = xml.next();
      } catch (Exception e) {
        PluginLogger.error(e, "Error parsing config file");
      }
    }

    return config;
  }
}
