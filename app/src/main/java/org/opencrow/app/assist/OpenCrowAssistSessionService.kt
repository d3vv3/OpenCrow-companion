package org.opencrow.app.assist

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Creates the VoiceInteractionSession that receives assist data (including screenshots)
 * from the system, then delegates to AssistActivity for the Compose-based UI.
 */
class OpenCrowAssistSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return OpenCrowAssistSession(this)
    }
}

private class OpenCrowAssistSession(
    private val service: OpenCrowAssistSessionService
) : VoiceInteractionSession(service) {

    companion object {
        private const val TAG = "AssistSession"
    }

    private var screenshotPath: String? = null

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        if (screenshot != null) {
            try {
                val file = File(service.cacheDir, "assist_screenshot_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    screenshot.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                screenshotPath = file.absolutePath
                Log.d(TAG, "Screenshot saved: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save screenshot", e)
            }
        }
    }

    override fun onHandleAssist(state: AssistState) {
        // Launch AssistActivity with the screenshot path if available
        val intent = Intent(service, org.opencrow.app.AssistActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            screenshotPath?.let { putExtra("screenshot_path", it) }
        }
        service.startActivity(intent)
        hide()
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        // Reset screenshot for each new session
        screenshotPath = null
    }
}
