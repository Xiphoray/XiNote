import ctypes
import os

vosk_path = None
for root, dirs, files in os.walk(os.path.expanduser("~/.gradle")):
    if "libvosk.so" in files and "x86_64" in root:
        vosk_path = os.path.join(root, "libvosk.so")
        break

if not vosk_path:
    # Try finding in the apk directory or extract from aar
    os.system("unzip -o app/build/outputs/apk/debug/app-debug.apk lib/x86_64/libvosk.so")
    vosk_path = "lib/x86_64/libvosk.so"

print(f"Loading {vosk_path}")
lib = ctypes.cdll.LoadLibrary(vosk_path)
lib.vosk_set_log_level(1)

model_path = os.path.abspath("app/src/main/assets/model-cn")
print(f"Loading model from {model_path}")
model = lib.vosk_model_new(model_path.encode('utf-8'))
if model:
    print("Model loaded successfully!")
else:
    print("Model load failed!")
