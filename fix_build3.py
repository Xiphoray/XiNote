import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

imports = re.findall(r'^import .*', code, re.MULTILINE)
unique_imports = []
for imp in imports:
    if imp not in unique_imports:
        unique_imports.append(imp)

code_without_imports = re.sub(r'^import .*\n', '', code, flags=re.MULTILINE)

with open("app/build.gradle.kts", "w") as f:
    f.write("\n".join(unique_imports) + "\n\n" + code_without_imports)
