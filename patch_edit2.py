import re

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "r") as f:
    code = f.read()

# Fix definition
code = re.sub(r'    currentLanguage: AppLanguage,\n    isListening: Boolean = false,\n    onVoiceInputClick: \(\) -> Unit = \{\}\n\) \{', '    currentLanguage: AppLanguage\n) {', code)

# Remove mic icon logic
mic_icon = """                        IconButton(
                            onClick = onVoiceInputClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Voice Input",
                                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }"""
code = code.replace(mic_icon, "")

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "w") as f:
    f.write(code)
