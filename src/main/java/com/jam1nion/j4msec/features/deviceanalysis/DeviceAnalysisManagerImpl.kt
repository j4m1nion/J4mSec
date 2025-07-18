package com.jam1nion.j4msec.features.deviceanalysis

import android.content.Context
import android.os.Build
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.deviceanalysis.models.DeviceAnalysisResult
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel

internal object DeviceAnalysisManager {
    private const val TAG = "DeviceAnalysisManager"
     fun analyzeDeviceAsString(context: Context): String {
        val builder = StringBuilder().apply {
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Type: ${if (J4mSec.virtualDeviceDetectionManager?.isRunningOnVirtualDevice(context) == true) "EMULATOR" else "PHYSICAL"}")
            appendLine("Debugger: ${if (J4mSec.debugDetectionManager?.isDebuggerRunning() == true) "ACTIVE" else "INACTIVE"}")
            appendLine("Hooker: ${if (J4mSec.hookerDetectionManager?.isHookDetected() == true) "DETECTED" else "UNDETECTED"}")
        }

        val result = builder.toString()

        if (J4mSec.configuration.enableLogging) {
            J4mSec.secureLogManager?.logAsync(TAG, result, LoggingLevel.SECURITY)
        }

        return result
    }

    fun analyzeDevice(context: Context): DeviceAnalysisResult {
        return DeviceAnalysisResult(
            deviceManufacturer = Build.MANUFACTURER,
            deviceModel = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            isEmulator = J4mSec.virtualDeviceDetectionManager?.isRunningOnVirtualDevice(context) == true,
            isDebuggerAttached = J4mSec.debugDetectionManager?.isDebuggerRunning() == true,
            isHookDetected = J4mSec.hookerDetectionManager?.isHookDetected() == true
        )
    }
}