import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

# Replace the whole class to be safe and clean!
