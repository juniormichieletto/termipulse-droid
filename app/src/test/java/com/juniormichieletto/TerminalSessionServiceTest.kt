package com.juniormichieletto

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.juniormichieletto.service.TerminalSessionService
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TerminalSessionServiceTest {

    @Test
    fun testServiceLifecycleAndNotification() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Test static helpers
        TerminalSessionService.startService(context, 2, true)
        TerminalSessionService.stopService(context)

        // Test controller lifecycle
        val controller = Robolectric.buildService(TerminalSessionService::class.java)
        val service = controller.create().get()

        val intent = Intent(context, TerminalSessionService::class.java).apply {
            putExtra("ACTIVE_SESSIONS", 3)
            putExtra("HAS_LONG_JOB", true)
        }

        service.onStartCommand(intent, 0, 1)
        service.updateNotificationState(3, false)

        val binder = service.onBind(intent)
        assertNotNull(binder)

        controller.destroy()
    }
}
