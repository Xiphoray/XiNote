    private fun initModel() {
        StorageService.unpack(context, "model-cn", "model",
            { model ->
                this.model = model
            },
            { exception ->
                android.util.Log.e("VoskSTT", "Error unpacking model: " + exception.message, exception)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Error unpacking model: " + exception.message, android.widget.Toast.LENGTH_LONG).show()
                }
            })
    }
