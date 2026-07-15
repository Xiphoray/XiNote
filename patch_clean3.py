import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    code = f.read()

# Match the STT title and the entire STT options section
code = re.sub(r'                    Text\(\n                        text = Localization\.getString\("stt_engine", currentLanguage\) \?: "Speech Engine",.*?                    \}\n', '', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(code)

