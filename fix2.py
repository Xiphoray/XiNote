import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

# Replace the literal newlines inside strings with actual \n escape sequence
code = re.sub(r'var logStr = "Logs:\n"', 'var logStr = "Logs:\\\\n"', code)
code = re.sub(r'logs.takeLast\(15\).joinToString\("\n"\)', 'logs.takeLast(15).joinToString("\\\\n")', code)
code = re.sub(r'val fullError = "\$\{e.message\}\n\$fileList\n\$logStr"', 'val fullError = "${e.message}\\\\n$fileList\\\\n$logStr"', code)

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "w") as f:
    f.write(code)
