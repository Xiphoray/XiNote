import urllib.request
content = urllib.request.urlopen("https://raw.githubusercontent.com/alphacep/vosk-api/master/android/lib/src/main/java/org/vosk/android/StorageService.java").read().decode('utf-8')
print(content)
