package com.jam1nion.j4msec.features.virtualdevicedetection

import android.content.Context

interface VirtualDeviceDetectionManager {
    fun deviceAnalysis(context: Context, onVirtual: () -> Unit, onPhysical: () -> Unit)
    fun isRunningOnVirtualDevice(context: Context): Boolean
}