package com.jam1nion.j4msec

import android.content.Context
import com.jam1nion.j4msec.features.appsignatureverification.AppSignatureManager
import com.jam1nion.j4msec.features.appsignatureverification.AppSignatureManagerImpl
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationManager
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationManagerImpl
import com.jam1nion.j4msec.features.certificatepinning.CertificatePinningManager
import com.jam1nion.j4msec.features.certificatepinning.CertificatePinningManagerImpl
import com.jam1nion.j4msec.features.debugmodedetection.DebugDetectionManager
import com.jam1nion.j4msec.features.debugmodedetection.DebugDetectionManagerImpl
import com.jam1nion.j4msec.features.deviceanalysis.DeviceAnalysisManager
import com.jam1nion.j4msec.features.deviceanalysis.models.DeviceAnalysisResult
import com.jam1nion.j4msec.features.hookerdetection.HookerDetectionManager
import com.jam1nion.j4msec.features.hookerdetection.HookerDetectionManagerImpl
import com.jam1nion.j4msec.features.securelogging.SecureLogManager
import com.jam1nion.j4msec.features.securelogging.SecureLogManagerImpl
import com.jam1nion.j4msec.features.securesharedprefs.SecureSharedPrefsManager
import com.jam1nion.j4msec.features.securesharedprefs.SecureSharedPrefsManagerImpl
import com.jam1nion.j4msec.features.securesharedprefs.SecureStrongSharedPrefsManager
import com.jam1nion.j4msec.features.securesharedprefs.SecureStrongSharedPrefsManagerImpl
import com.jam1nion.j4msec.features.tamperdetection.TamperDetectionManager
import com.jam1nion.j4msec.features.tamperdetection.TamperDetectionManagerImpl
import com.jam1nion.j4msec.features.virtualdevicedetection.VirtualDeviceDetectionManager
import com.jam1nion.j4msec.features.virtualdevicedetection.VirtualDeviceDetectionManagerImpl
import com.jam1nion.j4msec.models.J4msecConfiguration


object J4mSec {
    var configuration = J4msecConfiguration()

    var appSignatureManager: AppSignatureManager ? = null
    var biometricAuthenticationManager: BiometricAuthenticationManager ? = null
    var certificatePinningManager: CertificatePinningManager ? = null
    var debugDetectionManager: DebugDetectionManager ? = null
    var hookerDetectionManager: HookerDetectionManager ? = null
    var secureLogManager : SecureLogManager ? = null
    var secureSharedPrefsManager: SecureSharedPrefsManager ? = null
    var secureStrongSharedPrefsManager: SecureStrongSharedPrefsManager ? = null
    var tamperDetectionManager : TamperDetectionManager ? = null
    var virtualDeviceDetectionManager : VirtualDeviceDetectionManager ? = null

    fun init(
        context: Context,
        configuration: J4msecConfiguration = J4msecConfiguration()
    ){
        this.configuration = configuration
        System.loadLibrary("j4msec_lib")
        appSignatureManager = AppSignatureManagerImpl()
        biometricAuthenticationManager = BiometricAuthenticationManagerImpl()
        certificatePinningManager = CertificatePinningManagerImpl()
        debugDetectionManager = DebugDetectionManagerImpl()
        hookerDetectionManager = HookerDetectionManagerImpl()
        secureLogManager = SecureLogManagerImpl().also { it.init(context, configuration.secureLogConfiguration.logCoroutineScope, configuration.secureLogConfiguration.logDirectory, configuration.secureLogConfiguration.logFilename) }
        secureSharedPrefsManager = SecureSharedPrefsManagerImpl().also { it.init(context, configuration.securePrefsConfiguration.preferenceName) }
        secureStrongSharedPrefsManager = SecureStrongSharedPrefsManagerImpl().also { it.init(context, configuration.securePrefsConfiguration.preferenceName, configuration.securePrefsConfiguration.strongAuthTimeoutSec) }
        tamperDetectionManager = TamperDetectionManagerImpl()
        virtualDeviceDetectionManager = VirtualDeviceDetectionManagerImpl()
    }

    fun analyzeDeviceAsString(context: Context) : String = DeviceAnalysisManager.analyzeDeviceAsString(context = context)
    fun analyzeDevice(context: Context) : DeviceAnalysisResult = DeviceAnalysisManager.analyzeDevice(context = context)

}

