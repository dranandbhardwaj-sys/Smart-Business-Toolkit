package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.BusinessProfileEntity
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialProfile: BusinessProfileEntity,
    onFinishSetup: (
        businessName: String,
        category: String,
        mobile: String,
        gstRegistered: Boolean,
        gstin: String,
        upiId: String,
        logoUri: String?,
    ) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var step by remember { mutableIntStateOf(1) }
    var businessName by remember { mutableStateOf(initialProfile.businessName) }
    var category by remember { mutableStateOf(initialProfile.category) }
    var mobile by remember { mutableStateOf(initialProfile.mobile) }
    var gstRegistered by remember { mutableStateOf(initialProfile.gstRegistered) }
    var gstin by remember { mutableStateOf(initialProfile.gstin) }
    var upiId by remember { mutableStateOf(initialProfile.upiId) }
    var logoUri by remember { mutableStateOf<String?>(initialProfile.logoUri) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) logoUri = uri.toString()
    }

    val categories = listOf(
        "Retail & Kirana",
        "Electronics & Hardware",
        "Freelancer / Agency",
        "Medical & Pharmacy",
        "Garments & Boutique",
        "Wholesale & Distribution",
        "Salon & Services",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text(
                text = "Step $step of 7",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { step / 7f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            )
            Spacer(modifier = Modifier.height(22.dp))

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (step) {
                        1 -> {
                            Text("What's your Business Name?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("This appears on your GST invoices, WhatsApp shares & receipts.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            OutlinedTextField(
                                value = businessName,
                                onValueChange = { businessName = it },
                                label = { Text("Business / Shop Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("onboarding_business_name"),
                            )
                        }
                        2 -> {
                            Text("Select Business Category", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Helps us tailor invoice templates and expense categories.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = category == cat,
                                        onClick = { category = cat },
                                        label = { Text(cat) },
                                    )
                                }
                            }
                        }
                        3 -> {
                            Text("Business Mobile Number", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Displayed on bills so customers can reach or WhatsApp you.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text("Mobile Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        4 -> {
                            Text("Are you GST Registered?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Enable GST tax breakdown (CGST/SGST) or bill as an unregistered shop.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("GST Registered Business", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Switch(checked = gstRegistered, onCheckedChange = { gstRegistered = it })
                            }
                            if (gstRegistered) {
                                OutlinedTextField(
                                    value = gstin,
                                    onValueChange = { gstin = it.uppercase() },
                                    label = { Text("15-Digit GSTIN") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        5 -> {
                            Text("Add Your Business UPI ID", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Printed as a payment QR & UPI link on every customer invoice.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("UPI ID (e.g. yourshop@okaxis)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        6 -> {
                            Text("Upload Business Logo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Choose a photo from gallery or keep our clean storefront emblem.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (logoUri != null) {
                                    AsyncImage(
                                        model = logoUri,
                                        contentDescription = stringResource(R.string.cd_business_logo),
                                        modifier = Modifier.size(96.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_business_avatar),
                                        contentDescription = stringResource(R.string.cd_business_logo),
                                        modifier = Modifier.size(96.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                ) {
                                    Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Select Logo from Gallery")
                                }
                            }
                        }
                        7 -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ready to Launch!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Text("Review your Smart Business Toolkit workspace details:", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            SummaryLine("Business", businessName)
                            SummaryLine("Category", category)
                            SummaryLine("Mobile", mobile)
                            SummaryLine("GST Status", if (gstRegistered) "Registered ($gstin)" else "Non-GST")
                            SummaryLine("UPI ID", upiId, mono = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (step > 1) {
                            OutlinedButton(
                                onClick = { step -= 1 },
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) {
                                Text("Back")
                            }
                        }
                        Button(
                            onClick = {
                                if (step < 7) {
                                    step += 1
                                } else {
                                    onFinishSetup(
                                        businessName.ifBlank { "My Smart Business" },
                                        category,
                                        mobile,
                                        gstRegistered,
                                        gstin,
                                        upiId,
                                        logoUri,
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.4f).height(48.dp).testTag("onboarding_next_btn"),
                        ) {
                            Text(if (step < 7) "Continue" else "Finish Setup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, mono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalSaaSColors.current.textMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (mono) MonoFontFamily else null,
        )
    }
}
