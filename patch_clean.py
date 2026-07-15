import re

with open("app/src/main/java/com/example/ui/NoteViewModel.kt", "r") as f:
    code = f.read()

code = re.sub(r'    fun loadSttEngine\(context: Context\) \{.*?\n    \}\n\n    fun changeSttEngine\(context: Context, engine: Int\) \{.*?\n    \}\n\n', '', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/NoteViewModel.kt", "w") as f:
    f.write(code)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    code = f.read()

# Replace the whole STT section in SettingsScreen
stt_section = r"""                    item \{
                        Column\(
                            modifier = Modifier
                                \.fillMaxWidth\(\)
                                \.background\(MaterialTheme\.colorScheme\.surfaceVariant\.copy\(alpha = 0\.3f\), RoundedCornerShape\(16\.dp\)\)
                                \.clip\(RoundedCornerShape\(16\.dp\)\)
                        \) \{
                            Text\(
                                text = Localization\.getString\("stt_engine", currentLanguage\) \?: "Speech Engine",
                                style = MaterialTheme\.typography\.titleMedium,
                                color = MaterialTheme\.colorScheme\.primary,
                                fontWeight = FontWeight\.Bold,
                                modifier = Modifier\.padding\(start = 16\.dp, top = 16\.dp, end = 16\.dp, bottom = 8\.dp\)
                            \)
                            Column\(
                                modifier = Modifier
                                    \.fillMaxWidth\(\)
                                    \.padding\(horizontal = 8\.dp, vertical = 8\.dp\),
                                verticalArrangement = Arrangement\.spacedBy\(8\.dp\)
                            \) \{
                                listOf\(
                                    0 to "stt_native",
                                    1 to "stt_vosk"
                                \)\.forEach \{ \(engineValue, stringKey\) ->
                                    val isSelected = sttEngine == engineValue
                                    val itemBgColor = if \(isSelected\) \{
                                        MaterialTheme\.colorScheme\.primaryContainer
                                    \} else \{
                                        MaterialTheme\.colorScheme\.surface
                                    \}
                                    val itemContentColor = if \(isSelected\) \{
                                        MaterialTheme\.colorScheme\.onPrimaryContainer
                                    \} else \{
                                        MaterialTheme\.colorScheme\.onSurface
                                    \}
                                    
                                    Row\(
                                        modifier = Modifier
                                            \.fillMaxWidth\(\)
                                            \.clip\(RoundedCornerShape\(12\.dp\)\)
                                            \.background\(itemBgColor\)
                                            \.clickable \{
                                                viewModel\.changeSttEngine\(context, engineValue\)
                                            \}
                                            \.padding\(horizontal = 16\.dp, vertical = 12\.dp\),
                                        verticalAlignment = Alignment\.CenterVertically
                                    \) \{
                                        Box\(
                                            modifier = Modifier
                                                \.size\(40\.dp\)
                                                \.background\(if \(isSelected\) MaterialTheme\.colorScheme\.primary else MaterialTheme\.colorScheme\.surfaceVariant, CircleShape\),
                                            contentAlignment = Alignment\.Center
                                        \) \{
                                            Icon\(
                                                imageVector = if \(engineValue == 0\) Icons\.Default\.Mic else Icons\.Default\.MicNone,
                                                contentDescription = null,
                                                tint = if \(isSelected\) MaterialTheme\.colorScheme\.onPrimary else MaterialTheme\.colorScheme\.onSurfaceVariant
                                            \)
                                        \}
                                        Spacer\(modifier = Modifier\.width\(16\.dp\)\)
                                        Text\(
                                            text = Localization\.getString\(stringKey, currentLanguage\) \?: stringKey,
                                            style = MaterialTheme\.typography\.bodyLarge,
                                            color = itemContentColor,
                                            fontWeight = if \(isSelected\) FontWeight\.Bold else FontWeight\.Normal,
                                            modifier = Modifier\.weight\(1f\)
                                        \)
                                        if \(isSelected\) \{
                                            Icon\(
                                                imageVector = Icons\.Default\.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme\.colorScheme\.primary
                                            \)
                                        \}
                                    \}
                                \}
                            \}
                        \}
                    \}"""
                    
code = re.sub(stt_section, '', code)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(code)

