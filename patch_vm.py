import re

with open("app/src/main/java/com/example/ui/NoteViewModel.kt", "r") as f:
    code = f.read()

code = re.sub(r'    val sttEngine = MutableStateFlow\(0\) // 0: Native, 1: Vosk\n', '', code)
code = re.sub(r'        sttEngine\.value = sp\.getInt\("stt_engine", 0\)\n', '', code)
code = re.sub(r'    fun setSttEngine\(engine: Int\) \{\n        sttEngine\.value = engine\n        sp\.edit\(\)\.putInt\("stt_engine", engine\)\.apply\(\)\n    \}\n', '', code)

with open("app/src/main/java/com/example/ui/NoteViewModel.kt", "w") as f:
    f.write(code)
