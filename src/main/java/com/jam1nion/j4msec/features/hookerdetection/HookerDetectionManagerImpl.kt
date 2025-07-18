package com.jam1nion.j4msec.features.hookerdetection

import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import com.jam1nion.j4msec.features.utils.containsAny
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

internal class HookerDetectionManagerImpl : HookerDetectionManager {

    companion object{
        private const val TAG = "HookerDetectionManager"
    }
    private fun isFridaProcessRunning() : Boolean{
        val susKeywords = listOf("frida", "frida-server", "xposed")
        return try {
            val processes = File("/proc").listFiles()
            processes?.any { it.name.toIntOrNull() != null && File("/proc/${it.name}/cmdline").readText(
                Charsets.UTF_8).trim().containsAny(susKeywords) } == true
        }
        catch (ex: Exception){
            false
        }
    }


    private fun checkXposed() : Boolean{
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        }
        catch (ex: ClassNotFoundException){
            false
        }
    }

   private fun isFridaPortOpen(): Boolean{
        val ports = listOf(27042, 27043)
        return ports.any { port ->
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), 150)
                    true
                }
            }
            catch (ex: Exception){
                false
            }
        }
    }

    override fun hookDetected(onDetected: () -> Unit, onUndetected: () -> Unit){
        if (isHookDetected()){
            onDetected.invoke()
        }
        else{
            onUndetected.invoke()
        }
    }

    override fun isHookDetected() : Boolean{
        val hooking = isFridaProcessRunning() || checkXposed() || isFridaPortOpen()

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(hooking) "Hooking environment detected!" else "No hooking environment detected!",
                if(hooking) LoggingLevel.SECURITY else LoggingLevel.DEBUG
            )
        }

        return hooking
    }


}