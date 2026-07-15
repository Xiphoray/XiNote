import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    code = f.read()

code = re.sub(r'    val sttEngine by viewModel.sttEngine.collectAsState\(\)\n', '', code)

stt_block = r"""                    item \{
                        Card\(
                            modifier = Modifier\.fillMaxWidth\(\),
                            colors = CardDefaults\.cardColors\(containerColor = MaterialTheme\.colorScheme\.surfaceVariant\.copy\(alpha = 0\.5f\)\),
                            shape = RoundedCornerShape\(16\.dp\)
                        \) \{
                            Column\(modifier = Modifier\.padding\(16\.dp\)\) \{
                                Text\(
                                    text = Localization\.getString\("stt_engine", currentLanguage\) \?: "Speech Engine",
                                    style = MaterialTheme\.typography\.titleMedium,
                                    color = MaterialTheme\.colorScheme\.primary,
                                    modifier = Modifier\.padding\(bottom = 12\.dp\)
                                \)
                                val sttOptions = listOf\(
                                    0 to "stt_native",
                                    1 to "stt_vosk"
                                \)
                                sttOptions\.forEach \{ \(engineValue, labelKey\) ->
                                    val isSelected = sttEngine == engineValue
                                    Row\(
                                        modifier = Modifier
                                            \.fillMaxWidth\(\)
                                            \.clickable \{ viewModel\.setSttEngine\(engineValue\) \}
                                            \.padding\(vertical = 8\.dp\),
                                        verticalAlignment = Alignment\.CenterVertically
                                    \) \{
                                        androidx\.compose\.material3\.RadioButton\(
                                            selected = isSelected,
                                            onClick = \{ viewModel\.setSttEngine\(engineValue\) \}
                                        \)
                                        Text\(
                                            text = Localization\.getString\(labelKey, currentLanguage\) \?: labelKey,
                                            style = MaterialTheme\.typography\.bodyMedium,
                                            modifier = Modifier\.padding\(start = 8\.dp\)
                                        \)
                                    \}
                                \}
                            \}
                        \}
                    \}"""
                    
code = re.sub(stt_block, '', code)

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(code)
