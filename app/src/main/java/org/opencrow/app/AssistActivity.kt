package org.opencrow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.opencrow.app.ui.screens.assist.AssistScreen
import org.opencrow.app.ui.theme.OpenCrowTheme

class AssistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val screenshotPath = intent?.getStringExtra("screenshot_path")

        setContent {
            OpenCrowTheme {
                AssistScreen(
                    screenshotPath = screenshotPath,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
