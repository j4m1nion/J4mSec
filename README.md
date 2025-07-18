# J4MSec 

**J4MSec** is a lightweight Kotlin-based security library for Android. It provides modular runtime security features to help developers detect tampering, validate integrity, and protect user data—without requiring advanced security expertise.

---

## Features

J4MSec currently offers:

- Root detection
- Emulator detection
- Frida and hook detection
- App tampering detection
- App signature verification
- Biometric authentication (wrapper)
- Secure shared preferences (AES and Keystore-backed)
- Secure logging manager
- Certificate pinning (OkHttp support)

All components are modular and can be used independently or together.

---

## Installation

Add J4MSec to your build.gradle.kts

```kotlin
dependencies {
    implementation("com.jam1nion:j4msec:1.0.0")
}
```

## Initialization

```kotlin
J4mSec.init(
            context = context,
            configuration = J4msecConfiguration(
                enableLogging = true,
                secureLogConfiguration = J4msecSecureLogConfiguration(
                    logCoroutineScope = coroutineScope,
                    logDirectory = context.filesDir,
                    logFilename = "log_app"
                ),
                securePrefsConfiguration = J4msecSecurePrefsConfiguration(
                    preferenceName = "app_prefs",
                    strongAuthTimeoutSec = 45
                )
            )
        )
```
**enableLogging**: Enable internal logging
**logCoroutineScope**: Coroutine scope for log writing
**logDirectory**: Directory where logs are stored
**logFilename**: Name of the log file  
**preferenceName**: SharedPreferences file name
**strongAuthTimeoutSec**: Time window in seconds (after successful unlock) to allow strong auth preference access

 ## Features

 ### App Signature Verification

Verify that the app’s certificate matches the expected hash. Supports plain, Base64, and obfuscated formats.

 ```kotlin
interface AppSignatureManager {

    fun appSignature(context: Context, certificateSignature: ByteArray, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit)
    fun appSignature(context: Context, certificateSignature: String, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit)
    fun appSignature(context: Context, certificateSignature: String, xorKey: Char = 0x5A.toChar(), reverse: Boolean = true, salt: String? = null, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit)
    fun appSignature(context: Context, certificateSignature: ByteArray) : Boolean
    fun appSignature(context: Context, certificateSignature: String) : Boolean
    fun appSignature(context: Context, certificateSignature: String, xorKey: Char = 0x5A.toChar(), reverse: Boolean = true, salt: String? = null) : Boolean
}

```

certificateHash can be: 
  - the bytearray of the sha256 key of a file .jks
  - the base64 string of the sha256 key of a file .jks
  - the base64 string of the sha256 key of a file .jks that is xorred, reversed and with a salt attached.

To extract the SHA-256 hash from a .jks file:

```bash

./gradlew extractCertHash -Pkeystore=./release.jks -Palias=myalias -Pstorepass=mypassword -Pkeypass=mypassword

```
**Output**: 

Base64 SHA-256 hash of public key: **_KNpde1W7E1k2Pdh9kdBIQtxVe7GKhEgckq/6UnFVDjc=_**

To obfuscate the hash:

```bash

 ./gradlew encodeCert -PcertHash="KNpde1W7E1k2Pdh9kdBIQtxVe7GKhEgckq/6UnFVDjc=" -PxorKey=Z -Preverse=true -Psalt="com.example.myapp"

```

**Output**: 

Obfuscated cert hash: **bVQPKwig9chGEt7Q6yEPhhgSissngmdsA0nhDyEHgHI=com.example.myapp**

Example: 
```kotlin

J4mSec.appSignatureManager?.appSignature(
                                    context = context,
                                    certificateSignature = "bVQPKwig9chGEt7Q6yEPhhgSissngmdsA0nhDyEHgHI=com.example.myapp",
                                    xorKey = 'Z',
                                    reverse = true,
                                    salt = "com.example.myapp",
                                    onAppSignatureValid = { /* manage app signature valid */ },
                                    onAppSignatureInvalid = { /* manage app signature invalid */ )
```
 ### Biometric Auth

A wrapper for system-provided biometric or device credentials.

```kotlin
interface BiometricAuthenticationManager {

    fun observeLockStatus(owner: LifecycleOwner, handler: (LockStatus) -> Unit)
    fun isBiometricAvailable(context: Context) : Boolean
    fun isDeviceSecured(context: Context) : Boolean
    fun biometricLock(context: Context)
    fun biometricLockBlocking(context: Context)
    fun showPrompt(
        context: Context,
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (String) -> Unit
    )
    fun appLock(context: Context, delay: Long = 3000L)
    fun appUnlock()

}
```
**observeLockStatus**: Observe the change of status of the app between LOCKED(App is showing activity that asks user to login), UNLOCKED(User authenticates itself succesfully)  
**isBiometricAvailable**: Returns a boolean with the availability of the fingerprint login  
**isDeviceSecured**: Return a boolean with the availability of a login on the device  
**biometricLock**: Launch an activity that covers the full screen and showing the system dialog to request the login, the activity is dismissed when the user login or if the user press backbutton.  
**biometricLockBlocking**: Launch an activity that covers the full screen and showing the system dialog to request the login, the activity is dismissed when the user login. If the user try to close it with the back button the activity show the dialog again.   
**showPrompt**: Wrapper for the system dialog that request the login, used for a custom behaviour of the login.   
**appLock**: Starts an observer that observe the app behaviour, when the app goes in stop if the delay time has passed the app is locked and when the user take it again on foreground it will be asked to login.   
**appUnlock** Stop the observer 

Example: 

```kotlin
  J4mSec.biometricAuthenticationManager?.biometricLockBlocking(this)
```

 ### Certificate Pinning

 Creates a pre-configured OkHttpClient with cert pinning.

```kotlin
interface CertificatePinningManager {

    fun getCertificatePinningOkHttpClient(
        hostname: String,
        pins: List<String>,
        timoutSs : Long = 15,
        interceptors: List<Interceptor> = emptyList(),
        networkInterceptors: List<Interceptor> = emptyList()
    ) : OkHttpClient
}
```
The method returns an OkHttpClient with certificate pinning configurated on hostname for the pins in pins. It also adds interceptors and networkInterceptors if not empty. 

Example : 

```kotlin
val okHttpClient : OkHttpClient? = J4mSec.certificatePinningManager?.getCertificatePinningOkHttpClient(
                                    "hostname.com",
                                    pins = listOf(
                                        "sha256/4E6K2s2P4XoD+h2RsqctM3BTfB6IGYu1v9mVh9tNnE0=",
                                        "sha256/KW3qU2C0HfO1UuFYvGavcI6J2VtL7vIlbJ2HtA0XV9Q=",
                                        "sha256/CGvsaY3a5o2l0PJ7kxrkxF5DMCJkJAXhOPIZ0+E1nI="
                                    ),
                                    timoutSs = 15,
                                    interceptors = emptyList(),
                                    networkInterceptors = emptyList()
                                )
```

 ### Debug Mode Detection

 Detects if a debugger is attached (Java/native).

 ```kotlin
interface DebugDetectionManager {

    fun debugDetection(onDebuggerDetected: () -> Unit, onDebuggerNotDetected: () -> Unit)
    fun isDebuggerRunning() : Boolean
}
```

The methods identified if there is a debugger attached either if it is java based or native based. 

Example: 
 ```kotlin
J4mSec.debugDetectionManager?.debugDetection(
                                    onDebuggerDetected = {/* manage debugger detected */},
                                    onDebuggerNotDetected = {/* manage debugger not detected */}
                                )
 ```

### Hook Detection

Detect if the app is being hooked (Frida, Xposed).

```kotlin
interface HookerDetectionManager {
    fun hookDetected(onDetected: () -> Unit, onUndetected: () -> Unit)
    fun isHookDetected() : Boolean
}
```

The methods identified if there is an hooker (Frida, Xposed) that is working on the app.

Example:

```kotlin
J4mSec.hookerDetectionManager?.hookDetected(
                                    onDetected = {/* manage hooker detected */},
                                    onUndetected = {/* manage hooker not detected */}
                                )
```

### Secure Logging

Logs to encrypted file using the Android Keystore. 

```kotlin
interface SecureLogManager {

    suspend fun logDeviceInfo()
    fun logAsync(tag: String, message: String, level : LoggingLevel = LoggingLevel.INFO)
    suspend fun log(tag: String, message: String, level : LoggingLevel = LoggingLevel.INFO)
    fun exportDecryptedAsync(onResult: (List<String>) -> Unit)
    suspend fun exportDecrypted() : List<String>
    fun exportFilteredAsync(levels: List<LoggingLevel>, onResult: (List<String>) -> Unit)
    suspend fun exportFiltered(levels: List<LoggingLevel>): List<String>
    fun verifyIntegrityAsync(onResult: (Boolean) -> Unit)
    suspend fun verifyIntegrity() : Boolean
    fun exportDecryptedSkippingCorruptedAsync(onResult: (List<String>) -> Unit)
    suspend fun exportDecryptedSkippingCorrupted() : List<String>
    fun shutdownLogger()
}
```
**logDeviceInfo**: Log the informations of the device (MANUFACTURER, MODEL, VERSION.RELEASE, VERSION.SDK_INT)  
**logAsync**: Log tag and message in the log file. It uses the coroutinescope specified in initialization of the library (if not provided, use a dedicated coroutine scope, cancelled in shutdownLogger)  
**log**: Log tag and message in the log file. Suspend function.  
**exportDecryptedAsync**: Export the log file in a list of strings. It uses the coroutinescope specified in initialization of the library (if not provided, use a dedicated coroutine scope, cancelled in shutdownLogger)  
**exportDecrypted**: Export the log file in a list of strings. Suspend function.  
**exportFilteredAsync**: Export the log file filtering the tag with those included in levels in a list of strings. It uses the coroutinescope specified in initialization of the library (if not provided, use a dedicated coroutine scope, cancelled in shutdownLogger)  
**exportFiltered**: Export the log file filtering the tag with those included in levels in a list of strings. Suspend function.  
**verifyIntegrityAsync**: Verify the integrity of the log file.  
**exportDecryptedSkippingCorruptedAsync**:  Export the log file in a list of strings. It skips all the lines that do not succeed the integrity function. It uses the coroutinescope specified in initialization of the library (if not provided, use a dedicated coroutine scope, cancelled in shutdownLogger)  
**exportDecryptedSkippingCorrupted**: Export the log file in a list of strings. It skips all the lines that do not succeed the integrity function. Suspend function  
**shutdownLogger**: Shutdown the logger, clearing the reference to file, key and internal coroutine scope. Call it only on app closure.   

Example
```kotlin
 J4mSec.secureLogManager?.logAsync(
                "TAG",
                "Message",
                LoggingLevel.Info
            )
```
### Secure Shared Prefs

Encrypts preferences with Android Keystore. Auto-reset on key failure.

```kotlin
interface SecureSharedPrefsManager {

    fun observeErrors(owner: LifecycleOwner, handler: (SecureSharedPrefsErrors) -> Unit)
    fun observeAndAutoReset(owner: LifecycleOwner, coroutineScope: CoroutineScope)
    fun checkKeyHealth() : SecureSharedPrefsKeyHealth
    fun putString(key: String, value : String, commit : Boolean = false)
    fun getString(key: String) : String?
    fun remove(key: String)
    fun clear()
    fun hasKey(key: String) : Boolean
    fun resetDueKeyInvalid()
}
```
**observeErrors**: It allows the observation of a live data that emits errors.  
**observeAndAutoReset**: It observes the errors and react to keyInvalid error, clearing the shared preferences and regenerating the key.  
**checkKeyHealth**: it returns the status of the key (VALID / NOT VALID)  
**putString**: write a property in the shared properties  
**getString**: read a property from the shared properties  
**remove**: remove a key from the shared properties  
**clear**: clear completely the shared properties  
**hasKey**: it returns if the key is present in the shared properties  
**resetDueKeyInvalid**: reset the key and clear the shared properties  

Example:

```kotlin
J4mSec.secureSharedPrefsManager?.putString("TEST", "this is a test")
```
### Secure Strong Shared Prefs

Same as above, with timed access control after biometric/device auth. Time access slot identified by **strongAuthTimeoutSec** in initialization. 

```kotlin
interface SecureStrongSharedPrefsManager {

    fun observeErrors(owner: LifecycleOwner, handler: (SecureSharedPrefsErrors) -> Unit)
    fun observeAndAutoReset(owner: LifecycleOwner, coroutineScope: CoroutineScope)
    fun checkKeyHealth() : SecureSharedPrefsKeyHealth
    fun putString(key: String, value : String, commit : Boolean = false)
    fun getString(key: String) : String?
    fun remove(key: String)
    fun clear()
    fun hasKey(key: String) : Boolean
    fun resetDueKeyInvalid()
}
```
**observeErrors**: It allows the observation of a live data that emits errors.  
**observeAndAutoReset**: It observes the errors and react to keyInvalid error, clearing the shared preferences and regenerating the key.  
**checkKeyHealth**: it returns the status of the key (VALID / NOT VALID / NEEDS_AUTH)  
**putString**: write a property in the shared properties  
**getString**: read a property from the shared properties  
**remove**: remove a key from the shared properties  
**clear**: clear completely the shared properties  
**hasKey**: it returns if the key is present in the shared properties  
**resetDueKeyInvalid**: reset the key and clear the shared properties

Example:

```kotlin
J4mSec.secureStrongSharedPrefsManager?.putString("TEST", "this is a strong test")
```

### Tamper detection

Detect changes to APK or asset files.

```kotlin
interface TamperDetectionManager {

    fun appTamperingDetection(
        context: Context,
        knownAppHash: String,
        onTampering: () -> Unit,
        onIntegrity: () -> Unit
    )

    fun isAppTampered(
        context: Context,
        knownAppHash: String,
    ): Boolean

    fun assetTampered(
        context: Context,
        assetName: String,
        expectedBase64Hash: String,
        onTampering: () -> Unit,
        onIntegrity: () -> Unit
    )

    fun isAssetTampered(
        context: Context,
        assetName: String,
        expectedBase64Hash : String,
    ): Boolean

    fun verifyAllAssets(
        context: Context,
        expectedHashes: Map<String, String>
    ): List<String>
}
```
**appTamperingDetection** : Check the hash evaluated of the app with the one provided.  
**isAppTampered**: Check the hash evaluated of the app with the one provided. Returns a boolean   
**assetTampered**: Check the hash evaluated of an asset in the assets folder specified by assetName with an hash provided.   
**isAssetTampered**: Check the hash evaluated of an asset in the assets folder specified by assetName with an hash provided.  Returns a boolean   
**verifyAllAssets**: Verify multiple assets.

Example:

```kotlin
J4mSec.tamperDetectionManager?.appTamperingDetection(
                                    context = this@MainActivity,
                                    knownAppHash = appHash,
                                    onTampering = {/* manage tamper detected */},
                                    onIntegrity = {/* manage tamper not detected */}
                                )
```

### Virtual device detection

Detects emulator usage.

```kotlin
interface VirtualDeviceDetectionManager {
    fun deviceAnalysis(context: Context, onVirtual: () -> Unit, onPhysical: () -> Unit)
    fun isRunningOnVirtualDevice(context: Context): Boolean
}
```
**deviceAnalysis**: Analyse the device to find out if it is physical or virtual.  
**isRunningOnVirtualDevice**: Analyse the device to find out if it is physical or virtual. It returns a boolean.

Example: 

```kotlin
J4mSec.virtualDeviceDetectionManager?.deviceAnalysis(
                                    context,
                                    onVirtual = {/* manage virtual device detected */},
                                    onPhysical = {/* manage physical device detected */})
```

### Device Analysis

Aggregates and returns device security and fingerprint data.

```kotlin
    fun analyzeDeviceAsString(context: Context) : String = DeviceAnalysisManager.analyzeDeviceAsString(context = context)
    fun analyzeDevice(context: Context) : DeviceAnalysisResult = DeviceAnalysisManager.analyzeDevice(context = context)
```

**analyzeDeviceAsString**: Analyze the device returning informations on manufacturer, model, os, apiversion, type, debugger and hooker. It returns a string.  
**analyzeDevice**: Analyze the device returning informations on manufacturer, model, os, apiversion, type, debugger and hooker. It returns a model.
```kotlin
data class DeviceAnalysisResult(
    val isEmulator: Boolean,
    val isDebuggerAttached: Boolean,
    val isHookDetected: Boolean,
    val deviceManufacturer: String,
    val deviceModel: String,
    val androidVersion: String,
    val apiLevel: Int
)
```

Example: 
```kotlin
  J4mSec.analyzeDevice(context)
```

## LICENSE

This project is licensed under the Apache License. It was created for fun—if it’s useful, feel free to use it.
