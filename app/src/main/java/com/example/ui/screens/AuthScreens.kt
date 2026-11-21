package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalSaaSColors

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSubmit: (String) -> Unit,
    onNavigateRegister: () -> Unit,
    onForgotPasswordSent: (String) -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var email by remember { mutableStateOf("rajesh@sharmatraders.in") }
    var password by remember { mutableStateOf("SmartBiz@2026") }
    var showPassword by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showForgotModal by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

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
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.img_business_avatar),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Smart Business Toolkit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("आपका छोटा बिजनेस, अब स्मार्ट तरीके से।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Welcome back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Sign in to access your GST billing, ledger & stock.", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorText = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorText = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    )

                    if (errorText != null) {
                        Text(errorText.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { resetEmail = email; showForgotModal = true }) {
                            Text("Forgot password?")
                        }
                    }

                    Button(
                        onClick = {
                            if (!email.contains("@") || password.length < 4) {
                                errorText = "Please enter a valid email and password (min 4 chars)."
                            } else {
                                onLoginSubmit(email.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_submit_button"),
                    ) {
                        Text("Login to Workspace", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("New to Smart Business?", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                        TextButton(
                            onClick = onNavigateRegister,
                            modifier = Modifier.testTag("login_to_register_btn"),
                        ) {
                            Text("Register Free", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showForgotModal) {
        AlertDialog(
            onDismissRequest = { showForgotModal = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your registered business email to receive a reset link.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Business Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showForgotModal = false
                    onForgotPasswordSent(resetEmail.ifBlank { "your email" })
                }) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotModal = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onCreateAccount: (fullName: String, email: String, mobile: String, businessName: String, businessType: String) -> Unit,
    onNavigateLogin: () -> Unit,
) {
    val semantic = LocalSaaSColors.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    val businessTypes = listOf("Retail & Kirana", "Electronics & Hardware", "Freelancer / Services", "Medical & Pharmacy", "Wholesale & Distribution")
    var businessType by remember { mutableStateOf(businessTypes.first()) }
    var typeExpanded by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
            Text("Create Your SaaS Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("14-Day Free Trial • Then ₹399/month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, semantic.cardBorder),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMsg = null },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                    )
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it; errorMsg = null },
                        label = { Text("Mobile Number (+91)") },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input"),
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it; errorMsg = null },
                        label = { Text("Business / Shop Name") },
                        leadingIcon = { Icon(Icons.Rounded.Business, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_business_input"),
                    )

                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                    ) {
                        OutlinedTextField(
                            value = businessType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Business Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
                        ) {
                            businessTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        businessType = option
                                        typeExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMsg = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { acceptedTerms = !acceptedTerms },
                    ) {
                        Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                        Text(
                            text = "I accept the Smart Business Toolkit Terms & Privacy Policy.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    if (errorMsg != null) {
                        Text(errorMsg.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() || businessName.isBlank() -> errorMsg = "Enter your full name and business name."
                                !email.contains("@") -> errorMsg = "Enter a valid email address."
                                mobile.length < 10 -> errorMsg = "Enter a 10-digit mobile number."
                                password.length < 4 || password != confirmPassword -> errorMsg = "Passwords must match (min 4 chars)."
                                !acceptedTerms -> errorMsg = "Please accept the Terms to continue."
                                else -> onCreateAccount(fullName, email, mobile, businessName, businessType)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("register_submit_btn"),
                    ) {
                        Text("Create Account & Continue", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Already have an account?", style = MaterialTheme.typography.bodySmall, color = semantic.textMuted)
                        TextButton(onClick = onNavigateLogin) { Text("Login", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
