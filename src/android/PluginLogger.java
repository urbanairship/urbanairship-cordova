/* Copyright Urban Airship and Contributors */

package com.urbanairship.cordova;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.util.Log;

import com.urbanairship.util.UAStringUtil;

import java.util.Locale;

/**
 * Cordova logger for Urban Airship.
 */
public final class PluginLogger {

    @NonNull
    private static final String TAG = "UALib-Cordova";

    /**
     * The current log level, as defined by <code>android.util.Log</code>.
     * Defaults to <code>android.util.Log.ERROR</code>.
     */
    private static int logLevel = Log.INFO;

    /**
     * Private, unused constructor
     */
    private PluginLogger() {
    }

    /**
     * Sets the log level.
     *
     * @param logLevel The log level.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void setLogLevel(int logLevel) {
        PluginLogger.logLevel = logLevel;
    }

    /**
     * Send a warning log message.
     *
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void warn(@NonNull String message, @Nullable Object... args) {
        log(Log.WARN, null, message, args);
    }

    /**
     * Send a warning log message.
     *
     * @param t An exception to log
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void warn(@NonNull Throwable t, @NonNull String message, @Nullable Object... args) {
        log(Log.WARN, t, message, args);
    }

    /**
     * Send a warning log message.
     *
     * @param t An exception to log
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void warn(@NonNull Throwable t) {
        log(Log.WARN, t, null);
    }

    /**
     * Send a verbose log message.
     *
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void verbose(@NonNull String message, @Nullable Object... args) {
        log(Log.VERBOSE, null, message, args);
    }

    /**
     * Send a debug log message.
     *
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void debug(@NonNull String message, @Nullable Object... args) {
        log(Log.DEBUG, null, message, args);
    }

    /**
     * Send a debug log message.
     *
     * @param t An exception to log
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void debug(@NonNull Throwable t, @NonNull String message, @Nullable Object... args) {
        log(Log.DEBUG, t, message, args);
    }

    /**
     * Send an info log message.
     *
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void info(@NonNull String message, @NonNull Object... args) {
        log(Log.INFO, null, message, args);
    }

    /**
     * Send an info log message.
     *
     * @param t An exception to log
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void info(@NonNull Throwable t, @NonNull String message, @Nullable Object... args) {
        log(Log.INFO, t, message, args);
    }

    /**
     * Send an error log message.
     *
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void error(@NonNull String message, @Nullable Object... args) {
        log(Log.ERROR, null, message, args);
    }

    /**
     * Send an error log message.
     *
     * @param t An exception to log
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void error(@NonNull Throwable t) {
        log(Log.ERROR, t, null);
    }

    /**
     * Send an error log message.
     *
     * @param t An exception to log
     * @param message The message you would like logged.
     * @param args The message args.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void error(@NonNull Throwable t, @NonNull String message, @Nullable Object... args) {
        log(Log.ERROR, t, message, args);
    }

    /**
     * Helper method that performs the logging.
     *
     * @param priority The log priority level.
     * @param throwable The optional exception.
     * @param message The optional message.
     * @param args The optional message args.
     */
    private static void log(int priority, @Nullable Throwable throwable, @Nullable String message, @Nullable Object... args) {
        if (logLevel > priority) {
            return;
        }

        if (message == null && throwable == null) {
            return;
        }

        String formattedMessage;

        if (UAStringUtil.isEmpty(message)) {
            // Default to empty string
            formattedMessage = "";
        } else {
            // Format the message if we have arguments
            try {
                formattedMessage = (args == null || args.length == 0) ? message : String.format(Locale.ROOT, message, args);
            } catch (Exception e) {
                Log.wtf(TAG, "Failed to format log.", e);
                return;
            }
        }

        // Log directly if we do not have a throwable
        if (throwable == null) {
            if (priority == Log.ASSERT) {
                Log.wtf(TAG, formattedMessage);
            } else {
                Log.println(priority, TAG, formattedMessage);
            }
            return;
        }

        // Log using one of the provided log methods
        switch (priority) {
            case Log.INFO:
                Log.i(TAG, formattedMessage, throwable);
                break;
            case Log.DEBUG:
                Log.d(TAG, formattedMessage, throwable);
                break;
            case Log.VERBOSE:
                Log.v(TAG, formattedMessage, throwable);
                break;
            case Log.WARN:
                Log.w(TAG, formattedMessage, throwable);
                break;
            case Log.ERROR:
                Log.e(TAG, formattedMessage, throwable);
                break;
            case Log.ASSERT:
                Log.wtf(TAG, formattedMessage, throwable);
                break;
        }
    }

}