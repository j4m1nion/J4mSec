package com.jam1nion.j4msec.features.certificatepinning

import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import com.jam1nion.j4msec.features.utils.isBase64Encoded
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal class CertificatePinningManagerImpl : CertificatePinningManager {

   companion object{
       const val TAG = "CertificatePinningManager"
   }

    override fun getCertificatePinningOkHttpClient(
        hostname: String,
        pins: List<String>,
        timoutSs : Long,
        interceptors: List<Interceptor>,
        networkInterceptors: List<Interceptor>
    ) : OkHttpClient{
        val pinMap = pins.groupBy { pinIsValid(it) }
        val discardedPins = pinMap[false].orEmpty()
        val validPins = pinMap[true].orEmpty()

        if(discardedPins.isNotEmpty() && J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "$hostname Discarded certificate pins: $discardedPins",
                LoggingLevel.WARN
            )
        }



        val builder = OkHttpClient.Builder()
            .connectTimeout(timoutSs, TimeUnit.SECONDS)
            .readTimeout(timoutSs, TimeUnit.SECONDS)

        if(interceptors.isNotEmpty()){
            interceptors.forEach {
                builder.addInterceptor(it)
            }
        }

        if(networkInterceptors.isNotEmpty()){
            networkInterceptors.forEach {
                builder.addNetworkInterceptor(it)
            }
        }

        if(validPins.isEmpty() == true && J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "$hostname has no valid pin.",
                LoggingLevel.ERROR
            )
        }
        else{
            val certificatePinner = CertificatePinner.Builder().apply {
                validPins.forEach { pin ->
                    add(hostname, pin)
                }
            }.build()
            builder.certificatePinner(certificatePinner)
        }

        return builder.build()

    }


    private fun pinIsValid(pin : String): Boolean {
        return pin.startsWith("sha256/") && pin.removePrefix("sha256/").isBase64Encoded()
    }


}