package com.jam1nion.j4msec.features.virtualdevicedetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import com.jam1nion.j4msec.features.utils.getProp
import java.io.File
import java.net.NetworkInterface

internal class VirtualDeviceDetectionManagerImpl : VirtualDeviceDetectionManager {

    companion object{
        private const val TAG = "VirtualDeviceDetectionManager"

    }

    enum class BuildInfo {
        FINGERPRINT, MODEL, MANUFACTURER, BRAND, DEVICE, PRODUCT, HARDWARE, BOARD
    }

    private val virtualDeviceBuildInfo = mapOf(
        BuildInfo.FINGERPRINT to listOf("generic", "unknown", "emulator", "sdk", "goldfish", "ranchu").map { it.lowercase() },
        BuildInfo.MODEL to listOf("emulator", "android sdk built for x86").map { it.lowercase() },
        BuildInfo.MANUFACTURER to listOf("genymotion", "unknown").map { it.lowercase() },
        BuildInfo.BRAND to listOf("generic", "generic_x86").map { it.lowercase() },
        BuildInfo.DEVICE to listOf("generic", "generic_x86").map { it.lowercase() },
        BuildInfo.BOARD to listOf("goldfish", "ranchu", "unknown", "android_x86").map { it.lowercase() },
        BuildInfo.HARDWARE to listOf("goldfish", "ranchu", "vbox86", "android_x86", "bst", "nox", "ttVM_x86").map { it.lowercase() },
        BuildInfo.PRODUCT to listOf("sdk", "google_sdk", "sdk_x86", "sdk_gphone_x86", "sdk_google_phone_x86", "vbox86p", "vbox86tp", "bluestacks", "bluestacks_x86", "nox").map { it.lowercase() }

    )

   private fun deviceAnalysisOnBuildProperties(): Boolean {
        return virtualDeviceBuildInfo.any { (key, keywords) ->
            val value = when (key) {
                BuildInfo.FINGERPRINT -> Build.FINGERPRINT
                BuildInfo.MODEL -> Build.MODEL
                BuildInfo.MANUFACTURER -> Build.MANUFACTURER
                BuildInfo.BRAND -> Build.BRAND
                BuildInfo.DEVICE -> Build.DEVICE
                BuildInfo.BOARD -> Build.BOARD
                BuildInfo.HARDWARE -> Build.HARDWARE
                BuildInfo.PRODUCT -> Build.PRODUCT
            }.lowercase()

            keywords.any { keyword -> value.contains(keyword) }
        }
    }

    private fun checkQemuPipesCheck() : Boolean {
        val knowPipes = listOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe"
        )

        return knowPipes.any { path ->
            try {
                File(path).exists()
            }
            catch (ex : Exception){
                false
            }
        }
    }

    private fun checkEmulatorFiles() : Boolean{
        val susFiles = listOf(
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd"
        )

        return susFiles.any { path ->
            try {
                File(path).exists()
            }
            catch (ex: Exception){
                false
            }
        }
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun checkTelephonyInfo(context: Context) : Boolean{
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return false
        val serial = try {
            Build.getSerial()
        }
        catch (ex : Exception){
            ""
        }

        val susOperator = telephonyManager.networkOperatorName?.lowercase()?.contains("android") == true
        val noImei = telephonyManager.imei?.isBlank() == true || telephonyManager.imei?.all { it == '0' } == true
        val noSerial = serial == null || serial.isBlank() || serial.all { it == '0' }

        return susOperator || noImei || noSerial
    }


    private fun checkQemuCpuInfo() : Boolean{
        return try {
            File("/proc/cpuinfo").useLines { lines ->
                lines.any { it.contains("goldfish", ignoreCase = true) || it.contains("qemu", ignoreCase = true) }
            }
        } catch (ex : Exception){
            false
        }
    }

    private fun checkQemuSystemProps() : Boolean{
        val susProps = listOf("ro.kernel.qemu", "ro.hardware", "ro.boot.hardware")
        return susProps.any { prop ->
            prop.getProp()?.lowercase()?.let {
                it.contains("goldfish") || it.contains("ranchu") || it.contains("qemu") || it.contains("vbox")
            } == true
        }
    }

    private fun checkEmulatorIp() : Boolean{
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            interfaces.toList().flatMap { it.inetAddresses.toList() }.any {
                it.hostAddress == "10.0.2.15" || it.hostAddress?.startsWith("10.0.") == true
            }
        }catch (ex: Exception){
            false
        }
    }

    private fun checkEmulatorCommonPackages(context: Context): Boolean {
        val susPackages = listOf("com.bluestacks", "com.genymotion.superuser")

        return susPackages.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            }
            catch (_: Exception){
                false
            }

        }
    }

    @SuppressLint("HardwareIds")
    private fun checkSecureSetting(context: Context): Boolean {
        val settingSecure = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return settingSecure != null && (settingSecure.isBlank() || settingSecure == "9774d56d682e549c")
    }

    private fun isRunningOnEmulator(context: Context) : Boolean{
        return deviceAnalysisOnBuildProperties() ||
                checkQemuPipesCheck() ||
                checkQemuCpuInfo() ||
                checkTelephonyInfo(context) ||
                checkEmulatorFiles() ||
                checkQemuSystemProps() ||
                checkEmulatorIp() ||
                checkEmulatorCommonPackages(context) ||
                checkSecureSetting(context)
    }



    override fun deviceAnalysis(context: Context, onVirtual: () -> Unit, onPhysical: () -> Unit){
        val isRunningOnEmulator = isRunningOnEmulator(context)
        if(isRunningOnEmulator){
            onVirtual.invoke()
        }
        else
            onPhysical.invoke()


        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(isRunningOnEmulator) "App is running on emulator" else "App is not running on emulator",
                if(isRunningOnEmulator) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }
    }

    override fun isRunningOnVirtualDevice(context: Context): Boolean {
        val isRunningOnEmulator = isRunningOnEmulator(context)
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(isRunningOnEmulator) "App is running on emulator" else "App is not running on emulator",
                if(isRunningOnEmulator) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }

        return isRunningOnEmulator
    }
}