package com.jam1nion.j4msec.features.hookerdetection

interface HookerDetectionManager {
    fun hookDetected(onDetected: () -> Unit, onUndetected: () -> Unit)
    fun isHookDetected() : Boolean
}