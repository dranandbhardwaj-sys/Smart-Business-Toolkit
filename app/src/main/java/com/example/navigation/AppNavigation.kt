package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.InvoiceEntity
import com.example.ui.components.IndianInvoicePreviewDialog
import com.example.ui.components.SaaSAppShell
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CustomerFormDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseFormDialog
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NewInvoiceScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ProductFormDialog
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.state.AppUiState
import com.example.ui.state.MainViewModel

@Composable
fun SmartBusinessNavGraph(
    state: AppUiState,
    viewModel: MainViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.LANDING

    var previewInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var showQuickCustomer by remember { mutableStateOf(false) }
    var showQuickExpense by remember { mutableStateOf(false) }
    var showQuickProduct by remember { mutableStateOf(false) }

    if (state.isLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    val startDestination = remember {
        if (state.session.isLoggedIn) {
            if (state.session.isOnboarded) AppRoutes.DASHBOARD else AppRoutes.ONBOARDING
        } else {
            AppRoutes.LANDING
        }
    }

    val isAuthFlowRoute = currentRoute in setOf(
        AppRoutes.LANDING,
        AppRoutes.LOGIN,
        AppRoutes.REGISTER,
        AppRoutes.ONBOARDING,
    )

    val navigateToAppRoute: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val navContent: @Composable () -> Unit = {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(AppRoutes.LANDING) {
                LandingScreen(
                    darkTheme = state.session.darkMode,
                    onToggleTheme = { viewModel.toggleDarkMode() },
                    onStartTrial = {
                        viewModel.startFreeTrialDemo {
                            navController.navigate(AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.LANDING) { inclusive = true }
                            }
                        }
                    },
                    onLoginClick = { navController.navigate(AppRoutes.LOGIN) },
                    onRegisterClick = { navController.navigate(AppRoutes.REGISTER) },
                    onContactSubmit = { name ->
                        viewModel.showToast("Thanks $name! Our team will call you shortly.")
                    },
                )
            }

            composable(AppRoutes.LOGIN) {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoginSubmit = { email ->
                        viewModel.login(email) {
                            val target = if (state.session.isOnboarded) AppRoutes.DASHBOARD else AppRoutes.ONBOARDING
                            navController.navigate(target) {
                                popUpTo(AppRoutes.LANDING) { inclusive = true }
                            }
                        }
                    },
                    onNavigateRegister = { navController.navigate(AppRoutes.REGISTER) },
                    onForgotPasswordSent = { email ->
                        viewModel.showToast("Password reset link sent to $email")
                    },
                )
            }

            composable(AppRoutes.REGISTER) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onCreateAccount = { name, email, mobile, bizName, bizType ->
                        viewModel.register(name, email, mobile, bizName, bizType) {
                            navController.navigate(AppRoutes.ONBOARDING) {
                                popUpTo(AppRoutes.LANDING) { inclusive = true }
                            }
                        }
                    },
                    onNavigateLogin = { navController.navigate(AppRoutes.LOGIN) },
                )
            }

            composable(AppRoutes.ONBOARDING) {
                OnboardingScreen(
                    initialProfile = state.profile,
                    onFinishSetup = { bizName, cat, mobile, gstReg, gstin, upiId, logoUri ->
                        viewModel.completeOnboarding(bizName, cat, mobile, gstReg, gstin, upiId, logoUri) {
                            navController.navigate(AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable(AppRoutes.DASHBOARD) {
                DashboardScreen(
                    state = state,
                    onNavigate = navigateToAppRoute,
                    onNewInvoice = { navController.navigate(AppRoutes.NEW_INVOICE) },
                    onQuickAddCustomer = { showQuickCustomer = true },
                    onQuickAddExpense = { showQuickExpense = true },
                    onQuickAddProduct = { showQuickProduct = true },
                    onPreviewInvoice = { previewInvoice = it },
                    onMarkInvoicePaid = { viewModel.markInvoicePaid(it) },
                )
            }

            composable(AppRoutes.INVOICES) {
                InvoicesScreen(
                    invoices = state.invoices,
                    profile = state.profile,
                    onCreateNew = { navController.navigate(AppRoutes.NEW_INVOICE) },
                    onPreviewInvoice = { previewInvoice = it },
                    onMarkPaid = { viewModel.markInvoicePaid(it) },
                    onDeleteInvoice = { viewModel.deleteInvoice(it) },
                    onActionFeedback = { viewModel.showToast(it) },
                )
            }

            composable(AppRoutes.NEW_INVOICE) {
                NewInvoiceScreen(
                    customers = state.customers,
                    products = state.products,
                    profile = state.profile,
                    onBack = { navController.popBackStack() },
                    onQuickSaveCustomer = { viewModel.saveCustomer(it) },
                    onQuickSaveProduct = { viewModel.saveProduct(it) },
                    onSaveInvoice = { customer, items, discount, paid, method, notes ->
                        viewModel.createInvoice(customer, items, discount, paid, method, notes) { saved ->
                            navController.popBackStack()
                            previewInvoice = saved
                        }
                    },
                )
            }

            composable(AppRoutes.CUSTOMERS) {
                CustomersScreen(
                    customers = state.customers,
                    invoices = state.invoices,
                    payments = state.payments,
                    onSaveCustomer = { viewModel.saveCustomer(it) },
                    onDeleteCustomer = { viewModel.deleteCustomer(it) },
                    onRecordPaymentForCustomer = { cust, amt, method ->
                        viewModel.recordPayment(cust, null, amt, method, "", "Customer balance payment")
                    },
                )
            }

            composable(AppRoutes.EXPENSES) {
                ExpensesScreen(
                    expenses = state.expenses,
                    onSaveExpense = { viewModel.saveExpense(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                )
            }

            composable(AppRoutes.PAYMENTS) {
                PaymentsScreen(
                    payments = state.payments,
                    customers = state.customers,
                    invoices = state.invoices,
                    profile = state.profile,
                    onRecordPayment = { cust, inv, amt, method, txnId, notes ->
                        viewModel.recordPayment(cust, inv, amt, method, txnId, notes)
                    },
                )
            }

            composable(AppRoutes.INVENTORY) {
                InventoryScreen(
                    products = state.products,
                    onSaveProduct = { viewModel.saveProduct(it) },
                    onAdjustStock = { prod, delta -> viewModel.adjustStock(prod, delta) },
                    onDeleteProduct = { viewModel.deleteProduct(it) },
                )
            }

            composable(AppRoutes.REPORTS) {
                ReportsScreen(
                    state = state,
                    onExportFeedback = { viewModel.showToast(it) },
                )
            }

            composable(AppRoutes.SETTINGS) {
                SettingsScreen(
                    state = state,
                    onSaveProfile = { viewModel.updateProfile(it) },
                    onToggleTheme = { viewModel.toggleDarkMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onOpenSubscription = { navigateToAppRoute(AppRoutes.SUBSCRIPTION) },
                    onOpenAdmin = { navigateToAppRoute(AppRoutes.ADMIN) },
                    onLogout = {
                        viewModel.logout {
                            navController.navigate(AppRoutes.LANDING) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable(AppRoutes.SUBSCRIPTION) {
                SubscriptionScreen(
                    subscription = state.subscription,
                    paymentHistory = state.subscriptionPayments,
                    onRenewSubscription = { method -> viewModel.renewSubscription(method) },
                )
            }

            composable(AppRoutes.ADMIN) {
                AdminDashboardScreen(
                    adminUsers = state.adminUsers,
                    subscriptionPayments = state.subscriptionPayments,
                    profile = state.profile,
                    onUpdateUserPlan = { user, status -> viewModel.updateAdminUserPlan(user, status) },
                    onUpdateWhatsappUrl = { url -> viewModel.updateWhatsappSupportUrl(url) },
                )
            }

            composable(AppRoutes.SUPPORT) {
                SupportScreen(
                    whatsappSupportUrl = state.profile.whatsappSupportUrl,
                    tickets = state.supportTickets,
                    onSubmitFeedback = { subj, cat, msg -> viewModel.submitSupportTicket(subj, cat, msg) },
                )
            }
        }
    }

    if (isAuthFlowRoute) {
        navContent()
    } else {
        SaaSAppShell(
            currentRoute = currentRoute,
            profile = state.profile,
            subscription = state.subscription,
            darkTheme = state.session.darkMode,
            language = state.session.language,
            onNavigate = navigateToAppRoute,
            onToggleTheme = { viewModel.toggleDarkMode() },
            onToggleLanguage = { viewModel.toggleLanguage() },
            onQuickNewInvoice = { navController.navigate(AppRoutes.NEW_INVOICE) },
        ) {
            navContent()
        }
    }

    previewInvoice?.let { inv ->
        IndianInvoicePreviewDialog(
            invoice = inv,
            profile = state.profile,
            onDismiss = { previewInvoice = null },
            onActionFeedback = { viewModel.showToast(it) },
        )
    }

    if (showQuickCustomer) {
        CustomerFormDialog(
            initial = null,
            onSave = {
                viewModel.saveCustomer(it)
                showQuickCustomer = false
            },
            onDismiss = { showQuickCustomer = false },
        )
    }

    if (showQuickExpense) {
        ExpenseFormDialog(
            initial = null,
            onSave = {
                viewModel.saveExpense(it)
                showQuickExpense = false
            },
            onDismiss = { showQuickExpense = false },
        )
    }

    if (showQuickProduct) {
        ProductFormDialog(
            initial = null,
            onSave = {
                viewModel.saveProduct(it)
                showQuickProduct = false
            },
            onDismiss = { showQuickProduct = false },
        )
    }
}
