package com.example.ui.screens

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
    val clipboardManager = LocalClipboardManager.current
    val allUsers by viewModel.allUsers.collectAsState()

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Exactly 2 sections: 0 = Sign In, 1 = Sign Up
    var selectedTab by remember { mutableIntStateOf(0) }

    val prefilledEmail by viewModel.prefilledAuthEmail.collectAsState()
    val authNotice by viewModel.authSuccessNotice.collectAsState()
    val securityNotice by viewModel.securityLogoutMessage.collectAsState()

    // Login state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Forgot password state
    var showForgotPasswordModal by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var isResetLoading by remember { mutableStateOf(false) }
    var resetStatusMessage by remember { mutableStateOf<String?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }

    // Register state
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf("VOLUNTEER") } // "VOLUNTEER" or "NGO_LEADER"
    var regOrg by remember { mutableStateOf("") }
    var regConsentAgreed by remember { mutableStateOf(false) } // Form Consent for DPDP Act 2023
    var showLegalModalByTab by remember { mutableStateOf<Int?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Synchronize prefilled email and registration success notice
    LaunchedEffect(prefilledEmail) {
        if (!prefilledEmail.isNullOrBlank()) {
            email = prefilledEmail!!
        }
    }

    LaunchedEffect(authNotice) {
        if (!authNotice.isNullOrBlank()) {
            successNotice = authNotice
            selectedTab = 0
        }
    }

    LaunchedEffect(securityNotice) {
        if (!securityNotice.isNullOrBlank()) {
            selectedTab = 0
        }
    }

    val scrollState = rememberScrollState()

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
                                    Text("Rahul Nile", fontSize = 10.sp, color = SocialTextMuted)
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
                                    Text("Dr. Aarav Patel", fontSize = 10.sp, color = SocialTextMuted)
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
                                errorMessage = null
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
                                errorMessage = null
                                successNotice = null
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("tab_register"),
                            text = {
                                Text(
                                    "Sign Up",
                                    fontSize = 13.5.sp,
                                    color = if (selectedTab == 1) SocialGreenDark else SocialTextMuted,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Automated Security Logout Banner (After 5 Logins)
                    AnimatedVisibility(visible = securityNotice != null, enter = fadeIn(), exit = fadeOut()) {
                        securityNotice?.let { sNotice ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Security Alert",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = sNotice,
                                        color = Color(0xFF92400E),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearSecurityLogoutMessage() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Dismiss",
                                            tint = Color(0xFF92400E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

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

                    // SECTION 0: LOGIN - Clean standard Sign In panel with Google Sign-In and Forgot Password
                    if (selectedTab == 0) {
                        // Google Sign-In button
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                successNotice = null

                                viewModel.signInWithGoogle(
                                    activity = activity,
                                    preferredEmail = email.trim().ifBlank { null }
                                ) { success, err ->
                                    isLoading = false
                                    if (!success) {
                                        errorMessage = err ?: "Google authentication could not be completed"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .testTag("auth_google_sign_in_btn"),
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
                                text = "Sign in with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                            Text(
                                text = "or sign in with email",
                                fontSize = 11.5.sp,
                                color = SocialTextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Address Input
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

                        // Password label with Forgot Password link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialTextMain
                            )
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialGreen,
                                modifier = Modifier
                                    .clickable {
                                        resetEmailInput = email.trim()
                                        resetStatusMessage = null
                                        resetErrorMessage = null
                                        showForgotPasswordModal = true
                                    }
                                    .testTag("auth_forgot_password_btn")
                            )
                        }
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

                        Spacer(modifier = Modifier.height(20.dp))

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
                                        errorMessage = err ?: "Invalid credentials. Please verify your email and password."
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
                                Text(text = "Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // SECTION 1: SIGN UP
                    if (selectedTab == 1) {
                        // Google Quick Register with Firebase Auth & Firestore Persistence
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                successNotice = null

                                viewModel.signInWithGoogle(activity = activity) { success, err ->
                                    isLoading = false
                                    if (!success) {
                                        errorMessage = err ?: "Google authentication could not be completed"
                                    }
                                }
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
                                text = "Sign up with Google",
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Explicit Form Consent Checkbox (DPDP Act 2023 Compliant)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (regConsentAgreed) Color(0xFFA7F3D0) else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { regConsentAgreed = !regConsentAgreed }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = regConsentAgreed,
                                    onCheckedChange = { regConsentAgreed = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SocialGreen,
                                        uncheckedColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.testTag("cb_registration_consent")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Statutory Consent & Agreement (DPDP Act 2023)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SocialTextMain
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "I explicitly agree to the Terms of Service and acknowledge the Privacy Policy. I consent to SocialConnect Foundation processing my identity and contact information strictly for volunteer drive management and safety coordination pursuant to India's DPDP Act 2023.",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 14.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Privacy Policy",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SocialGreen,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier.clickable { showLegalModalByTab = 0 }
                                        )
                                        Text(
                                            text = "Terms of Service",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SocialGreen,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier.clickable { showLegalModalByTab = 1 }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    errorMessage = "Please enter your name, email, and password"
                                    return@Button
                                }
                                if (!regConsentAgreed) {
                                    errorMessage = "Please check the consent box to agree to the Terms & Privacy Policy."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null

                                viewModel.register(
                                    email = regEmail.trim(),
                                    pass = regPassword.trim(),
                                    name = regFullName.trim(),
                                    role = regRole,
                                    org = regOrg.trim(),
                                    phone = regPhone.trim()
                                ) { success, err ->
                                    isLoading = false
                                    if (success) {
                                        val registeredEmail = regEmail.trim()
                                        email = registeredEmail
                                        selectedTab = 0
                                        errorMessage = null
                                        successNotice = "Account created successfully! Check your email to verify your account or sign in below."
                                        regPassword = ""
                                    } else {
                                        errorMessage = err ?: "Registration failed. Please try again."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .testTag("auth_create_account_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White),
                            enabled = !isLoading && regConsentAgreed
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Footer Legal & Compliance Policy Links
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy Policy",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialGreen,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showLegalModalByTab = 0 }
                        .padding(4.dp)
                )
                Text(text = " • ", fontSize = 11.sp, color = SocialTextMuted)
                Text(
                    text = "Terms of Service",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialGreen,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showLegalModalByTab = 1 }
                        .padding(4.dp)
                )
                Text(text = " • ", fontSize = 11.sp, color = SocialTextMuted)
                Text(
                    text = "Cookies",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialGreen,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showLegalModalByTab = 2 }
                        .padding(4.dp)
                )
                Text(text = " • ", fontSize = 11.sp, color = SocialTextMuted)
                Text(
                    text = "Refunds",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialGreen,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showLegalModalByTab = 3 }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏢 Corporate & Grievance Officer Details",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showLegalModalByTab = 4 }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "SocialConnect Foundation • CIN: U85300DL2024NPL419820 • DPDP Act 2023 Compliant",
                fontSize = 10.sp,
                color = SocialTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Legal Policies & Compliance Modal
        if (showLegalModalByTab != null) {
            LegalPoliciesModal(
                initialTab = showLegalModalByTab!!,
                viewModel = viewModel,
                onDismiss = { showLegalModalByTab = null }
            )
        }

        // Forgot Password Recovery Modal
        if (showForgotPasswordModal) {
            AlertDialog(
                onDismissRequest = {
                    if (!isResetLoading) showForgotPasswordModal = false
                },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SocialGreenUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LockReset,
                                contentDescription = null,
                                tint = SocialGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Reset Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialTextMain
                            )
                            Text(
                                text = "Account recovery",
                                fontSize = 12.sp,
                                color = SocialTextMuted
                            )
                        }
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered email address and we'll send you instructions to reset your password.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = resetEmailInput,
                            onValueChange = {
                                resetEmailInput = it
                                resetErrorMessage = null
                            },
                            placeholder = { Text("name@example.com", fontSize = 13.sp, color = Color(0xFFA8A29E)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = SocialGreen, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_password_email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = authInputColors,
                            textStyle = authTextStyle,
                            singleLine = true
                        )

                        if (resetErrorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = resetErrorMessage!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (resetStatusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = resetStatusMessage!!,
                                    color = Color(0xFF065F46),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetEmailInput.isBlank() || !resetEmailInput.contains("@")) {
                                resetErrorMessage = "Please enter a valid email address"
                                return@Button
                            }
                            isResetLoading = true
                            resetErrorMessage = null
                            resetStatusMessage = null
                            viewModel.sendPasswordReset(resetEmailInput.trim()) { success, msg ->
                                isResetLoading = false
                                if (success) {
                                    resetStatusMessage = msg ?: "Password recovery instructions sent to your email!"
                                } else {
                                    resetErrorMessage = msg ?: "Failed to send recovery email."
                                }
                            }
                        },
                        enabled = !isResetLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SocialGreen, contentColor = Color.White),
                        modifier = Modifier.testTag("send_recovery_email_btn")
                    ) {
                        if (isResetLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Send Recovery Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPasswordModal = false },
                        enabled = !isResetLoading
                    ) {
                        Text("Close", color = SocialTextMuted, fontWeight = FontWeight.SemiBold)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}
