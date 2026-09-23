package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("SocialConnect", appName)
  }

  @Test
  fun `verify task progress calculation`() {
    val totalSubtasks = 4
    val completedSubtasks = 3
    val progress = (completedSubtasks.toFloat() / totalSubtasks * 100).toInt()
    assertEquals(75, progress)
  }

  @Test
  fun `launch MainActivity successfully`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }
}
