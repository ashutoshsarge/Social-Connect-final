package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SocialCreamBorder
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted

/**
 * Cookie and Local Storage Consent Component.
 *
 * Fully compliant with India's Digital Personal Data Protection (DPDP) Act 2023,
 * ePrivacy, and international privacy standards.
 *
 * Implements granular preference controls:
 * 1. Strictly Necessary (Always active - auth tokens, CSRF/security, offline DB)
 * 2. Functional & UI Preferences (City, cause filters, theme)
 * 3. Anonymous Telemetry & Analytics (Opt-in performance and error reporting)
 */
@Composable
fun CookieConsentBanner(
    isVisible: Boolean,
    onAcceptAll: () -> Unit,
    onRejectNonEssential: () -> Unit,
    onSaveCustomPreferences: (functional: Boolean, analytics: Boolean) -> Unit,
    onViewPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomizer by remember { mutableStateOf(false) }
    var functionalEnabled by remember { mutableStateOf(true) }
    var analyticsEnabled by remember { mutableStateOf(false) } // Opt-in by default for privacy compliance

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .testTag("cookie_consent_banner"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SocialCreamBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with Privacy Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cookie,
                                contentDescription = "Cookie & Local Storage Preferences",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cookie & Storage Privacy Notice",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialTextMain
                            )
                            Text(
                                text = "Compliant with India's DPDP Act 2023",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = SocialGreenDark
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SocialGreenUltraLight,
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Text(
                            text = "DPDP 2023",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialGreenDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "We use local storage and privacy-respecting tokens to secure your sessions, remember volunteer cause preferences, and ensure app stability. We do not sell your personal data or track you across third-party websites.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewPolicy() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SocialGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Read our comprehensive Cookie & Local Storage Policy",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SocialGreen,
                        textDecoration = TextDecoration.Underline
                    )
                }

                // Granular Preference Customizer (Expandable)
                if (showCustomizer) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Customize Storage Categories",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Strictly Necessary
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = SocialGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Strictly Necessary",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SocialTextMain
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(Always Active)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SocialGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Required for secure user authentication, OTP verification, and basic session state.",
                                    fontSize = 10.5.sp,
                                    color = SocialTextMuted
                                )
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = null, // Disabled: always active
                                enabled = false,
                                colors = SwitchDefaults.colors(
                                    disabledCheckedThumbColor = SocialGreen,
                                    disabledCheckedTrackColor = Color(0xFFBBF7D0)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Functional & Preferences
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Functional Preferences",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocialTextMain
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Remembers your chosen city, volunteer causes (Environment, Education), and search filters.",
                                    fontSize = 10.5.sp,
                                    color = SocialTextMuted
                                )
                            }
                            Switch(
                                checked = functionalEnabled,
                                onCheckedChange = { functionalEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SocialGreen
                                ),
                                modifier = Modifier.testTag("toggle_functional_cookies")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Anonymous Telemetry & Analytics
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Anonymous Diagnostics & Analytics",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocialTextMain
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Helps our non-profit platform identify crashes and improve volunteer drive accessibility.",
                                    fontSize = 10.5.sp,
                                    color = SocialTextMuted
                                )
                            }
                            Switch(
                                checked = analyticsEnabled,
                                onCheckedChange = { analyticsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SocialGreen
                                ),
                                modifier = Modifier.testTag("toggle_analytics_cookies")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                if (!showCustomizer) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRejectNonEssential,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_reject_cookies"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                        ) {
                            Text(
                                text = "Reject Non-Essential",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onAcceptAll,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_accept_all_cookies"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocialGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Accept All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { showCustomizer = true },
                            modifier = Modifier.testTag("btn_customize_cookies")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = SocialGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Customize Preferences",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SocialGreen
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCustomizer = false },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                        ) {
                            Text("Back", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                onSaveCustomPreferences(functionalEnabled, analyticsEnabled)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp)
                                .testTag("btn_save_cookie_preferences"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocialGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save My Preferences", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
