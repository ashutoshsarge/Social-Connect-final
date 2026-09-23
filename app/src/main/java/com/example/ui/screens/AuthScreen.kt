package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.service.FirebaseAuthService
import com.example.ui.components.SocialConnectLogo
import com.example.ui.theme.SocialCreamBg
import com.example.ui.theme.SocialCreamBorder
import com.example.ui.theme.SocialCreamCard
import com.example.ui.theme.SocialCreamSubtle
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialLogoNavy
import com.example.ui.theme.SocialTeal
import com.example.ui.theme.SocialTealLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.theme.SocialWarm
import com.example.ui.theme.SocialWarmLight
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay

/**
 * Modern, aesthetic authentication screen featuring:
 * - Two dedicated sections: Login and Register
 * - Google Authorization integration for sign in and dispatching OTP verification
 * - High-contrast readable typed text on creamy and white containers
 * - Uncropped official SocialConnect logo
 * - Isolated One-Tap Demo logins for Volunteer and NGO Leader
 */
@Composable
fun AuthScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val allUsers by viewModel.allUsers.collectAsState()

    // Exactly 2 sections: 0 = Login, 1 = Register
    var selectedTab by remember { mutableIntStateOf(0) }

    // Login state
    var email by remember { mutableStateOf("diyasarge@gmail.com") }
    var password by remember { mutableStateOf("seva123") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register state
    var regFullName by remember { mutableStateOf("Diya Sarge") }
    var regEmail by remember { mutableStateOf("diyasarge@gmail.com") }
    var regPhone by remember { mutableStateOf("+91 98765 43210") }
    var regPassword by remember { mutableStateOf("seva123") }
    var regRole by remember { mutableStateOf("VOLUNTEER") } // "VOLUNTEER" or "NGO_LEADER"
    var regOrg by remember { mutableStateOf("Delhi Youth Volunteers") }

    // Google Authorization & OTP verification state
    var isOtpVerificationActive by remember { mutableStateOf(false) }
    var otpSourceMode by remember { mutableStateOf("GOOGLE") } // "GOOGLE" or "MANUAL"
    var otpTargetEmail by remember { mutableStateOf("diyasarge@gmail.com") }
    var otpTargetPhone by remember { mutableStateOf("+91 98765 43210") }
    var otpCodeInput by remember { mutableStateOf("") }
    var otpVerificationId by remember { mutableStateOf<String?>(null) }
    var otpCodeHint by remember { mutableStateOf<String?>(null) }
    var otpTimerSeconds by remember { mutableIntStateOf(0) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Countdown timer for OTP resend
    LaunchedEffect(otpTimerSeconds) {
        if (otpTimerSeconds > 0) {
            delay(1000L)
            otpTimerSeconds -= 1
        }
    }

    // Standard high-contrast text field styling
    val authInputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF0F172A),
        unfocusedTextColor = Color(0xFF0F172A),
        cursorColor = SocialGreen,
        focusedBorderColor = SocialGreen,
        unfocusedBorderColor = SocialCreamBorder,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFFCFBF9)
    )

    val authTextStyle = TextStyle(
        color = Color(0xFF0F172A),
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SocialCreamBg)
            .verticalScroll(scrollState)
    ) {
        // Soft ambient warm lighting gradient at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEFE8DA),
                            Color(0xFFF7F3EA),
                            SocialCreamBg
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo with uncropped emblem & typography
            SocialConnectLogo(
                emblemSize = 92.dp,
                showText = true,
                textFontSize = 26,
                textColor = SocialLogoNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Civic Action & Verified Social Drives",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SocialGreenDark,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // One-Tap Demo Access Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x26059669)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SocialCreamBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "INSTANT DEMO PROFILES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SocialGreenDark,
                            letterSpacing = 0.8.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SocialGreenUltraLight
                        ) {
                            Text(
                                text = "Strict Role Access",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialGreenDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Demo 1: Volunteer
                        Surface(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                viewModel.loginAsVolunteerDemo {
                                    isLoading = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("demo_volunteer_button"),
                            shape = RoundedCornerShape(14.dp),
                            color = SocialGreenUltraLight,
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SocialGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = "Volunteer Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Volunteer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SocialGreenDark)
                                    Text("Diya Sarge", fontSize = 10.sp, color = SocialTextMuted)
                                }
                            }
                        }

                        // Demo 2: NGO Leader
                        Surface(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                viewModel.loginAsNgoDemo {
                                    isLoading = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("demo_ngo_button"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFCCFBF1),
                            border = BorderStroke(1.dp, Color(0xFF5EEAD4))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0D9488)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "NGO Leader Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("NGO Leader", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                                    Text("Aarav Patel", fontSize = 10.sp, color = SocialTextMuted)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Auth Card with Exactly 2 Sections (Login & Register)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x1A0B2035)),
                colors = CardDefaults.cardColors(containerColor = SocialCreamCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SocialCreamBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Two-section Tab Row: 0 = Login, 1 = Register
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = SocialCreamSubtle,
                        contentColor = SocialGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .padding(3.dp),
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .clip(RoundedCornerShape(12.dp)),
                                color = SocialGreen,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                isOtpVerificationActive = false
                                errorMessage = null
                                successNotice = null
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("tab_sign_in"),
                            text = {
                                Text(
                                    "Sign In",
                                    fontSize = 13.5.sp,
                                    color = if (selectedTab == 0) SocialGreenDark else SocialTextMuted,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                isOtpVerificationActive = false
                                errorMessage = null
                                successNotice = null
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("tab_register"),
                            text = {
                                Text(
                                    "Register",
                                    fontSize = 13.5.sp,
                                    color = if (selectedTab == 1) SocialGreenDark else SocialTextMuted,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Notice Banner
                    AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                        errorMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(text = msg, color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Success Notice Banner
                    AnimatedVisibility(visible = successNotice != null, enter = fadeIn(), exit = fadeOut()) {
                        successNotice?.let { notice ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = notice, color = SocialGreenDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Active OTP Confirmation Box (Shared for Google Auth and Manual OTP)
                    if (isOtpVerificationActive) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SocialGreenUltraLight,
                            border = BorderStroke(1.5.dp, SocialGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify 6-Digit Code", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SocialGreenDark)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "A secure verification code was sent via Google Authorization to $otpTargetEmail",
                                    fontSize = 12.sp,
                                    color = SocialTextMain
                                )

                                otpCodeHint?.let { hint ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                                    ) {
                                        Text(
                                            text = "Verification code: $hint",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SocialGreenDark,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = otpCodeInput,
                                    onValueChange = {
                                        if (it.length <= 6) {
                                            otpCodeInput = it
                                            errorMessage = null
                                        }
                                    },
                                    placeholder = { Text("Enter 6-digit OTP", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(18.dp))
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("auth_otp_code_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = authInputColors,
                                    textStyle = authTextStyle,
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (otpCodeInput.trim().length != 6) {
                                            errorMessage = "Please enter the complete 6-digit code"
                                            return@Button
                                        }
                                        isLoading = true
                                        errorMessage = null

                                        FirebaseAuthService.verifyOtp(
                                            code = otpCodeInput.trim(),
                                            verificationId = otpVerificationId,
                                            onSuccess = {
                                                if (selectedTab == 0) {
                                                    // Login existing or register fallback
                                                    viewModel.login(otpTargetEmail, "google_verified_auth") { success, _ ->
                                                        if (!success) {
                                                            viewModel.register(
                                                                email = otpTargetEmail,
                                                                pass = "google_verified_auth",
                                                                name = if (otpTargetEmail.contains("diya", true)) "Diya Sarge" else "Verified Changemaker",
                                                                role = "VOLUNTEER",
                                                                org = "SocialConnect Member",
                                                                phone = otpTargetPhone
                                                            ) { _, _ -> }
                                                        }
                                                        isLoading = false
                                                    }
                                                } else {
                                                    // Register completed
                                                    viewModel.register(
                                                        email = regEmail.trim(),
                                                        pass = regPassword.trim(),
                                                        name = regFullName.trim(),
                                                        role = regRole,
                                                        org = regOrg.trim(),
                                                        phone = regPhone.trim()
                                                    ) { success, err ->
                                                        isLoading = false
                                                        if (!success) {
                                                            errorMessage = err ?: "Registration failed"
                                                        }
                                                    }
                                                }
                                            },
                                            onError = { err ->
                                                isLoading = false
                                                errorMessage = err
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Confirm & Authorize", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (otpTimerSeconds > 0) "Resend in ${otpTimerSeconds}s" else "Resend Code",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (otpTimerSeconds > 0) SocialTextMuted else SocialGreen,
                                        modifier = Modifier.clickable(enabled = otpTimerSeconds == 0) {
                                            FirebaseAuthService.sendGoogleAuthorizationOtp(
                                                googleEmail = otpTargetEmail,
                                                onCodeSent = { hint, vId ->
                                                    otpVerificationId = vId
                                                    otpCodeHint = hint
                                                    otpTimerSeconds = 30
                                                    successNotice = "New code sent to $otpTargetEmail"
                                                },
                                                onError = { errorMessage = it }
                                            )
                                        }
                                    )

                                    Text(
                                        text = "Cancel",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFDC2626),
                                        modifier = Modifier.clickable {
                                            isOtpVerificationActive = false
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 0: LOGIN
                    if (selectedTab == 0 && !isOtpVerificationActive) {
                        // Google Authorization Button at top of login
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                successNotice = null

                                FirebaseAuthService.authenticateWithGoogle(
                                    activity = activity,
                                    onSuccess = { gEmail, gName ->
                                        otpTargetEmail = gEmail
                                        FirebaseAuthService.sendGoogleAuthorizationOtp(
                                            googleEmail = gEmail,
                                            onCodeSent = { hint, vId ->
                                                isLoading = false
                                                otpVerificationId = vId
                                                otpCodeHint = hint
                                                otpTimerSeconds = 45
                                                isOtpVerificationActive = true
                                                successNotice = "Google Authorization verified! OTP sent to $gEmail"
                                            },
                                            onError = { err ->
                                                isLoading = false
                                                errorMessage = err
                                            }
                                        )
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google Authorization",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                            Text(
                                text = "or sign in with password",
                                fontSize = 11.sp,
                                color = SocialTextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Email Address",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            placeholder = { Text("name@example.com", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email", tint = SocialGreen, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = authInputColors,
                            textStyle = authTextStyle,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Password",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("Enter your password", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password", tint = SocialGreen, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = SocialTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = authInputColors,
                            textStyle = authTextStyle,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both email and password"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                viewModel.login(email.trim(), password.trim()) { success, err ->
                                    isLoading = false
                                    if (!success) {
                                        errorMessage = err ?: "Invalid credentials. Try demo logins above!"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .testTag("auth_sign_in_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(text = "Sign In to SocialConnect", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick OTP Login Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (email.isNotBlank()) {
                                        otpTargetEmail = email.trim()
                                    }
                                    isLoading = true
                                    FirebaseAuthService.sendOtp(
                                        target = otpTargetEmail,
                                        isPhone = false,
                                        activity = activity,
                                        onCodeSent = { hint, vId ->
                                            isLoading = false
                                            otpVerificationId = vId
                                            otpCodeHint = hint
                                            otpTimerSeconds = 45
                                            isOtpVerificationActive = true
                                            successNotice = "Verification code sent to $otpTargetEmail"
                                        },
                                        onError = {
                                            isLoading = false
                                            errorMessage = it
                                        }
                                    )
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign In via OTP Verification Code",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialGreenDark
                            )
                        }
                    }

                    // SECTION 1: REGISTER
                    if (selectedTab == 1 && !isOtpVerificationActive) {
                        // Google Quick Register Button
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                successNotice = null

                                FirebaseAuthService.authenticateWithGoogle(
                                    activity = activity,
                                    onSuccess = { gEmail, gName ->
                                        regEmail = gEmail
                                        regFullName = gName
                                        otpTargetEmail = gEmail
                                        FirebaseAuthService.sendGoogleAuthorizationOtp(
                                            googleEmail = gEmail,
                                            onCodeSent = { hint, vId ->
                                                isLoading = false
                                                otpVerificationId = vId
                                                otpCodeHint = hint
                                                otpTimerSeconds = 45
                                                isOtpVerificationActive = true
                                                successNotice = "Google Account verified! Enter OTP to finalize registration"
                                            },
                                            onError = { err ->
                                                isLoading = false
                                                errorMessage = err
                                            }
                                        )
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign up with Google Authorization",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                            Text(
                                text = "or create account with form",
                                fontSize = 11.sp,
                                color = SocialTextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Role Selection
                        Text(
                            text = "Account Role",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (regRole == "VOLUNTEER") SocialGreenUltraLight else SocialCreamSubtle,
                                border = BorderStroke(1.5.dp, if (regRole == "VOLUNTEER") SocialGreen else SocialCreamBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { regRole = "VOLUNTEER" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = if (regRole == "VOLUNTEER") SocialGreenDark else SocialTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Volunteer",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (regRole == "VOLUNTEER") SocialGreenDark else SocialTextMuted
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (regRole == "NGO_LEADER") Color(0xFFCCFBF1) else SocialCreamSubtle,
                                border = BorderStroke(1.5.dp, if (regRole == "NGO_LEADER") Color(0xFF0F766E) else SocialCreamBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { regRole = "NGO_LEADER" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = if (regRole == "NGO_LEADER") Color(0xFF0F766E) else SocialTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "NGO Leader",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (regRole == "NGO_LEADER") Color(0xFF0F766E) else SocialTextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Full Name",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            placeholder = { Text("Your full name", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_reg_name_input"),
                            singleLine = true,
                            colors = authInputColors,
                            textStyle = authTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Email Address",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            placeholder = { Text("name@example.com", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_reg_email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = authInputColors,
                            textStyle = authTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Phone Number",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            placeholder = { Text("+91 98765 43210", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = authInputColors,
                            textStyle = authTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Password",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            placeholder = { Text("Create a secure password", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_reg_password_input"),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = authInputColors,
                            textStyle = authTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (regRole == "VOLUNTEER") "Associated Youth Club / Group" else "Registered NGO Name",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocialTextMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = regOrg,
                            onValueChange = { regOrg = it },
                            placeholder = { Text("Organization or club", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Business, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = authInputColors,
                            textStyle = authTextStyle
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    errorMessage = "Please fill in all required fields"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                otpTargetEmail = regEmail.trim()
                                otpTargetPhone = regPhone.trim()

                                // Send verification OTP via Google/Firebase Auth before final account activation
                                FirebaseAuthService.sendGoogleAuthorizationOtp(
                                    googleEmail = regEmail.trim(),
                                    onCodeSent = { hint, vId ->
                                        isLoading = false
                                        otpVerificationId = vId
                                        otpCodeHint = hint
                                        otpTimerSeconds = 45
                                        isOtpVerificationActive = true
                                        successNotice = "Verification code dispatched to ${regEmail.trim()}"
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .testTag("auth_create_account_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Send Verification OTP & Create Account", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
