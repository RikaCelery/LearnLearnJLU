-keep class androidx.compose.runtime.** { *; }
-keep class androidx.collection.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.ui.text.platform.ReflectionUtil { *; }
# We're excluding Material 2 from the project as we're using Material 3
-dontwarn androidx.compose.material.**
##---------------Begin: proguard configuration for Pusher Java Client  ----------
-dontwarn org.slf4j.LoggerFactory
-dontwarn org.slf4j.MarkerFactory
-dontwarn org.slf4j.MDC
##---------------End: proguard configuration for Pusher Java Client  ----------
# Kotlinx coroutines rules seems to be outdated with the latest version of Kotlin and Proguard
-keep class kotlinx.coroutines.** { *; }
-dontwarn org.jsoup.**
-dontwarn okhttp3.internal.**
-dontwarn nl.adaptivity.xmlutil.StAXWriter