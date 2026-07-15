import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

code = code.replace("import java.net.URL\nimport java.io.File\nimport java.io.FileOutputStream\nimport java.util.zip.ZipInputStream\n\nabstract class", "abstract class")

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
