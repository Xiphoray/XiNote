import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

code = re.sub(r'abstract class DownloadAndExtractModelTask.*', '', code, flags=re.DOTALL)

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
