import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

# Remove the bad imports from the bottom
code = re.sub(r'import java\.net\.URL\nimport java\.util\.zip\.ZipInputStream\nimport java\.io\.FileOutputStream\n', '', code)

# Prepend them to the top
new_imports = "import java.net.URL\nimport java.util.zip.ZipInputStream\nimport java.io.FileOutputStream\n"
code = new_imports + code

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
