object Sdk {
    const val MIN_SDK_VERSION = 21
    const val TARGET_SDK_VERSION = 30
    const val COMPILE_SDK_VERSION = 30
    const val BUILD_TOOLS_VERSION = "30.0.2"
}

object BuildPluginsVersion {
    const val AGP = "4.0.0"
    const val KOTLIN = "1.3.72"
}

object App {
    const val APP_ID = "dev.luboganev.progress"

    const val APP_VERSION_NAME = "1.0.0"
    const val APP_VERSION_CODE = 1
}

object AndroidX {
    const val core = "androidx.core:core-ktx:1.3.1"
    const val appCompat = "androidx.appcompat:appcompat:1.2.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    val lifeCycle = LifeCycle
    const val materialComponents = "com.google.android.material:material:1.2.0"
}

object LifeCycle {
    private const val version = "2.2.0"
    val runtime = "androidx.lifecycle:lifecycle-runtime:$version"
    val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
    val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
    val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
}