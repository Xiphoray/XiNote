package com.example.widget

import android.content.Intent
import android.service.quicksettings.TileService
import com.example.MainActivity

class NoteTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "ADD_NOTE"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivityAndCollapse(intent)
    }
}
