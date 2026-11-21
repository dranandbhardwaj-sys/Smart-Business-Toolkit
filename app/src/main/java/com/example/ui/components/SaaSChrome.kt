package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.BusinessProfileEntity
import com.example.data.local.SubscriptionEntity
import com.example.navigation.AppRoutes
import com.example.ui.theme.LocalSaaSColors
import com.example.util.tr
import kotlinx.coroutines.launch

data class NavDestinationItem(
    val route: String,
    val labelEn: String,
    val labelHi: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showInBottomNav: Boolean = false,
)

val PrimaryNavDestinations = listOf(
    NavDestinationItem(AppRoutes.DASHBOARD, "Dashboard", "डैशबोर्ड", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard, true),
    NavDestinationItem(AppRoutes.INVOICES, "Invoices", "बिलिंग", Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong, true),
    NavDestinationItem(AppRoutes.CUSTOMERS, "Customers", "ग्राहक", Icons.Rounded.Groups, Icons.Outlined.Groups, true),
    NavDestinationItem(AppRoutes.INVENTORY, "Inventory", "स्टॉक", Icons.Rounded.Inventory2, Icons.Outlined.Inventory2, true),
    NavDestinationItem(AppRoutes.EXPENSES, "Expenses", "खर्च", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, false),
    NavDestinationItem(AppRoutes.PAYMENTS, "Payments", "पेमेंट", Icons.Rounded.Payments, Icons.Outlined.Payments, false),
    NavDestinationItem(AppRoutes.REPORTS, "Reports", "रिपोर्ट्स", Icons.Rounded.Assessment, Icons.Outlined.Assessment, true),
    NavDestinationItem(AppRoutes.SUBSCRIPTION, "Pro Plan", "प्लान ₹399", Icons.Rounded.WorkspacePremium, Icons.Outlined.WorkspacePremium, false),
    NavDestinationItem(AppRoutes.ADMIN, "Admin SaaS", "एडमिन", Icons.Rounded.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, false),
    NavDestinationItem(AppRoutes.SUPPORT, "Support", "सहायता", Icons.Rounded.HelpOutline, Icons.Outlined.HelpOutline, false),
    NavDestinationItem(AppRoutes.SETTINGS, "Settings", "सेटिंग्स", Icons.Rounded.Settings, Icons.Outlined.Settings, false),
)

@Composable
fun SaaSAppShell(
    currentRoute: String,
    profile: BusinessProfileEntity,
    subscription: SubscriptionEntity,
    darkTheme: Boolean,
    language: String,
    onNavigate: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onQuickNewInvoice: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentNav = PrimaryNavDestinations.find { it.route == currentRoute }
    val pageTitle = currentNav?.let { tr(it.labelEn, it.labelHi, language) } ?: "Smart Business"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 840.dp
        val isMedium = maxWidth in 600.dp..839.dp

        if (isExpanded) {
            // Desktop / Tablet Permanent Sidebar + Top Header
            Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                PermanentSidebar(
                    currentRoute = currentRoute,
                    profile = profile,
                    subscription = subscription,
                    language = language,
                    onSelectRoute = onNavigate,
                )
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    SaaSTopHeader(
                        title = pageTitle,
                        profile = profile,
                        subscription = subscription,
                        darkTheme = darkTheme,
                        language = language,
                        showMenuButton = false,
                        onOpenDrawer = {},
                        onToggleTheme = onToggleTheme,
                        onToggleLanguage = onToggleLanguage,
                        onOpenSubscription = { onNavigate(AppRoutes.SUBSCRIPTION) },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        content(Modifier.fillMaxSize())
                    }
                }
            }
        } else {
            // Mobile & Medium Adaptive Drawer + BottomBar / Rail
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        SidebarMenuBody(
                            currentRoute = currentRoute,
                            profile = profile,
                            subscription = subscription,
                            language = language,
                            onSelectRoute = { route ->
                                scope.launch { drawerState.close() }
                                onNavigate(route)
                            },
                        )
                    }
                },
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        SaaSTopHeader(
                            title = pageTitle,
                            profile = profile,
                            subscription = subscription,
                            darkTheme = darkTheme,
                            language = language,
                            showMenuButton = true,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onToggleTheme = onToggleTheme,
                            onToggleLanguage = onToggleLanguage,
                            onOpenSubscription = { onNavigate(AppRoutes.SUBSCRIPTION) },
                        )
                    },
                    bottomBar = {
                        if (!isMedium) {
                            SaaSBottomNavBar(
                                currentRoute = currentRoute,
                                language = language,
                                onNavigate = onNavigate,
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentRoute == AppRoutes.DASHBOARD || currentRoute == AppRoutes.INVOICES) {
                            FloatingActionButton(
                                onClick = onQuickNewInvoice,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("fab_new_invoice"),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Create Invoice")
                            }
                        }
                    },
                ) { innerPadding ->
                    Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        if (isMedium) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ) {
                                PrimaryNavDestinations.filter { it.showInBottomNav }.forEach { item ->
                                    val selected = item.route == currentRoute
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { onNavigate(item.route) },
                                        icon = {
                                            Icon(
                                                if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.labelEn,
                                            )
                                        },
                                        label = { Text(tr(item.labelEn, item.labelHi, language), maxLines = 1) },
                                    )
                                }
                            }
                        }
                        content(Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun SaaSTopHeader(
    title: String,
    profile: BusinessProfileEntity,
    subscription: SubscriptionEntity,
    darkTheme: Boolean,
    language: String,
    showMenuButton: Boolean,
    onOpenDrawer: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenSubscription: () -> Unit,
) {
    val semantic = LocalSaaSColors.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    if (showMenuButton) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("drawer_menu_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = stringResource(R.string.cd_nav_menu),
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = profile.businessName,
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (subscription.status == "ACTIVE") semantic.successContainer else semantic.warningContainer,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onOpenSubscription)
                            .testTag("header_plan_badge"),
                    ) {
                        Text(
                            text = if (subscription.status == "ACTIVE") "PRO" else "TRIAL ${subscription.trialDaysLeft}d",
                            color = if (subscription.status == "ACTIVE") semantic.success else semantic.warning,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }

                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.testTag("header_language_toggle"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
                            contentDescription = stringResource(R.string.cd_language_toggle),
                            tint = if (language == "hi") MaterialTheme.colorScheme.primary else semantic.textMuted,
                        )
                    }

                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("header_theme_toggle"),
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = stringResource(R.string.cd_theme_toggle),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            HorizontalDivider(color = semantic.cardBorder, thickness = 1.dp)
        }
    }
}

@Composable
private fun SaaSBottomNavBar(
    currentRoute: String,
    language: String,
    onNavigate: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, semantic.cardBorder),
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding(),
        ) {
            PrimaryNavDestinations.filter { it.showInBottomNav }.forEach { item ->
                val selected = item.route == currentRoute
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.testTag("bottom_nav_${item.route}"),
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.labelEn,
                        )
                    },
                    label = {
                        Text(
                            text = tr(item.labelEn, item.labelHi, language),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PermanentSidebar(
    currentRoute: String,
    profile: BusinessProfileEntity,
    subscription: SubscriptionEntity,
    language: String,
    onSelectRoute: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(268.dp)
            .fillMaxHeight(),
        border = BorderStroke(1.dp, semantic.cardBorder),
    ) {
        SidebarMenuBody(
            currentRoute = currentRoute,
            profile = profile,
            subscription = subscription,
            language = language,
            onSelectRoute = onSelectRoute,
        )
    }
}

@Composable
private fun SidebarMenuBody(
    currentRoute: String,
    profile: BusinessProfileEntity,
    subscription: SubscriptionEntity,
    language: String,
    onSelectRoute: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
    ) {
        // Brand Identity Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (profile.logoUri != null) {
                AsyncImage(
                    model = profile.logoUri,
                    contentDescription = stringResource(R.string.cd_business_logo),
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_business_avatar),
                    contentDescription = stringResource(R.string.cd_business_logo),
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Business",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = profile.businessName,
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PrimaryNavDestinations.forEach { item ->
            val selected = item.route == currentRoute
            NavigationDrawerItem(
                label = {
                    Text(
                        text = tr(item.labelEn, item.labelHi, language),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                selected = selected,
                onClick = { onSelectRoute(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.labelEn,
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_${item.route}"),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subscription Upgrade Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable { onSelectRoute(AppRoutes.SUBSCRIPTION) },
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (subscription.status == "ACTIVE") "PRO PLAN ACTIVE" else "PRO ₹399/month",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "आपका छोटा बिजनेस, अब स्मार्ट तरीके से।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
