import re

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "r") as f:
    code = f.read()

# Remove the STT logic from EditNoteScreen
# First, remove STT vars
code = re.sub(r'    var sttErrorDialog by remember \{ mutableStateOf<String\?>\(null\) \}\n\n    val sttEngine by viewModel.sttEngine.collectAsState\(\)\n    val sttProvider = remember\(sttEngine\) \{ com.example.stt.STTFactory.createProvider\(context, sttEngine\) \}\n    var isListening by remember \{ mutableStateOf\(false\) \}\n\n    DisposableEffect\(sttProvider\) \{\n        onDispose \{\n            sttProvider.destroy\(\)\n        \}\n    \}\n\n    val recordAudioPermissionLauncher.*?\n        \}\n    \}', '', code, flags=re.DOTALL)

# Remove startVoiceInput function
code = re.sub(r'    fun startVoiceInput\(\) \{.*?\n    \}\n    \n    var showSettingsSheet', '    var showSettingsSheet', code, flags=re.DOTALL)

# Remove sttErrorDialog composable
code = re.sub(r'    if \(sttErrorDialog != null\) \{.*?\n    \}\n\n    Scaffold\(', '    Scaffold(', code, flags=re.DOTALL)

# Remove Voice Input args from EditorCardLayout call 1
code = re.sub(r'                            currentLanguage = currentLanguage,\n                            isListening = isListening,\n                            onVoiceInputClick = \{ startVoiceInput\(\) \}', '                            currentLanguage = currentLanguage', code)

# Remove Voice Input args from EditorCardLayout call 2
code = re.sub(r'                            currentLanguage = currentLanguage,\n                            isListening = isListening,\n                            onVoiceInputClick = \{ startVoiceInput\(\) \}', '                            currentLanguage = currentLanguage', code)

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "w") as f:
    f.write(code)
