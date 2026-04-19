package org.opencrow.app.assist

import android.service.voice.VoiceInteractionService

/**
 * Registered as the digital assistant service. The system calls this to create
 * voice interaction sessions when the user invokes the assist gesture.
 */
class OpenCrowAssistService : VoiceInteractionService()
