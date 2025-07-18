package com.jam1nion.j4msec.features.certificatepinning

import okhttp3.Interceptor
import okhttp3.OkHttpClient

interface CertificatePinningManager {

    fun getCertificatePinningOkHttpClient(
        hostname: String,
        pins: List<String>,
        timoutSs : Long = 15,
        interceptors: List<Interceptor> = emptyList(),
        networkInterceptors: List<Interceptor> = emptyList()
    ) : OkHttpClient
}