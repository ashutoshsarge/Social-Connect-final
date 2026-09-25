package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.EmailDispatchService
import com.example.ui.theme.SocialBgBody
import com.example.ui.theme.SocialCreamBorder
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmailVerificationPendingScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pendingEmail by viewModel.pendingVerificationEmail.collectAsState()
    val isChecking by viewModel.isCheckingVerification.collectAsState()

    var resendSuccessMsg by remember { mutableStateOf<String?>(null) }
    var resendErrorMsg by remember { mutableStateOf<String?>(null) }
    var isResending by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableStateOf(0) }

    val emailDisplay = pendingEmail ?: "your email address"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SocialBgBody)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Brand Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.cancelVerificationAndReturn() },
                    modifier = Modifier.testTag("verification_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = SocialTextMain
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SocialGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SocialConnect Security",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                }

                Spacer(modifier = Modifier.size(48.dp)) // balance
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Verification Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x26047857))
                    .testTag("email_verification_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SocialCreamBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Mail Icon Header
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SocialGreenUltraLight, Color(0xFFDCFCE7))
                                )
                            )
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = "Verify Email",
                            tint = SocialGreen,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Verify Your Email Address",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SocialTextMain,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A Firebase Auth verification link has been sent to:",
                        fontSize = 13.sp,
                        color = SocialTextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Email pill badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SocialGreenUltraLight,
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = SocialGreenDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = emailDisplay,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "To protect community integrity, SocialConnect requires email verification before accessing volunteer events, NGO portals, and certificates.",
                        fontSize = 12.5.sp,
                        color = SocialTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Feedback Messages
                    if (resendSuccessMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(resendSuccessMsg!!, fontSize = 12.sp, color = SocialGreenDark, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    if (resendErrorMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = resendErrorMsg!!,
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Open Email App Button
                    Button(
                        onClick = {
                            val opened = EmailDispatchService.openEmailInbox(context)
                            if (!opened) {
                                Toast.makeText(context, "Please open your email client to check your inbox.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_email_app_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Email Inbox",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Check Verification Status Button
                    Button(
                        onClick = {
                            viewModel.checkEmailVerificationStatus { verified ->
                                if (verified) {
                                    Toast.makeText(context, "Email verified successfully! Welcome to SocialConnect.", Toast.LENGTH_LONG).show()
                                } else {
                                    resendErrorMsg = "Your email is not verified yet. Please click the link in the email sent to $emailDisplay and try again."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("check_verification_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        ),
                        enabled = !isChecking
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking status...", fontSize = 13.5.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I've Verified — Check Status",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend Verification Email Button
                    OutlinedButton(
                        onClick = {
                            if (resendCooldown > 0 || isResending) return@OutlinedButton
                            isResending = true
                            resendSuccessMsg = null
                            resendErrorMsg = null

                            viewModel.resendVerificationEmail { success, msg ->
                                isResending = false
                                if (success) {
                                    resendSuccessMsg = msg ?: "Verification email sent! Check your inbox."
                                    resendCooldown = 60
                                    coroutineScope.launch {
                                        while (resendCooldown > 0) {
                                            delay(1000)
                                            resendCooldown--
                                        }
                                    }
                                } else {
                                    resendErrorMsg = msg ?: "Could not resend email. Please wait a moment."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("resend_verification_btn"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = SocialTextMain
                        ),
                        enabled = resendCooldown == 0 && !isResending
                    ) {
                        if (isResending) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SocialGreen, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (resendCooldown > 0) "Resend available in ${resendCooldown}s" else "Resend Verification Email",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (resendCooldown > 0) SocialTextMuted else SocialGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cancel / Use Different Account
                    OutlinedButton(
                        onClick = { viewModel.cancelVerificationAndReturn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("verification_switch_account_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = SocialTextMuted
                        )
                    ) {
                        Text(
                            text = "Log In with Another Account",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SocialTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instructions tips
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Verification Tips",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Check your Spam or Promotions tab if the email is not in your primary inbox.\n• The link is valid for 24 hours.\n• Once you click the link in your email, return here and tap \"I've Verified — Check Status\".",
                        fontSize = 11.5.sp,
                        color = SocialTextMuted,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
