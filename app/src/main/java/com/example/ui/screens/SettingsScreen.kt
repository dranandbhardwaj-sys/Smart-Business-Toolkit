package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.BusinessProfileEntity
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.state.AppUiState
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily

@Composable
fun SettingsScreen(
    state: AppUiState,
    onSaveProfile: (BusinessProfileEntity) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit,
) {
    val semantic = LocalSaaSColors.current
    val profile = state.profile

    var businessName by remember(profile) { mutableStateOf(profile.businessName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var gstin by remember(profile) { mutableStateOf(profile.gstin) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var phone by remember(profile) { mutableStateOf(profile.mobile) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var upiId by remember(profile) { mutableStateOf(profile.upiId) }
    var invoicePrefix by remember(profile) { mutableStateOf(profile.invoicePrefix) }
    var currency by remember(profile) { mutableStateOf(profile.currency) }
    var logoUri by remember(profile) { mutableStateOf(profile.logoUri) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) logoUri = uri.toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Business & App Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Configure GSTIN, invoice preferences, language & theme", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
        }

        // Logo & Business Profile Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Business Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (logoUri != null) {
                            AsyncImage(
                                model = logoUri,
                                contentDescription = stringResource(R.string.cd_business_logo),
                                modifier = Modifier.size(64.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_business_avatar),
                                contentDescription = stringResource(R.string.cd_business_logo),
                                modifier = Modifier.size(64.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        OutlinedButton(onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Change Logo")
                        }
                    }

                    OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gstin, onValueChange = { gstin = it.uppercase() }, label = { Text("GSTIN") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Business Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("Business UPI ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Invoice & Currency Settings
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Invoice Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = invoicePrefix, onValueChange = { invoicePrefix = it }, label = { Text("Invoice Number Prefix") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Currency", style = MaterialTheme.typography.labelMedium, color = semantic.textMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("INR (₹)", "USD ($)").forEach { curr ->
                            FilterChip(selected = currency == curr, onClick = { currency = curr }, label = { Text(curr) })
                        }
                    }
                    Button(
                        onClick = {
                            onSaveProfile(
                                profile.copy(
                                    businessName = businessName.trim(),
                                    ownerName = ownerName.trim(),
                                    gstin = gstin.trim(),
                                    address = address.trim(),
                                    mobile = phone.trim(),
                                    email = email.trim(),
                                    upiId = upiId.trim(),
                                    logoUri = logoUri,
                                    invoicePrefix = invoicePrefix.trim().ifBlank { "INV-" },
                                    currency = currency,
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_save_btn"),
                    ) {
                        Text("Save Business Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Language & Theme
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Appearance & Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Dark Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Switch between Light Slate & Dark Emerald", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                        }
                        Switch(checked = state.session.darkMode, onCheckedChange = { onToggleTheme() })
                    }
                    HorizontalDivider(color = semantic.cardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Language (English / हिन्दी)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Current: ${if (state.session.language == "hi") "हिन्दी" else "English"}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)
                        }
                        OutlinedButton(onClick = onToggleLanguage) {
                            Text(if (state.session.language == "hi") "Switch to EN" else "हिन्दी में देखें")
                        }
                    }
                }
            }
        }

        // Account & Supabase Readiness Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Account & Backend Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        StatusPill(text = "Supabase Ready", tone = StatusTone.INFO)
                    }
                    Text("Signed in: ${state.session.userEmail}", style = MaterialTheme.typography.bodySmall, fontFamily = MonoFontFamily)
                    Text("Architecture: ${state.supabaseStatus.providerName}", style = MaterialTheme.typography.labelSmall, color = semantic.textMuted)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onOpenSubscription, modifier = Modifier.weight(1f)) {
                            Text("Subscription")
                        }
                        OutlinedButton(onClick = onOpenAdmin, modifier = Modifier.weight(1f)) {
                            Text("Admin SaaS")
                        }
                    }

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_logout_btn"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }
}
