# ============================================================
# 通用规则
# ============================================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

# ============================================================
# Kotlin & Kotlinx
# ============================================================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlinx Serialization — 保留 @Serializable 注解的类和序列化器
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.lemon.mcdevmanagermp.**$$serializer { *; }
-keepclassmembers class com.lemon.mcdevmanagermp.** {
    *** Companion;
}
-keepclasseswithmembers class com.lemon.mcdevmanagermp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Kotlinx DateTime
-dontwarn kotlinx.datetime.**

# SLF4J Simple (Android)
-dontwarn org.slf4j.impl.**
-keep class org.slf4j.impl.** { *; }

# ============================================================
# Kotlinx Serialization (通配 — 保护所有 @Serializable 类)
# ============================================================
-keep @kotlinx.serialization.Serializable class ** {
    <init>(...);
    *** Companion;
    static *** Companion;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    static *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================
# Ktor
# ============================================================
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Ktor Client
-keep class io.ktor.client.** { *; }
-keep class io.ktor.http.** { *; }

# ============================================================
# Ktorfit
# ============================================================
-dontwarn de.jensklingenberg.ktorfit.**
-keep class de.jensklingenberg.ktorfit.** { *; }
-keep @de.jensklingenberg.ktorfit.http.* class * { *; }
-keepclassmembers class * {
    @de.jensklingenberg.ktorfit.http.* <methods>;
}

# ============================================================
# Compose Multiplatform
# ============================================================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

-dontwarn org.jetbrains.compose.**
-keep class org.jetbrains.compose.** { *; }

# Compose Compiler 生成的类
-keep class **.ComposableSingletons$* { *; }
-keepclassmembers class **.ComposableSingletons$* { *; }

# ============================================================
# AndroidX Navigation Compose
# ============================================================
-keepnames class androidx.navigation.**
-keepclassmembers class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ============================================================
# AndroidX Room
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Room 生成的类
-keep class * extends androidx.room.PrimaryKey { *; }
-keep class * extends androidx.room.ForeignKey { *; }
-keep class * extends androidx.room.Index { *; }

# ============================================================
# AndroidX Lifecycle
# ============================================================
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-dontwarn androidx.lifecycle.**

# ============================================================
# AndroidX Activity
# ============================================================
-keep class * extends androidx.activity.ComponentActivity { *; }
-keepclassmembers class * extends androidx.activity.ComponentActivity { *; }

# ============================================================
# Sketch (Image Loading)
# ============================================================
-dontwarn io.github.panpf.sketch4.**
-keep class io.github.panpf.sketch4.** { *; }
-keepclassmembers class io.github.panpf.sketch4.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }

# ============================================================
# OkHttp / OkIo
# ============================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keepnames class okhttp3.internal.Util { *; }

# ============================================================
# BigNum
# ============================================================
-dontwarn com.ionspin.kotlin.bignum.**
-keep class com.ionspin.kotlin.bignum.** { *; }

# ============================================================
# Kotlin Logging (oshai)
# ============================================================
-dontwarn io.github.oshai.**
-keep class io.github.oshai.** { *; }

# ============================================================
# Logback
# ============================================================
-dontwarn ch.qos.logback.**
-keep class ch.qos.logback.** { *; }

# ============================================================
# 项目自身 — 保留所有 VO / Entity / API 接口
# ============================================================

# VO (Value Objects) — 序列化/反序列化需要完整字段
-keep class com.lemon.mcdevmanagermp.data.vo.** { *; }

# DTO (Data Transfer Objects)
-keep class com.lemon.mcdevmanagermp.data.dto.** { *; }

# Entity (Room 数据库实体)
-keep class com.lemon.mcdevmanagermp.data.db.entity.** { *; }

# API 接口 (Ktorfit 代理类需要方法签名)
-keep interface com.lemon.mcdevmanagermp.data.api.** { *; }

# 平台相关类
-keep class com.lemon.mcdevmanagermp.platform.** { *; }

# 导航路由（Route sealed interface + 子类）
-keep class * extends java.lang.Enum { *; }
-keep class com.lemon.mcdevmanagermp.ui.navigation.Route { *; }
-keep class com.lemon.mcdevmanagermp.ui.navigation.Route$* { *; }

# 序列化相关的 companion object
-keepclassmembers class com.lemon.mcdevmanagermp.** {
    public static final ** Companion;
}

# ============================================================
# 通用：Native 方法、JNI
# ============================================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# 通用：View 构造函数（Android 框架需要）
-keepclassmembers class * extends android.view.View {
    <init>(android.content.Context);
    <init>(android.content.Context, android.util.AttributeSet);
    <init>(android.content.Context, android.util.AttributeSet, int);
}

# 通用：Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# 通用：Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 通用：R 文件
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 移除日志（Release 构建）
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
