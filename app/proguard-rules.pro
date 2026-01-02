-keep class * {
    *;
}


-keepattributes *Annotation*


-keepnames class *


-keepclassmembers class * {
    *;
}


-keepclassmembers class * {
    <init>(...);
}


-keepclassmembers class * {
    <fields>;
}


-dontoptimize

-dontobfuscate

-dontshrink


-keep class * extends android.app.Service
-keep class * extends android.app.Activity
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
