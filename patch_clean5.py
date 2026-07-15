import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    lines = f.readlines()

# The card starts at line 368, let's find the end.
start_line = -1
for i, line in enumerate(lines):
    if '"stt_engine"' in line:
        # trace back to Card
        for j in range(i, -1, -1):
            if 'Card(' in lines[j]:
                start_line = j
                break
        break

if start_line != -1:
    brace_count = 0
    end_line = start_line
    for i in range(start_line, len(lines)):
        brace_count += lines[i].count('{') - lines[i].count('}')
        # Card( ... ) { ... }
        # Let's count parentheses and braces. This is tricky.
        pass

# simpler approach: remove line 367 to 453, assuming Card ends before line 455
end_line = -1
for j in range(start_line, len(lines)):
    if 'Card(' in lines[j] and j > start_line:
        end_line = j - 1
        break

if end_line == -1:
    end_line = len(lines) - 1

# Let's trace back from the next Card
del lines[start_line:end_line]

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.writelines(lines)

