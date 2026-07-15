import re

# EditNoteScreen
with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "r") as f:
    code = f.read()

# I used a bad regex for removing the mic icon logic in EditorCardLayout.
mic_code = """                        IconButton(
                            onClick = onVoiceInputClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Voice Input",
                                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }"""
code = code.replace(mic_code, "")

# Some whitespace issues might have caused it to not match. Let's just find "isListening" and wipe that block.
code = re.sub(r'                        IconButton\(\n                            onClick = onVoiceInputClick,.*?\n                        \}', '', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "w") as f:
    f.write(code)

# NoteViewModel
with open("app/src/main/java/com/example/ui/NoteViewModel.kt", "r") as f:
    code = f.read()

code = code.replace("loadSttEngine(context)", "")

with open("app/src/main/java/com/example/ui/NoteViewModel.kt", "w") as f:
    f.write(code)

# SettingsScreen
with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    code = f.read()

code = re.sub(r'                    item \{\n                        Column\(\n                            modifier = Modifier\n                                \.fillMaxWidth\(\)\n                                \.background\(MaterialTheme\.colorScheme\.surfaceVariant\.copy\(alpha = 0\.3f\), RoundedCornerShape\(16\.dp\)\).*?0 to "stt_native",.*?\}', '', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(code)

