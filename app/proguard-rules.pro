# Release shrinking (R8). Libraries ship their own rules; this keeps crash logs readable.

# Line numbers in stack traces (the log's app.crash line).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Apache POI (history import of .xls): desktop-only classes it references but HSSF reading never
# touches, and build-time annotations of log4j-api.
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.swing.**
-dontwarn javax.xml.stream.**
-dontwarn aQute.bnd.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn org.osgi.**
# HSSF record classes are created through a lookup table; keep them whole.
-keep class org.apache.poi.hssf.record.** { *; }
