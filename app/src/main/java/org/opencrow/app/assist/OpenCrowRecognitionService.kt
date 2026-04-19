package org.opencrow.app.assist

import android.content.Intent
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Stub RecognitionService required by the VoiceInteractionService framework.
 * The digital assistant picker won't properly register the app without a
 * recognitionService declared in the voice_interaction metadata.
 *
 * openCrow sends raw audio to the server's Whisper API for transcription,
 * so this service is a no-op.
 */
class OpenCrowRecognitionService : RecognitionService() {
    override fun onStartListening(intent: Intent?, callback: Callback?) {
        callback?.error(SpeechRecognizer.ERROR_SERVER)
    }

    override fun onCancel(callback: Callback?) {}

    override fun onStopListening(callback: Callback?) {}
}
