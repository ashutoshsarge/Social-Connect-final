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

  @Test
  fun `userSessionManager initial landing requires login`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.data.local.UserSessionManager(context)
    sessionManager.logout()

    val result = sessionManager.checkAndResumeSession()
    org.junit.Assert.assertTrue(result is com.example.data.local.UserSessionResult.NoSession)
  }

  @Test
  fun `userSessionManager saves credentials and auto-logs out after threshold`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.data.local.UserSessionManager(context)
    val testUser = com.example.data.local.entity.UserEntity(
      id = 101,
      email = "security.test@socialconnect.org",
      passwordHash = "securePass123",
      fullName = "Security Tester",
      role = "VOLUNTEER"
    )

    // 1. Initial Login: Session 1
    sessionManager.saveLoginSession(testUser, "securePass123")
    assertEquals(1, sessionManager.getLoginCount())

    // 2. Resume Session 2
    val res2 = sessionManager.checkAndResumeSession()
    org.junit.Assert.assertTrue(res2 is com.example.data.local.UserSessionResult.ActiveSession)
    assertEquals(2, (res2 as com.example.data.local.UserSessionResult.ActiveSession).loginCount)

    // 3. Resume Session 3
    val res3 = sessionManager.checkAndResumeSession()
    assertEquals(3, (res3 as com.example.data.local.UserSessionResult.ActiveSession).loginCount)

    // 4. Resume Session 4
    val res4 = sessionManager.checkAndResumeSession()
    assertEquals(4, (res4 as com.example.data.local.UserSessionResult.ActiveSession).loginCount)

    // 5. Resume Session 5
    val res5 = sessionManager.checkAndResumeSession()
    assertEquals(5, (res5 as com.example.data.local.UserSessionResult.ActiveSession).loginCount)

    // 6. Next attempt reaches threshold -> Triggers Security Auto-Logout!
    val res6 = sessionManager.checkAndResumeSession()
    org.junit.Assert.assertTrue(res6 is com.example.data.local.UserSessionResult.SecurityAutoLogout)
    val autoLogout = res6 as com.example.data.local.UserSessionResult.SecurityAutoLogout
    org.junit.Assert.assertTrue(autoLogout.message.contains("expired after 5 logins"))
    assertEquals("security.test@socialconnect.org", autoLogout.expiredEmail)
  }
}
