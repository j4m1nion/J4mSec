package com.jam1nion.j4msec.features.deviceanalysis.models

data class DeviceAnalysisResult(
    val isEmulator: Boolean,
    val isDebuggerAttached: Boolean,
    val isHookDetected: Boolean,
    val deviceManufacturer: String,
    val deviceModel: String,
    val androidVersion: String,
    val apiLevel: Int
)
