import urllib.request
content = urllib.request.urlopen("https://raw.githubusercontent.com/alphacep/vosk-api/master/src/recognizer.cc").read().decode('utf-8')
print(content)
