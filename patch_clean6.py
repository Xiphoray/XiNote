import re

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "r") as f:
    code = f.read()

fab_code = r"""                // Voice Input Floating Button
                androidx\.compose\.material3\.FloatingActionButton\(
                    onClick = onVoiceInputClick,
                    modifier = Modifier
                        \.align\(Alignment\.BottomEnd\)
                        \.padding\(bottom = 16\.dp, end = 16\.dp\),
                    containerColor = if \(isListening\) MaterialTheme\.colorScheme\.errorContainer else MaterialTheme\.colorScheme\.primaryContainer,
                    contentColor = if \(isListening\) MaterialTheme\.colorScheme\.onErrorContainer else MaterialTheme\.colorScheme\.onPrimaryContainer,
                    shape = CircleShape,
                    elevation = androidx\.compose\.material3\.FloatingActionButtonDefaults\.elevation\(defaultElevation = 4\.dp\)
                \) \{
                    Icon\(
                        imageVector = if \(isListening\) Icons\.Default\.Mic else Icons\.Default\.MicNone,
                        contentDescription = "Voice Input"
                    \)
                \}"""
                
code = re.sub(fab_code, '', code)

# Let's also make sure we didn't miss anything
code = re.sub(r'                // Voice Input Floating Button\n                androidx\.compose\.material3\.FloatingActionButton\(.*?\n                \}', '', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "w") as f:
    f.write(code)

