import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

code = code.replace("val finalMdl = java.io.File(targetDir, \"am/final.mdl\")", "val finalMdl = File(targetDir, \"am/final.mdl\")")

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
