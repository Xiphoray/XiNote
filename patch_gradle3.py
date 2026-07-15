import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

code = code.replace("  implementation(\"net.java.dev.jna:jna:5.2.0@aar\")\n", "")
code = code.replace("  implementation(\"com.alphacephei:vosk-android:0.3.47\")\n", "")

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
