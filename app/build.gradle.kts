plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kitagent.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kitagent.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}

kotlin { jvmToolchain(17) }

val patchCexSource by tasks.registering {
    doLast {
        val source = file("src/main/java/com/kitagent/android/MainActivity.kt")
        var s = source.readText()
        s = s.replace("private inner class CexTerminal(ctx:Activity):LinearLayout(ctx)", "private inner class CexTerminal(private val ctx:Activity):LinearLayout(ctx)")
        s = s.replace("(bar.getChildAt(i).getChildAt(1) as TextView).text=vals[i]", "((bar.getChildAt(i) as? LinearLayout)?.getChildAt(1) as? TextView)?.text=vals[i]")
        source.writeText(s)
    }
}

tasks.named("preBuild") { dependsOn(patchCexSource) }
