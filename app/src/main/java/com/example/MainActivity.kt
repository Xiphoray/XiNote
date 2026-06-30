package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.ui.EditNoteScreen
import com.example.ui.MainScreen
import com.example.ui.NoteViewModel
import com.example.ui.NoteViewModelFactory
import com.example.ui.Screen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(applicationContext, database.noteDao())

        // 2. Initialize ViewModel via Factory
        val factory = NoteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]

        // 3. Load SharedPreferences widget configurations, theme, and language on start
        viewModel.loadPreferences(applicationContext)

        // 4. Handle deep link intent from widget and sharing
        handleIntent(intent)

        setContent {
            val appTheme by viewModel.currentTheme.collectAsState()
            val darkTheme = when (appTheme) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val screen by viewModel.currentScreen.collectAsState()

                    // Enhanced sliding transitions for smooth, fluent popups
                    androidx.compose.animation.AnimatedContent(
                        targetState = screen,
                        label = "ScreenTransition",
                        transitionSpec = {
                            if (targetState is Screen.EditNote) {
                                // Slide in from bottom (bottom sheet style) for a beautiful editor entrance
                                (androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250))).togetherWith(
                                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                                )
                            } else if (initialState is Screen.EditNote) {
                                // Slide out to bottom when returning from the editor
                                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)).togetherWith(
                                    androidx.compose.animation.slideOutVertically(
                                        targetOffsetY = { it },
                                        animationSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                    ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250))
                                )
                            } else {
                                // Default crossfade and scale for settings/home transition
                                (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                                 androidx.compose.animation.scaleIn(initialScale = 0.96f, animationSpec = androidx.compose.animation.core.tween(300))).togetherWith(
                                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                                )
                            }
                        }
                    ) { currentScreen ->
                        when (currentScreen) {
                            is Screen.Home -> {
                                MainScreen(viewModel = viewModel)
                            }
                            is Screen.Settings -> {
                                com.example.ui.SettingsScreen(viewModel = viewModel)
                            }
                            is Screen.About -> {
                                com.example.ui.AboutScreen(viewModel = viewModel)
                            }
                            is Screen.ManageTemplates -> {
                                com.example.ui.ManageTemplatesScreen(viewModel = viewModel)
                            }
                            is Screen.EditNote -> {
                                EditNoteScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.triggerWidgetUpdate()
        }
    }

    // Parses widget click action intents and sharing intents to navigate appropriately
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        
        // Handle Sharing Text
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                viewModel.navigateToEditNoteWithSharedText(sharedText, applicationContext)
            }
            return
        }
        
        val action = intent.getStringExtra("action") ?: intent.action
        when (action) {
            "ADD_NOTE" -> {
                viewModel.navigateToEditNote(null, applicationContext)
            }
            "EDIT_NOTE" -> {
                val noteId = intent.getIntExtra("note_id", -1)
                if (noteId != -1) {
                    viewModel.navigateToEditNote(noteId, applicationContext)
                }
            }
        }
    }
}
