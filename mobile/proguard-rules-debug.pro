-include proguard-rules.pro

-dontoptimize
-dontobfuscate

# LeakCanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
-dontwarn com.squareup.leakcanary.DisplayLeakService