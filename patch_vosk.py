import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

new_catch = """            } catch (e: Throwable) {
                var fileList = ""
                try {
                    val copiedFiles = mutableListOf<String>()
                    fun listFilesRec(dir: java.io.File, prefix: String = "") {
                        dir.listFiles()?.forEach { 
                            if (it.isDirectory) {
                                listFilesRec(it, "$prefix${it.name}/")
                            } else {
                                copiedFiles.add("$prefix${it.name} (${it.length()})")
                            }
                        }
                    }
                    if (resultPathDir.exists()) {
                        listFilesRec(resultPathDir)
                        fileList = "Files: " + copiedFiles.joinToString(", ")
                    } else {
                        fileList = "Dir does not exist"
                    }
                } catch(ignored: Exception) {}
            
                var logStr = "Logs:\\n"
                try {
                    val process = Runtime.getRuntime().exec("logcat -d -v time")
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                    val logs = mutableListOf<String>()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("VOSK") == true || line?.contains("kaldi") == true || line?.contains("Vosk") == true || line?.contains("org.vosk") == true) {
                            logs.add(line!!)
                        }
                    }
                    logStr += logs.takeLast(15).joinToString("\\n")
                } catch (ignored: Exception) {}
                
                val fullError = "${e.message}\\n$fileList\\n$logStr"
                modelError = fullError
                android.util.Log.e("VoskSTT", "Error initializing model: ", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Vosk Model Error, click mic for details", android.widget.Toast.LENGTH_LONG).show()
                }
            }"""

code = re.sub(r'            \} catch \(e: Throwable\) \{.*?\}\n        \}\.start\(\)', new_catch + '\n        }.start()', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "w") as f:
    f.write(code)
