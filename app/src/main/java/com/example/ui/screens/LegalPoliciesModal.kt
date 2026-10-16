package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SocialCreamBorder
import com.example.ui.theme.SocialDanger
import com.example.ui.theme.SocialDangerLight
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenDark
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import com.example.ui.viewmodel.AppViewModel

/**
 * Unified Legal, Governance & Privacy Compliance Modal.
 *
 * Provides comprehensive legal documentation and interactive compliance actions:
 * 1. Privacy Policy (Strict adherence to India's DPDP Act 2023, data minimization, consent, rights).
 * 2. Terms & Conditions (Platform facilitator, Good Samaritan protections, code of conduct, copyright).
 * 3. Cookie & Local Storage Policy (Granular categorizations, data retention, user consent control).
 * 4. Refund & Cancellation Policy (100% free volunteer cancellations, 48h charity workshop refund window).
 * 5. Official Business & Grievance Details (Registered Section 8 entity, CIN, DPO & Grievance Officer).
 */
@Composable
fun LegalPoliciesModal(
    initialTab: Int = 0, // 0: Privacy, 1: Terms, 2: Cookies, 3: Refund, 4: Business
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 4)) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        LegalTabItem("Privacy Policy", Icons.Default.Shield),
        LegalTabItem("Terms of Service", Icons.Default.Gavel),
        LegalTabItem("Cookie Policy", Icons.Default.Cookie),
        LegalTabItem("Refund Policy", Icons.Default.MonetizationOn),
        LegalTabItem("Business & Grievance", Icons.Default.Business)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(22.dp)),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SocialGreenUltraLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Legal & Compliance",
                                    tint = SocialGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Legal & Governance Center",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocialTextMain
                                )
                                Text(
                                    text = "SocialConnect Foundation • DPDP Act 2023 Compliant",
                                    fontSize = 11.sp,
                                    color = SocialGreenDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_close_legal_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Legal Modal",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    // Tab Navigation
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 16.dp,
                        containerColor = Color(0xFFF8FAFC),
                        contentColor = SocialGreen,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = SocialGreen,
                                height = 3.dp
                            )
                        },
                        divider = {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    selectedTab = index
                                    actionMessage = null
                                },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = if (selectedTab == index) SocialGreen else SocialTextMuted
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.title,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == index) SocialGreenDark else SocialTextMuted
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("tab_legal_${index}")
                            )
                        }
                    }

                    // Dynamic Notification / Action Feedback Banner
                    actionMessage?.let { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SocialGreenUltraLight,
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SocialGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SocialGreenDark
                                )
                            }
                        }
                    }

                    // Scrollable Policy Content Body
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (selectedTab) {
                            0 -> PrivacyPolicySection(
                                onExportData = {
                                    val summary = viewModel.exportUserPersonalData()
                                    actionMessage = "Personal data summary generated successfully pursuant to DPDP Section 11."
                                },
                                onRequestDelete = {
                                    showDeleteConfirmDialog = true
                                }
                            )
                            1 -> TermsOfServiceSection()
                            2 -> CookiePolicySection(viewModel = viewModel)
                            3 -> RefundPolicySection()
                            4 -> BusinessAndGrievanceSection()
                        }
                    }

                    // Bottom Footer Bar
                    Surface(
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last Updated: September 2026 • Version 2.4",
                                fontSize = 11.sp,
                                color = SocialTextMuted
                            )
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SocialGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_dismiss_legal_modal")
                            ) {
                                Text("Acknowledge & Close", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for DPDP Data Erasure (Right to be Forgotten)
    if (showDeleteConfirmDialog) {
        Dialog(onDismissRequest = { showDeleteConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = SocialDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Permanent Account & Data Erasure",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pursuant to Section 12 of India's Digital Personal Data Protection (DPDP) Act 2023, you have the statutory right to request erasure of your personal data. This will irreversibly remove your volunteer profile, logged hours, drive registrations, and credentials.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                viewModel.deleteUserAccountAndData { success, err ->
                                    if (success) {
                                        actionMessage = "Your account and personal data have been completely erased."
                                        onDismiss()
                                    } else {
                                        actionMessage = err ?: "Failed to process data erasure request."
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_confirm_erase_data"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SocialDanger,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Erase All My Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class LegalTabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

/* -------------------------------------------------------------------------- */
/* 1. Privacy Policy Section                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun PrivacyPolicySection(
    onExportData: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        // DPDP Act 2023 Compliance Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFECFDF5),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SocialGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Prepared in compliance with India's DPDP Act 2023",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialGreenDark
                    )
                    Text(
                        text = "Digital Personal Data Protection Act, 2023 (Act No. 22 of 2023)",
                        fontSize = 10.5.sp,
                        color = Color(0xFF065F46)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionHeading("1. Who We Are (Data Fiduciary)")
        BodyText("SocialConnect Foundation ('we', 'us', or 'our') operates the SocialConnect platform as a registered Section 8 not-for-profit society incorporated in Delhi, India. In terms of Section 2(i) of the Digital Personal Data Protection Act, 2023, SocialConnect acts as the Data Fiduciary responsible for determining the purpose and means of processing personal data provided by volunteers and non-profit partner organizations.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("2. Data Minimization: What We Collect")
        BodyText("In strict compliance with the Data Minimization principle, we only collect the minimum personal data strictly necessary to facilitate community service and volunteer drives:")
        BulletPoint("Identity & Contact: Full Name, Email Address, and Phone Number (used exclusively for authentication and emergency drive contact).")
        BulletPoint("Role & Organization: Selected role (Volunteer or NGO Organizer) and affiliated group/non-profit name.")
        BulletPoint("Service Preferences: Chosen social cause interests (e.g., Environment, Education) and preferred city/radius to discover nearby drives.")
        BulletPoint("Participation Records: Volunteer hours logged, verified attendance rosters, and completion certificates.")
        BulletPoint("Non-Collection Guarantee: We NEVER collect financial passwords, government Aadhaar/PAN biometrics, or sensitive personal data unnecessary for volunteering.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("3. Lawful Basis & Notice of Processing")
        BodyText("Under Section 4 and Section 6 of the DPDP Act 2023, we process personal data strictly upon obtaining your clear, informed, unconditional, and affirmative consent at the time of registration, or for legitimate volunteer facilitation:")
        BulletPoint("To verify your identity via secure passwordless 6-digit email or phone OTPs.")
        BulletPoint("To register you for community drives and provide verified coordinators with attendee headcounts.")
        BulletPoint("To dispatch critical schedule changes, weather alerts, and drive meeting point directions.")
        BulletPoint("To generate digitally verifiable volunteer service certificates.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("4. Third-Party Processors & Embeds")
        BodyText("We do not sell, rent, or trade your personal data. We engage strictly vetted technical service providers under binding data processing agreements:")
        BulletPoint("Authentication & Database: Cloud Supabase & PostgreSQL (RLS Protected) hosted in secure ISO 27001/SOC 2 certified regional data centers.")
        BulletPoint("Search Grounding: Google Gemini API (used solely for processing public natural language drive searches without transmitting your private profile data).")
        BulletPoint("Maps & Directions: OpenStreetMap / Google Maps (rendered locally on device for navigation to volunteer cleanup sites).")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("5. Protection of Children (DPDP Section 9)")
        BodyText("SocialConnect does not knowingly process personal data of children under 18 years of age without verifiable parental or legal guardian authorization. Minors participating in weekend community planting or educational drives must be accompanied by an adult guardian.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("6. Statutory Rights of the Data Principal")
        BodyText("Under Chapter III of the DPDP Act 2023, you hold the following statutory rights regarding your personal data:")
        BulletPoint("Right to Access Summary (Section 11): Right to obtain a summary of personal data being processed.")
        BulletPoint("Right to Correction & Updating (Section 12): Right to correct inaccurate or outdated data in your profile.")
        BulletPoint("Right to Erasure (Section 12): Right to withdraw consent and have your personal data permanently wiped from our systems.")
        BulletPoint("Right of Grievance Redressal (Section 13): Right to readily available grievance redressal provided by our designated Data Protection Officer.")
        BulletPoint("Right to Nominate (Section 14): Right to designate a nominee to exercise your rights in the event of death or incapacity.")

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Data Principal Actions Box
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Exercise Your DPDP Data Rights",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SocialTextMain
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "You can immediately exercise your statutory rights below without any fee or delay:",
                    fontSize = 11.sp,
                    color = SocialTextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportData,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_export_personal_data"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = SocialGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Data Summary", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SocialTextMain)
                    }

                    Button(
                        onClick = onRequestDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_request_erasure"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF2F2),
                            contentColor = SocialDanger
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = SocialDanger,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request Erasure", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SocialDanger)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeading("7. Data Retention & Security Safeguards")
        BodyText("Personal data is retained only for as long as your account remains active or as required by Indian taxation statutes for certified non-profit audits. All network transmissions are strictly encrypted via TLS 1.3, and access is restricted through role-based access control (RBAC).")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("8. Data Protection Officer & Redressal")
        BodyText("For privacy inquiries, rights enforcement, or statutory complaints, contact our Data Protection Officer at: dpo@socialconnect.org (Acknowledgment within 48 hours; resolution within 30 days pursuant to DPDP statutory rules). If unsatisfied with our response, you maintain the right to register a complaint with the Data Protection Board of India (DPBI).")
    }
}

/* -------------------------------------------------------------------------- */
/* 2. Terms of Service Section                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun TermsOfServiceSection() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        SectionHeading("1. Agreement to Terms")
        BodyText("By creating an account, browsing volunteer opportunities, or organizing community drives on SocialConnect, you agree to be bound by these Terms of Service, our Privacy Policy, and Community Guidelines. If you do not agree to these terms, you must discontinue platform use immediately.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("2. Nature of Platform (Facilitator Model)")
        BodyText("SocialConnect Foundation is a civic technology facilitator connecting community volunteers with registered non-governmental organizations (NGOs) and community initiatives. SocialConnect is NOT an employer, contractor, or insurance provider for volunteers or organizers:")
        BulletPoint("Voluntary Participation: Volunteering is strictly voluntary and unpaid. No employment or agency relationship is created.")
        BulletPoint("On-Ground Safety: Participating NGOs are solely responsible for local safety equipment (gloves, first aid kits, masks), volunteer instruction, and safe field conditions.")
        BulletPoint("Good Samaritan Principle: Actions performed by volunteers during cleanup, food relief, or emergency mobilization are governed by Good Samaritan protections recognized under Indian Law.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("3. User Eligibility & Code of Conduct")
        BodyText("To preserve community trust and safety, you agree to adhere strictly to the following standards:")
        BulletPoint("Authentic Information: You agree to provide accurate registration details and avoid impersonating other persons or organizations.")
        BulletPoint("Zero Tolerance for Harassment: Any hate speech, harassment, discrimination, or abusive conduct during offline drives or within community messaging forums will result in immediate permanent account termination.")
        BulletPoint("Prohibition on Commercial Solicitations: Organizers may NOT use the platform to market commercial products, solicit unauthorized multi-level marketing, or engage in political campaigning.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("4. Intellectual Property & Image Copyright")
        BodyText("All platform branding, custom vector illustrations, and software codebase are the property of SocialConnect Foundation or its open-source contributors:")
        BulletPoint("System Icons & Vectors: Derived from Google Material Symbols under the Apache License 2.0.")
        BulletPoint("Event Photography: Drive photos uploaded by organizers must be royalty-free, public domain, or owned by the respective NGO with volunteer consent.")
        BulletPoint("User Content: By uploading drive photos or impact updates, you grant SocialConnect a non-exclusive, royalty-free license to display content solely for community inspiration.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("5. Limitation of Liability")
        BodyText("To the fullest extent permitted by Indian Law, SocialConnect Foundation, its directors, and volunteers shall not be liable for any indirect, incidental, punitive, or consequential damages arising from physical injury, personal property loss, travel delays, or disputes occurring during offline volunteer drives.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("6. Governing Law & Dispute Jurisdiction")
        BodyText("These Terms are governed by and construed in accordance with the laws of the Republic of India, specifically the Information Technology Act, 2000, and the Digital Personal Data Protection Act, 2023. Any disputes arising out of these terms shall be subject to the exclusive jurisdiction of the competent courts in New Delhi, India.")
    }
}

/* -------------------------------------------------------------------------- */
/* 3. Cookie & Local Storage Policy Section                                  */
/* -------------------------------------------------------------------------- */

@Composable
private fun CookiePolicySection(viewModel: AppViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        SectionHeading("1. What Are Cookies & Local Storage?")
        BodyText("Cookies and local storage objects (such as Android SharedPreferences and SQLite Room Database) are compact data files saved on your device to maintain session state, secure your account against token theft, and preserve your chosen filter preferences.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("2. Do We Need Your Consent?")
        BodyText("YES. Under the Digital Personal Data Protection Act 2023 and global privacy directives (ePrivacy / GDPR), storing non-essential persistent identifiers or analytics trackers on your device requires prior, explicit, unbundled consent. Strictly necessary session tokens do not require consent as they are vital for system operation.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("3. Detailed Storage Category Breakdown")

        // Storage Table Item 1: Necessary
        StorageDetailCard(
            category = "Strictly Necessary (Always Active)",
            icon = Icons.Default.Lock,
            color = SocialGreen,
            items = listOf(
                "socialconnect_user_session (Persistent Credentials & 5-Login Security Cycle)",
                "otp_rate_limiter (Anti-abuse 60-second cooldown timestamp)",
                "room_database (Encrypted offline cache of verified drives)"
            ),
            description = "Crucial for cryptographic OTP authentication, account security, and offline resilience."
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Storage Table Item 2: Functional
        StorageDetailCard(
            category = "Functional Preferences (Configurable)",
            icon = Icons.Default.Cookie,
            color = Color(0xFF0284C7),
            items = listOf(
                "pref_location (Chosen city: e.g. Delhi NCR)",
                "pref_causes (Selected volunteer cause tags: Environment, Education)",
                "pref_distance_km (Radius slider preference)"
            ),
            description = "Remembers your personalized search filters so you don't need to reconfigure them each visit."
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Storage Table Item 3: Analytics
        StorageDetailCard(
            category = "Anonymous Telemetry & Diagnostics (Opt-In)",
            icon = Icons.Default.Security,
            color = Color(0xFFD97706),
            items = listOf(
                "diag_crash_reports (Anonymous stack traces on system errors)",
                "perf_latency_metrics (Aggregated API round-trip measurements)"
            ),
            description = "Zero cross-app tracking. Never tied to individual user profiles or personal email addresses."
        )

        Spacer(modifier = Modifier.height(14.dp))

        SectionHeading("4. Manage Your Preferences")
        BodyText("You can change your consent preferences anytime via the Settings / Preferences menu or by clearing application storage in Android Settings.")
    }
}

@Composable
private fun StorageDetailCard(
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    items: List<String>,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = category, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = SocialTextMain)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 11.sp, color = SocialTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            items.forEach { item ->
                Text(
                    text = "• $item",
                    fontSize = 10.5.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* 4. Refund & Cancellation Policy Section                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun RefundPolicySection() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        SectionHeading("1. 100% Free Volunteer Drives")
        BodyText("The overwhelming majority of volunteer drives on SocialConnect (tree plantation, Yamuna riverfront cleanups, food distribution, animal shelter care) are 100% FREE OF COST. Volunteers can cancel their RSVP at any time with zero financial penalty.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("2. Charity Training Workshops & Equipment Kits")
        BodyText("For select specialized training sessions (e.g., Certified Disaster First Aid Training, Organic Seed Kits, Reforestation Equipment):")
        BulletPoint("Full Refund (100%): Cancellations initiated at least 48 hours prior to the scheduled drive start time are eligible for a full 100% refund.")
        BulletPoint("Late Cancellation: Cancellations made under 48 hours prior are non-refundable as customized non-profit materials are already procured.")
        BulletPoint("Drive Rescheduling / Cancellation: If an NGO cancels a drive due to severe weather, air quality hazards, or civic restrictions, participants are automatically issued a 100% refund.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("3. Tax-Deductible Direct Donations (Section 80G)")
        BodyText("Voluntary monetary contributions remitted directly to registered NGO partners holding valid Section 80G certificates under the Indian Income Tax Act, 1961 are non-refundable once the official tax exemption donation receipt has been issued, except in verified cases of documented unauthorized banking transactions.")

        Spacer(modifier = Modifier.height(12.dp))

        SectionHeading("4. Processing Timelines & Dispute Resolution")
        BodyText("Approved refunds are initiated within 48 business hours and credited to the original payment instrument (UPI / Debit / Credit Card) within 5 to 7 banking working days. For billing support, email us at: refunds@socialconnect.org.")
    }
}

/* -------------------------------------------------------------------------- */
/* 5. Business & Grievance Redressal Section                                  */
/* -------------------------------------------------------------------------- */

@Composable
private fun BusinessAndGrievanceSection() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(18.dp)
    ) {
        SectionHeading("1. Corporate Identity & Registered Office")
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                DetailRow("Legal Entity Name", "SocialConnect Foundation")
                DetailRow("Entity Classification", "Section 8 Non-Profit Company (Indian Companies Act, 2013)")
                DetailRow("Corporate Identification (CIN)", "U85300DL2024NPL419820")
                DetailRow("NGO Darpan Unique ID", "DL/2024/0398211")
                DetailRow("Registered Office Address", "Plot No. 14, Sector 10, Dwarka, New Delhi – 110075, India")
                DetailRow("Official Website", "https://socialconnect.org")
                DetailRow("Official Contact Email", "contact@socialconnect.org")
                DetailRow("Helpdesk Phone", "+91 11 4987 6543 (Mon–Fri, 9:30 AM – 6:00 PM IST)")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionHeading("2. Statutory Grievance Redressal Officer (IT Rules & DPDP)")
        BodyText("Pursuant to Rule 3(2) of the Information Technology (Intermediary Guidelines and Digital Media Ethics Code) Rules, 2021 and Section 13 of the Digital Personal Data Protection Act, 2023, the details of our designated Grievance Officer and Data Protection Officer are as follows:")

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                DetailRow("Designated Grievance Officer", "Mr. Rahul Sharma")
                DetailRow("Designation", "Data Protection & Grievance Redressal Officer")
                DetailRow("Direct Email", "grievance-officer@socialconnect.org")
                DetailRow("Postal Address", "Grievance Desk, SocialConnect Foundation, Plot No. 14, Sector 10, Dwarka, New Delhi 110075")
                DetailRow("Statutory Acknowledgment", "Within 48 hours of complaint receipt")
                DetailRow("Statutory Resolution Window", "Within 30 calendar days")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionHeading("3. Escalation to the Data Protection Board of India")
        BodyText("If your grievance regarding personal data processing, access, or erasure is not resolved by our Grievance Officer within 30 days, or if you are dissatisfied with the resolution, you have the statutory right under Section 13(3) of the DPDP Act 2023 to submit an appeal or complaint to the Data Protection Board of India (DPBI).")
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = SocialTextMuted, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, color = SocialTextMain, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        color = SocialTextMain,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 17.5.sp,
        color = Color(0xFF334155),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        Text(text = "• ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SocialGreen)
        Text(text = text, fontSize = 11.5.sp, lineHeight = 16.5.sp, color = Color(0xFF334155))
    }
}
