package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationCampaign
import com.example.data.model.MaterialDonationItem
import com.example.ui.theme.SocialGreen
import com.example.ui.theme.SocialGreenLight
import com.example.ui.theme.SocialGreenUltraLight
import com.example.ui.theme.SocialTextMain
import com.example.ui.theme.SocialTextMuted
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventDonationSection(
    campaign: DonationCampaign,
    eventTitle: String,
    eventCategory: String,
    eventDateStr: String,
    ngoName: String,
    onDonateMoney: (Double) -> Unit,
    onDonateMaterial: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoneyDialog by remember { mutableStateOf(false) }
    var preselectedMoneyAmount by remember { mutableDoubleStateOf(500.0) }
    var selectedMaterialForPledge by remember { mutableStateOf<MaterialDonationItem?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    val moneyProgress = (campaign.raisedAmount / campaign.targetAmount).toFloat().coerceIn(0f, 1f)
    val remainingMoney = maxOf(0.0, campaign.targetAmount - campaign.raisedAmount)
    val percentFunded = (moneyProgress * 100).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Donation",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Support This Event",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Text(
                        text = "Funds & required material donations",
                        fontSize = 12.sp,
                        color = SocialTextMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SocialGreenUltraLight,
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SocialGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "80G Tax Exempt",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Success Feedback Banner
        successNotice?.let { notice ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFF6EE7B7))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SocialGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = notice,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF065F46),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { successNotice = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // User Contributions Notice if any
        if (campaign.userMoneyDonated > 0 || campaign.userMaterialDonated.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Contributions to this Drive",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialGreen
                        )
                    }
                    if (campaign.userMoneyDonated > 0) {
                        Text(
                            text = "• Contributed ₹${String.format(Locale.US, "%,d", campaign.userMoneyDonated.toLong())} in funds",
                            fontSize = 12.sp,
                            color = SocialTextMain,
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                        )
                    }
                    campaign.userMaterialDonated.forEach { pledge ->
                        Text(
                            text = "• Pledged: $pledge",
                            fontSize = 12.sp,
                            color = SocialTextMain,
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 1. MONEY DONATION CARD WITH PROGRESS BAR
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SocialGreenUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = SocialGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Monetary Operations Goal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SocialTextMain
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "$percentFunded% Funded",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Money Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${String.format(Locale.US, "%,d", campaign.raisedAmount.toLong())}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SocialGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "raised of ₹${String.format(Locale.US, "%,d", campaign.targetAmount.toLong())}",
                            fontSize = 13.sp,
                            color = SocialTextMuted,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = "${campaign.donorsCount} Donors",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SocialTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { moneyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = SocialGreen,
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (remainingMoney > 0) {
                            "₹${String.format(Locale.US, "%,d", remainingMoney.toLong())} needed to reach target"
                        } else {
                            "Goal Met! All extra funds support scale-up"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (remainingMoney > 0) SocialTextMuted else SocialGreen
                    )
                    Text(
                        text = "Verified NGO Account",
                        fontSize = 11.sp,
                        color = SocialTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))

                // Quick Donate Pills
                Text(
                    text = "Quick Contribution Amounts:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(200.0, 500.0, 1000.0, 2000.0).forEach { amt ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    preselectedMoneyAmount = amt
                                    showMoneyDialog = true
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹${amt.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocialTextMain
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        preselectedMoneyAmount = 500.0
                        showMoneyDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SocialGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Donate Funds (Custom / UPI)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. REQUIRED MATERIAL THINGS LIST
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Required Material Things",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "${campaign.materialItems.size} Needed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Toys, study materials & kits needed for this event",
                    fontSize = 12.sp,
                    color = SocialTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (campaign.materialItems.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text(
                    text = "No material items currently requested for this drive.",
                    fontSize = 13.sp,
                    color = SocialTextMuted,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                campaign.materialItems.forEach { item ->
                    val itemProgress = (item.collectedQuantity.toFloat() / item.neededQuantity.toFloat()).coerceIn(0f, 1f)
                    val remainingQty = maxOf(0, item.neededQuantity - item.collectedQuantity)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Emoji Icon Box
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.iconEmoji, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SocialTextMain,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Urgency Pill
                                        val urgencyBg = when (item.urgency) {
                                            "Urgent" -> Color(0xFFFEE2E2)
                                            "High Priority" -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                        val urgencyColor = when (item.urgency) {
                                            "Urgent" -> Color(0xFFDC2626)
                                            "High Priority" -> Color(0xFFD97706)
                                            else -> Color(0xFF475569)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = urgencyBg
                                        ) {
                                            Text(
                                                text = item.urgency,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = urgencyColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Category tag
                                    Text(
                                        text = "Category: ${item.category}",
                                        fontSize = 11.sp,
                                        color = SocialTextMuted
                                    )

                                    if (item.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Item Progress Bar & Counts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.collectedQuantity} of ${item.neededQuantity} ${item.unit} collected",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SocialTextMain
                                )

                                Text(
                                    text = if (remainingQty > 0) "$remainingQty ${item.unit} needed" else "Fulfilled! 🎉",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingQty > 0) Color(0xFFD97706) else SocialGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            LinearProgressIndicator(
                                progress = { itemProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (remainingQty == 0) SocialGreen else Color(0xFF0D9488),
                                trackColor = Color(0xFFE2E8F0)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Donate Item Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { selectedMaterialForPledge = item },
                                    modifier = Modifier.height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, SocialGreen),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SocialGreen
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pledge / Donate ${item.category}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MONEY DONATION INTERACTIVE DIALOG
    // ==========================================
    if (showMoneyDialog) {
        MoneyDonationDialog(
            targetAmount = campaign.targetAmount,
            raisedAmount = campaign.raisedAmount,
            eventTitle = eventTitle,
            ngoName = ngoName,
            initialAmount = preselectedMoneyAmount,
            onDismiss = { showMoneyDialog = false },
            onConfirm = { amount ->
                onDonateMoney(amount)
                showMoneyDialog = false
                successNotice = "Successfully contributed ₹${amount.toInt()}! 80G Tax receipt sent to your profile."
            }
        )
    }

    // ==========================================
    // MATERIAL DONATION INTERACTIVE DIALOG
    // ==========================================
    selectedMaterialForPledge?.let { materialItem ->
        MaterialPledgeDialog(
            item = materialItem,
            eventTitle = eventTitle,
            eventDateStr = eventDateStr,
            ngoName = ngoName,
            onDismiss = { selectedMaterialForPledge = null },
            onConfirm = { qty, dropOffMethod ->
                onDonateMaterial(materialItem.id, qty)
                selectedMaterialForPledge = null
                successNotice = "Thank you! Pledged $qty ${materialItem.unit} of \"${materialItem.name}\" ($dropOffMethod)."
            }
        )
    }
}

@Composable
private fun MoneyDonationDialog(
    targetAmount: Double,
    raisedAmount: Double,
    eventTitle: String,
    ngoName: String,
    initialAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(initialAmount.toInt().toString()) }
    var selectedPaymentMode by remember { mutableStateOf("UPI") } // "UPI", "Card", "NetBanking"
    var panNumber by remember { mutableStateOf("") }

    val currentAmount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SocialGreenUltraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = SocialGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contribute Money",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Beneficiary: $ngoName",
                    fontSize = 12.sp,
                    color = SocialTextMuted
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select or Enter Amount (INR)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Amount preset chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(200, 500, 1000, 2500).forEach { preset ->
                        val isSelected = amountText == preset.toString()
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { amountText = preset.toString() },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SocialGreen else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) SocialGreen else Color(0xFFCBD5E1))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹$preset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SocialTextMain
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 7) {
                            amountText = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Donation Amount (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SocialGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Payment Option",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "UPI (Google Pay, PhonePe, Paytm, BHIM)",
                        "Credit / Debit Card",
                        "Net Banking (All Indian Banks)"
                    ).forEach { mode ->
                        val modeKey = when {
                            mode.startsWith("UPI") -> "UPI"
                            mode.startsWith("Credit") -> "Card"
                            else -> "NetBanking"
                        }
                        val isSelected = selectedPaymentMode == modeKey

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPaymentMode = modeKey },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSelected) SocialGreen else Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPaymentMode = modeKey },
                                    colors = RadioButtonDefaults.colors(selectedColor = SocialGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    color = SocialTextMain,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = panNumber,
                    onValueChange = { if (it.length <= 10) panNumber = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("PAN Number (Optional for 80G tax receipt)") },
                    placeholder = { Text("ABCDE1234F") },
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "80G certificate will be issued to your registered email.",
                            fontSize = 10.sp,
                            color = SocialTextMuted
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentAmount > 0) {
                        onConfirm(currentAmount)
                    }
                },
                enabled = currentAmount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SocialGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Contribute ₹${currentAmount.toInt()}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SocialTextMuted)
            }
        }
    )
}

@Composable
private fun MaterialPledgeDialog(
    item: MaterialDonationItem,
    eventTitle: String,
    eventDateStr: String,
    ngoName: String,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, dropOffMethod: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedDropOff by remember { mutableStateOf("BRING_IN_PERSON") } // "BRING_IN_PERSON", "COURIER_HUB"

    val remainingNeeded = maxOf(1, item.neededQuantity - item.collectedQuantity)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pledge ${item.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SocialTextMain
                    )
                    Text(
                        text = "For $eventTitle",
                        fontSize = 11.sp,
                        color = SocialTextMuted
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Info banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Drive needs $remainingNeeded more ${item.unit}. Every item directly reaches beneficiaries.",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Quantity to Donate (${item.unit})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stepper Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = SocialTextMain)
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = "$quantity ${item.unit}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SocialTextMain
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    IconButton(
                        onClick = { if (quantity < 100) quantity++ },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SocialGreenUltraLight)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = SocialGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick quantity chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 5, 10).forEach { qtyPreset ->
                        val isSelected = quantity == qtyPreset
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { quantity = qtyPreset },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SocialGreen else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) SocialGreen else Color(0xFFCBD5E1))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$qtyPreset ${item.unit}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SocialTextMain
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "How will you provide the items?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SocialTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Drop-off options
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedDropOff = "BRING_IN_PERSON" },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedDropOff == "BRING_IN_PERSON") Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (selectedDropOff == "BRING_IN_PERSON") SocialGreen else Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDropOff == "BRING_IN_PERSON",
                            onClick = { selectedDropOff = "BRING_IN_PERSON" },
                            colors = RadioButtonDefaults.colors(selectedColor = SocialGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Bring in person on drive day",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialTextMain
                            )
                            Text(
                                text = "Handover to event desk on $eventDateStr",
                                fontSize = 11.sp,
                                color = SocialTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedDropOff = "COURIER_HUB" },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedDropOff == "COURIER_HUB") Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (selectedDropOff == "COURIER_HUB") SocialGreen else Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDropOff == "COURIER_HUB",
                            onClick = { selectedDropOff = "COURIER_HUB" },
                            colors = RadioButtonDefaults.colors(selectedColor = SocialGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Courier / Ship to NGO Hub",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocialTextMain
                            )
                            Text(
                                text = "Delivery tracking via $ngoName logistics hub",
                                fontSize = 11.sp,
                                color = SocialTextMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val methodLabel = if (selectedDropOff == "BRING_IN_PERSON") "In person on drive day" else "Courier to NGO hub"
                    onConfirm(quantity, methodLabel)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SocialGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Confirm Pledge ($quantity ${item.unit})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SocialTextMuted)
            }
        }
    )
}
