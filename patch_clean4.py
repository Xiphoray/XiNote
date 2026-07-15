import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    code = f.read()

# I will find the Card that contains "stt_engine" and delete it.
# Instead of regex, I'll just use string find and balance braces.

idx = code.find('"stt_engine"')
if idx != -1:
    start_idx = code.rfind('item {', 0, idx)
    if start_idx != -1:
        # count braces
        brace_count = 0
        end_idx = start_idx
        while end_idx < len(code):
            if code[end_idx] == '{':
                brace_count += 1
            elif code[end_idx] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end_idx += 1
                    break
            end_idx += 1
        
        code = code[:start_idx] + code[end_idx:]
        
with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(code)

