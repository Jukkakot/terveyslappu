# Release shrinking (R8). Libraries ship their own rules; this keeps crash logs readable.

# Line numbers in stack traces (the log's app.crash line).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
