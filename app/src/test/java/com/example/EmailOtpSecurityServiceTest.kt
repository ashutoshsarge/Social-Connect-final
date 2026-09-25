package com.example

import com.example.data.service.EmailOtpSecurityService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmailOtpSecurityServiceTest {

    private val testEmail = "security.test@example.com"

    @Before
    fun setUp() {
        EmailOtpSecurityService.cancelSession(testEmail)
    }

    @Test
    fun testSendOtpGeneratesSessionAndEnforcesFormat() {
        var generatedSession: EmailOtpSecurityService.OtpSession? = null
        var errorMsg: String? = null

        EmailOtpSecurityService.sendOtp(
            email = testEmail,
            context = null,
            dispatchEmailGateway = false,
            onSuccess = { session -> generatedSession = session },
            onError = { err -> errorMsg = err }
        )

        assertNotNull(generatedSession)
        assertEquals(testEmail, generatedSession?.email)
        assertEquals(6, generatedSession?.previewCodeHint?.length)
        assertTrue(generatedSession?.previewCodeHint?.all { it.isDigit() } == true)
        assertEquals(5, generatedSession?.attemptsRemaining)
        assertFalse(generatedSession?.isLocked == true)
    }

    @Test
    fun testInvalidEmailRejected() {
        var errorMsg: String? = null

        EmailOtpSecurityService.sendOtp(
            email = "invalid-email-string",
            context = null,
            dispatchEmailGateway = false,
            onSuccess = {},
            onError = { err -> errorMsg = err }
        )

        assertNotNull(errorMsg)
        assertTrue(errorMsg?.contains("valid email", ignoreCase = true) == true)
    }

    @Test
    fun testSuccessfulVerificationConsumesSession() {
        var session: EmailOtpSecurityService.OtpSession? = null
        EmailOtpSecurityService.sendOtp(
            email = testEmail,
            context = null,
            dispatchEmailGateway = false,
            onSuccess = { session = it },
            onError = {}
        )

        val code = session?.previewCodeHint ?: "123456"
        var verifySuccess = false

        EmailOtpSecurityService.verifyOtp(
            email = testEmail,
            code = code,
            onResult = { res ->
                if (res is EmailOtpSecurityService.VerificationResult.Success) {
                    verifySuccess = true
                }
            }
        )

        assertTrue(verifySuccess)
        // Session should be consumed
        assertEquals(null, EmailOtpSecurityService.getSession(testEmail))
    }

    @Test
    fun testBruteForceLockoutAfter5FailedAttempts() {
        var session: EmailOtpSecurityService.OtpSession? = null
        EmailOtpSecurityService.sendOtp(
            email = testEmail,
            context = null,
            dispatchEmailGateway = false,
            onSuccess = { session = it },
            onError = {}
        )

        // Attempt 1 to 4: Wrong code
        for (attempt in 1..4) {
            EmailOtpSecurityService.verifyOtp(
                email = testEmail,
                code = "000000",
                onResult = { res ->
                    assertTrue(res is EmailOtpSecurityService.VerificationResult.Error)
                    val err = res as EmailOtpSecurityService.VerificationResult.Error
                    assertEquals(5 - attempt, err.attemptsLeft)
                    assertFalse(err.isLocked)
                }
            )
        }

        // Attempt 5: Final wrong attempt triggers lockout
        EmailOtpSecurityService.verifyOtp(
            email = testEmail,
            code = "000000",
            onResult = { res ->
                assertTrue(res is EmailOtpSecurityService.VerificationResult.Error)
                val err = res as EmailOtpSecurityService.VerificationResult.Error
                assertEquals(0, err.attemptsLeft)
                assertTrue(err.isLocked)
            }
        )

        // Subsequent attempt with correct code should still fail because session is locked
        val realCode = session?.previewCodeHint ?: "123456"
        EmailOtpSecurityService.verifyOtp(
            email = testEmail,
            code = realCode,
            onResult = { res ->
                assertTrue(res is EmailOtpSecurityService.VerificationResult.Error)
                val err = res as EmailOtpSecurityService.VerificationResult.Error
                assertTrue(err.isLocked)
            }
        )
    }

    @Test
    fun testRateLimitingCooldownEnforced() {
        var errorOccurred = false

        EmailOtpSecurityService.sendOtp(
            email = testEmail,
            context = null,
            dispatchEmailGateway = false,
            onSuccess = {},
            onError = {}
        )

        // Immediate second dispatch should be blocked by 60s cooldown
        EmailOtpSecurityService.sendOtp(
            email = testEmail,
            context = null,
            dispatchEmailGateway = false,
            onSuccess = {},
            onError = { err ->
                if (err.contains("seconds before requesting another code", ignoreCase = true)) {
                    errorOccurred = true
                }
            }
        )

        assertTrue(errorOccurred)
    }
}
