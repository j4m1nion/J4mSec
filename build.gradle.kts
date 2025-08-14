import java.util.Base64
import kotlin.experimental.xor
import java.security.*

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.jam1nion.j4msec"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "consumer-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures{
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
                groupId = "com.github.j4m1nion"
                artifactId = "j4msec"
                version = "1.0.4"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.okhttp)
}

val extractCertHash by tasks.registering {
    group = "security"
    description = "Extracts Base64 SHA-256 hash of public key from a JKS file."

    val keystorePath = project.findProperty("keystore")?.toString()
        ?: throw GradleException("Missing -Pkeystore=<path>")
    val alias = project.findProperty("alias")?.toString()
        ?: throw GradleException("Missing -Palias=<alias>")
    val storepass = project.findProperty("storepass")?.toString()
        ?: throw GradleException("Missing -Pstorepass=<password>")
    val keypass = project.findProperty("keypass")?.toString() ?: storepass

    doLast {
        val keystoreFile = file(keystorePath)
        val keystore = KeyStore.getInstance("JKS")
        keystore.load(keystoreFile.inputStream(), storepass.toCharArray())

        val cert = keystore.getCertificate(alias)
            ?: throw GradleException("Certificate alias not found: $alias")

        val publicKey = cert.publicKey
        val derEncoded = publicKey.encoded

        val sha256 = MessageDigest.getInstance("SHA-256").digest(derEncoded)
        val base64Hash = Base64.getEncoder().encodeToString(sha256)

        println("Base64 SHA-256 hash of public key:")
        println(base64Hash)
    }
}
//usage
//    ./gradlew extractCertHash \
//-Pkeystore=./release.jks \
//-Palias=myalias \
//-Pstorepass=mypassword
//-Pkeypass=mypassword


val encodeCert by tasks.registering {
    group = "security"
    description = "Obfuscates a certificate hash with XOR, reverse, and optional salt."

    val certHash = project.findProperty("certHash")?.toString()
        ?: throw GradleException("Please provide -PcertHash=<Base64 string>")

    val xorKey = project.findProperty("xorKey")?.toString()?.firstOrNull() ?: 0x5A.toChar()
    val reverse = project.findProperty("reverse")?.toString()?.toBooleanStrictOrNull() ?: true
    val salt = project.findProperty("salt")?.toString()

    doLast {
        val decoded = Base64.getDecoder().decode(certHash)
        val xored = decoded.map { it xor xorKey.code.toByte() }.toByteArray()
        val reversed = if (reverse) xored.reversedArray() else xored
        val resultBase64 = Base64.getEncoder().encodeToString(reversed)
        val final = if (salt != null) resultBase64 + salt else resultBase64

        println("Obfuscated cert hash:")
        println(final)
    }
}
//usage
//    ./gradlew encodeCert \
//-PcertHash="certificatekeybase64" \
//-PxorKey=Z \
//-Preverse=true \
//-Psalt=com.example.myapp