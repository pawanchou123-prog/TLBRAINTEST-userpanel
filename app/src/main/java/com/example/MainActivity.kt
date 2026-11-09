package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.core.configureAppCheck
import com.example.ui.student.StudentAppRoot
import com.example.ui.theme.TlBrainTestTheme

/**
 * MainActivity launches Application 1: STUDENT USER APPLICATION (/app).
 *
 * Separate namespaces (/admin and /question-manager) remain isolated in their own
 * packages and are NOT merged into the Student Application dashboard or navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        com.example.core.audio.SoundManager.init(this)
        com.example.ui.student.audio.StudentAudioManager.init(this)
        enableEdgeToEdge()
        setContent {
            TlBrainTestTheme {
                StudentAppRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.ui.student.audio.StudentAudioManager.onGameExited()
    }

    override fun onPause() {
        super.onPause()
        com.example.ui.student.audio.StudentAudioManager.onGameEntered()
    }
}
