package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.safeColorFromLong
import com.example.data.local.toSafeArgbLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sitar", appName)
  }

  @Test
  fun `test safeColorFromLong does not crash on color operations`() {
    val sampleColor = Color(0xFF263859)
    val storedLong = sampleColor.toSafeArgbLong()
    val restoredColor = safeColorFromLong(storedLong)

    // Accessing red, green, blue or comparing should never throw
    assertNotNull(restoredColor.red)
    assertNotNull(restoredColor.green)
    assertNotNull(restoredColor.blue)
    val darker = com.example.data.MediaRepository.getDarkerVariant(restoredColor)
    assertNotNull(darker)
  }
}

