package com.juniormichieletto

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.juniormichieletto.terminal.TerminalTab
import com.juniormichieletto.ui.terminal.TerminalTabBar
import com.juniormichieletto.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTabs = listOf(
      TerminalTab(title = "Local Sandbox", hostLabel = "termipulse@127.0.0.1", username = "termipulse", host = "127.0.0.1"),
      TerminalTab(title = "Production DB", hostLabel = "ubuntu@db.prod.net", username = "ubuntu", host = "db.prod.net", badgeColorHex = "#00E5FF")
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        TerminalTabBar(
          tabs = sampleTabs,
          activeTabIndex = 0,
          onTabSelected = {},
          onTabClosed = {},
          onAddTabClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

