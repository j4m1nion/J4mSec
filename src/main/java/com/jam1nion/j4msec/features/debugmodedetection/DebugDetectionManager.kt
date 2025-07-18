package com.jam1nion.j4msec.features.debugmodedetection

interface DebugDetectionManager {

    fun debugDetection(onDebuggerDetected: () -> Unit, onDebuggerNotDetected: () -> Unit)
    fun isDebuggerRunning() : Boolean
}