import re

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "r") as f:
    code = f.read()

# Add sttErrorDialog state
state_code = """    var showInWidget by rememberSaveable(noteId) { mutableStateOf(note?.showInWidget ?: true) }
    var sttErrorDialog by remember { mutableStateOf<String?>(null) }"""

code = re.sub(r'    var showInWidget by rememberSaveable\(noteId\) \{ mutableStateOf\(note\?\.showInWidget \?: true\) \}', state_code, code)

# Add AlertDialog UI
dialog_code = """    if (sttErrorDialog != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { sttErrorDialog = null },
            title = { androidx.compose.material3.Text("Voice Input Error") },
            text = { 
                androidx.compose.foundation.lazy.LazyColumn {
                    item {
                        androidx.compose.material3.Text(sttErrorDialog ?: "")
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { sttErrorDialog = null }) {
                    androidx.compose.material3.Text("OK")
                }
            }
        )
    }

    Scaffold("""

code = re.sub(r'    Scaffold\(', dialog_code, code)

# Change onError to set sttErrorDialog
code = re.sub(r'onError = \{ error ->\n.*?android\.widget\.Toast\.makeText\(context, error, android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\)\n.*?\}', r'onError = { error -> sttErrorDialog = error }', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/EditNoteScreen.kt", "w") as f:
    f.write(code)
