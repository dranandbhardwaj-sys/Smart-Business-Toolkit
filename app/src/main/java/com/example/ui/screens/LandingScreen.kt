package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SmoothAreaLineChart
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.LocalSaaSColors
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onStartTrial: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onContactSubmit: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var expandedFaq by remember { mutableIntStateOf(0) }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Sticky SaaS Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, semantic.cardBorder),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "₹",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart Business Toolkit",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "₹399/month • Made for Bharat",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = stringResource(R.string.cd_theme_toggle),
                        )
                    }
                    TextButton(
                        onClick = onLoginClick,
                        modifier = Modifier.testTag("landing_header_login_btn"),
                    ) {
                        Text("Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. HERO SECTION
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.background,
                                )
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 720.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            color = semantic.successContainer,
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        ) {
                            Text(
                                text = "आपका छोटा बिजनेस, अब स्मार्ट तरीके से।",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "अपने बिजनेस का पूरा हिसाब, एक ही जगह।",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Billing, Customers, Expenses, Payments और Stock Management — छोटे बिजनेस के लिए एक स्मार्ट समाधान।",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = semantic.textMuted,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, semantic.cardBorder),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("All-in-One SaaS", style = MaterialTheme.typography.labelMedium, color = semantic.textMuted)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "₹399/month",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = onStartTrial,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(50.dp)
                                    .testTag("landing_start_trial_btn"),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Free Trial", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onLoginClick,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(50.dp)
                                    .testTag("landing_login_btn"),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text("Login", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(22.dp))
                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            border = BorderStroke(1.dp, semantic.cardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_landing_hero),
                                contentDescription = stringResource(R.string.cd_hero_banner),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }

            // 2. FEATURES SECTION (6 Cards)
            item {
                LandingSectionContainer(title = "Everything your shop needs", subtitle = "Built for Indian retail stores, kiranas, freelancers & service pros") {
                    val features = listOf(
                        Triple("Smart Billing", "GST & non-GST invoices with WhatsApp PDF sharing in 10 seconds.", Icons.Rounded.ReceiptLong),
                        Triple("Customer Management", "Track udhar/khata balances, purchase history & payment reminders.", Icons.Rounded.Groups),
                        Triple("Expense Tracking", "Categorize rent, electricity, staff salary & daily petty cash.", Icons.Rounded.AccountBalanceWallet),
                        Triple("QR & UPI Records", "Log UPI, BharatQR, Cash & Bank settlements with transaction IDs.", Icons.Rounded.QrCode2),
                        Triple("Inventory Management", "Real-time stock alerts, SKU tracking, Stock In & Stock Out.", Icons.Rounded.Inventory2),
                        Triple("Business Reports", "Sales, Profit, GST & Stock reports with instant PDF/CSV export.", Icons.Rounded.Assessment),
                    )
                    FlowRow(
                        maxItemsInEachRow = 2,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        features.forEach { (title, desc, icon) ->
                            LandingFeatureCard(
                                title = title,
                                desc = desc,
                                icon = icon,
                                modifier = Modifier.weight(1f).widthIn(min = 148.dp),
                            )
                        }
                    }
                }
            }

            // 3. HOW IT WORKS
            item {
                LandingSectionContainer(title = "How It Works", subtitle = "Go digital in under 2 minutes") {
                    val steps = listOf(
                        "1" to ("Create Free Trial Account" to "Enter your shop name, GST preference & UPI ID in a guided 7-step setup."),
                        "2" to ("Add Products & Customers" to "Import or add your daily items with purchase/selling price and GST%."),
                        "3" to ("Bill, Collect UPI & Grow" to "Send GST bills on WhatsApp, watch profit charts & stay tax-ready."),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        steps.forEach { (num, pair) ->
                            Card(
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, semantic.cardBorder),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(num, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(pair.first, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(pair.second, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. DASHBOARD PREVIEW
            item {
                LandingSectionContainer(title = "Live Dashboard Preview", subtitle = "Real-time profit & sales clarity at a glance") {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text("Monthly Sales", style = MaterialTheme.typography.labelMedium, color = semantic.textMuted)
                                    Text("₹1,84,200", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                StatusPill(text = "+24% vs last mo", tone = StatusTone.SUCCESS)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            SmoothAreaLineChart(
                                data = listOf("Jun" to 95f, "Jul" to 118f, "Aug" to 110f, "Sep" to 142f, "Oct" to 165f, "Nov" to 184f),
                                lineColor = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // 5. INVOICE PREVIEW
            item {
                LandingSectionContainer(title = "Indian GST Invoice Preview", subtitle = "Print or share directly on customer WhatsApp") {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("Sharma Electronics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("GSTIN: 27AABCS1429B1Z5", style = MaterialTheme.typography.labelSmall, fontFamily = MonoFontFamily, color = semantic.textMuted)
                                }
                                StatusPill(text = "PAID • UPI", tone = StatusTone.SUCCESS)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = semantic.cardBorder)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Havells Ceiling Fan x2", style = MaterialTheme.typography.bodySmall)
                                Text("₹4,980", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CGST 9% + SGST 9%", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                Text("₹896", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Payable", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("₹5,876", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // 6. BENEFITS
            item {
                LandingSectionContainer(title = "Why Shopkeepers Love It", subtitle = "Designed for speed, reliability & zero accounting confusion") {
                    val benefits = listOf(
                        Triple("Save 2+ Hours Daily", "Replace paper registers with instant digital billing.", Icons.Rounded.Speed),
                        Triple("Faster Udhar Recovery", "Know every pending rupee and send polite reminders.", Icons.Rounded.CheckCircle),
                        Triple("100% Secure Backup", "Prepared for cloud sync so your business ledger stays safe.", Icons.Rounded.Shield),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        benefits.forEach { (title, desc, icon) ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, semantic.cardBorder),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. PRICING
            item {
                LandingSectionContainer(title = "Simple, Transparent Pricing", subtitle = "No hidden commissions or per-invoice fees") {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            StatusPill(text = "SMART BUSINESS TOOLKIT PRO", tone = StatusTone.SUCCESS)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("₹399", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(" / month", style = MaterialTheme.typography.titleMedium, color = semantic.textMuted, modifier = Modifier.padding(bottom = 4.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            listOf(
                                "Unlimited GST & Non-GST Invoices",
                                "Unlimited Customers & Udhar Ledger",
                                "Expense & UPI/QR Payment Tracking",
                                "Low-Stock Alerts & Inventory Management",
                                "PDF / CSV Business Reports",
                            ).forEach { feat ->
                                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feat, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRegisterClick,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                Text("Create Account & Start 14-Day Free Trial", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 8. FAQ
            item {
                LandingSectionContainer(title = "Frequently Asked Questions", subtitle = "Clear answers before you start") {
                    val faqs = listOf(
                        "Can I use it without GST registration?" to "Yes! During onboarding you can toggle GST Registered to No and issue clean non-GST bills.",
                        "Does it work on Android mobile phones?" to "Yes, Smart Business Toolkit is mobile-first and works seamlessly on phones and tablets.",
                        "What happens after my 14-day free trial?" to "Continue with Smart Business Toolkit Pro at just ₹399/month via UPI, QR, or Card.",
                        "Can I share invoices on WhatsApp?" to "Yes, every invoice exports as an Indian PDF bill ready for one-tap WhatsApp sharing or printing.",
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        faqs.forEachIndexed { idx, (q, a) ->
                            val isOpen = expandedFaq == idx
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, semantic.cardBorder),
                                modifier = Modifier.clickable { expandedFaq = if (isOpen) -1 else idx },
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(q, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                            contentDescription = null,
                                        )
                                    }
                                    AnimatedVisibility(visible = isOpen) {
                                        Text(
                                            text = a,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = semantic.textMuted,
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 9. CONTACT
            item {
                LandingSectionContainer(title = "Talk to Our Onboarding Team", subtitle = "Need help migrating from paper registers or Tally?") {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, semantic.cardBorder),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = contactName,
                                onValueChange = { contactName = it },
                                label = { Text("Your Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("Mobile / WhatsApp Number") },
                                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = contactMessage,
                                onValueChange = { contactMessage = it },
                                label = { Text("How can we help?") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = {
                                    onContactSubmit(contactName.ifBlank { "Merchant" })
                                    contactName = ""
                                    contactPhone = ""
                                    contactMessage = ""
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                            ) {
                                Text("Request Callback")
                            }
                        }
                    }
                }
            }

            // 10. FOOTER
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, semantic.cardBorder),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Smart Business Toolkit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "आपका छोटा बिजनेस, अब स्मार्ट तरीके से। • ₹399/month",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textMuted,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "© 2026 Smart Business Toolkit India • GST, UPI & Inventory SaaS",
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.textMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LandingSectionContainer(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = LocalSaaSColors.current.textMuted,
        )
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun LandingFeatureCard(
    title: String,
    desc: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val semantic = LocalSaaSColors.current
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, semantic.cardBorder),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
        }
    }
}
