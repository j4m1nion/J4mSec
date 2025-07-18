package com.jam1nion.j4msec.features.debugmodedetection

import android.os.Debug
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel

internal class DebugDetectionManagerImpl : DebugDetectionManager {

    companion object{
        private const val TAG = "DebugDetectionManager"
    }


    private external fun p() : Boolean


    override fun debugDetection(onDebuggerDetected: () -> Unit, onDebuggerNotDetected: () -> Unit){
        val debuggerDetected = p() || Debug.isDebuggerConnected()
        if(debuggerDetected){
            onDebuggerDetected.invoke()

        }
        else{
            onDebuggerNotDetected.invoke()
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(debuggerDetected) "debugger detected" else "debugger not detected",
                if(debuggerDetected) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }
    }

    override fun isDebuggerRunning() : Boolean {
        val debugger = p() || Debug.isDebuggerConnected()
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(debugger) "debugger detected" else "debugger not detected",
                if(debugger) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }
        return debugger
    }


}