package com.jam1nion.j4msec.models

import kotlinx.coroutines.CoroutineScope
import java.io.File

data class J4msecSecureLogConfiguration(
    val logCoroutineScope: CoroutineScope? = null,
    val logDirectory: File? = null,
    val logFilename: String? = null
)