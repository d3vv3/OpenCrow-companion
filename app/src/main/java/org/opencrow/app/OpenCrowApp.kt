package org.opencrow.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.opencrow.app.di.AppContainer
import org.opencrow.app.heartbeat.HeartbeatScheduler

class OpenCrowApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Re-schedule heartbeat on startup using the server config.
        // WorkManager survives process restarts once enqueued, but a fresh
        // install or data-cleared scenario would never fire without this.
        appScope.launch {
            try {
                container.apiClient.initialize()
                if (!container.apiClient.isConfigured) return@launch

                val resp = container.apiClient.api.getHeartbeatConfig()
                val cfg = (if (resp.isSuccessful) resp.body() else null) ?: return@launch

                if (cfg.enabled) {
                    val mins = maxOf(15, cfg.intervalSeconds / 60)
                    HeartbeatScheduler.schedule(this@OpenCrowApp, mins)
                } else {
                    HeartbeatScheduler.cancel(this@OpenCrowApp)
                }
            } catch (e: Exception) {
                Log.w("OpenCrowApp", "Could not fetch heartbeat config at startup: ${e.message}")
            }
        }
    }
}
