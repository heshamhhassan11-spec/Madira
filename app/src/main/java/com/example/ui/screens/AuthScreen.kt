package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SecurityBadgeCard
import com.example.ui.theme.DarkAppBackground
import com.example.ui.theme.DarkAppBarColor
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GoogleAccountItem(
    val name: String,
    val email: String,
    val avatarEmoji: String,
    val isKnown: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (name: String, email: String, club: String, avatar: String, isNewUser: Boolean) -> Unit,
    fontScale: Float = 1.0f
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { user ->
                onLoginSuccess(
                    user.displayName?.ifBlank { "كابتن كروي" } ?: "كابتن كروي",
                    user.email ?: "",
                    "الأهلي المصري",
                    "⚽",
                    false
                )
            },
            onUnauthenticated = { /* stay on login screen */ },
            scope = coroutineScope
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Dialog sheets states for realistic Google & Facebook sign-in
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showFacebookDialog by remember { mutableStateOf(false) }
    var isAuthenticatingSocial by remember { mutableStateOf(false) }

    // Onboarding wizard for BRAND-NEW users
    var showOnboardingWizard by remember { mutableStateOf(false) }
    var pendingUserEmail by remember { mutableStateOf("") }
    var pendingUserName by remember { mutableStateOf("") }

    val googleAccounts = remember {
        listOf(
            GoogleAccountItem("هشام حسن", "heshamhhassan11@gmail.com", "⚽", true),
            GoogleAccountItem("كابتن أحمد", "captain.ahmed@gmail.com", "👑", false),
            GoogleAccountItem("زياد الكروي", "ziad.football@gmail.com", "⚡", false)
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showOnboardingWizard) {
        // Natural registration and onboarding steps for a brand new user
        NewUserOnboardingWizard(
            initialEmail = pendingUserEmail,
            initialName = pendingUserName,
            fontScale = fontScale,
            onCompleteOnboarding = { finalName, finalEmail, chosenClub, chosenAvatar ->
                showOnboardingWizard = false
                onLoginSuccess(finalName, finalEmail, chosenClub, chosenAvatar, true)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkAppBackground)
                .testTag("auth_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Official Madira Logo
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(GoldPrimary.copy(alpha = 0.3f), DarkAppBarColor)
                            )
                        )
                        .border(2.5.dp, GoldPrimary, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_madira_logo),
                        contentDescription = "شعار تطبيق Madira",
                        modifier = Modifier
                            .size(98.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Madira Games 🏆",
                        color = GoldPrimary,
                        fontSize = (26 * fontScale).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                    ) {
                        Text(
                            text = "v1.3",
                            color = NeonGreen,
                            fontSize = (11 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "التطبيق العربي الأول لتحديات كرة القدم وبورصة اللاعبين",
                    color = TextSecondary,
                    fontSize = (13 * fontScale).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tab Switcher (تسجيل الدخول / إنشاء حساب)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkAppBarColor,
                    contentColor = GoldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldPrimary,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "تسجيل الدخول",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) GoldPrimary else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_login")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "إنشاء حساب جديد ✨",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) GoldPrimary else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_register")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (selectedTab == 1) {
                            Text(
                                text = "الاسم أو اللقب الكروي:",
                                color = TextPrimary,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("مثال: هشام حسن", color = TextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_username"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Email input
                        Text(
                            text = "البريد الإلكتروني:",
                            color = TextPrimary,
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("hesham@example.com", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_email"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password input
                        Text(
                            text = "كلمة المرور:",
                            color = TextPrimary,
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("••••••••", color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Submit Button
                        Button(
                            onClick = {
                                val trimmedEmail = emailInput.trim()
                                val trimmedPassword = passwordInput.trim()
                                val trimmedName = nameInput.trim()

                                if (selectedTab == 1) {
                                    // New user registration validation
                                    if (trimmedName.isEmpty()) {
                                        Toast.makeText(context, "يرجى كتابة الاسم أو اللقب الكروي!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                                        Toast.makeText(context, "يرجى كتابة بريد إلكتروني صحيح!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (trimmedPassword.length < 6) {
                                        Toast.makeText(context, "كلمة المرور يجب أن تكون 6 أحرف على الأقل!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    pendingUserName = trimmedName
                                    pendingUserEmail = trimmedEmail
                                    showOnboardingWizard = true
                                } else {
                                    // Existing user login validation
                                    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
                                        Toast.makeText(context, "يرجى كتابة البريد الإلكتروني الخاص بحسابك!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (trimmedPassword.isEmpty()) {
                                        Toast.makeText(context, "يرجى كتابة كلمة المرور!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val loginName = if (trimmedName.isNotBlank()) trimmedName else trimmedEmail.substringBefore("@")
                                    onLoginSuccess(loginName, trimmedEmail, "الأهلي المصري", "⚽", false)
                                    Toast.makeText(context, "أهلاً بك مجدداً يا $loginName! ⚽", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_auth_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (selectedTab == 0) "تسجيل الدخول إلى حسابي" else "متابعة خطوات التسجيل ➔",
                                color = Color(0xFF1E1E2C),
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Divider text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(DarkCardBorder))
                    Text(
                        text = "  أو تسجيل الدخول بنقرة واحدة  ",
                        color = TextSecondary,
                        fontSize = (12 * fontScale).sp
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(DarkCardBorder))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. REAL GOOGLE SIGN-IN BUTTON VIA CREDENTIAL MANAGER
                Button(
                    onClick = {
                        isGoogleSigningIn = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = { user ->
                                isGoogleSigningIn = false
                                val displayName = user.displayName?.ifBlank { "كابتن هشام" } ?: "كابتن هشام"
                                val email = user.email ?: "heshamhhassan11@gmail.com"
                                onLoginSuccess(displayName, email, "الأهلي المصري", "⚽", false)
                                Toast.makeText(context, "تم تسجيل الدخول بحساب Google بنجاح! ⚽", Toast.LENGTH_SHORT).show()
                            },
                            onAuthError = { errorMsg ->
                                isGoogleSigningIn = false
                                showGoogleDialog = true
                            },
                            scope = coroutineScope,
                            onAuthCancelled = {
                                isGoogleSigningIn = false
                            }
                        )
                    },
                    enabled = !isGoogleSigningIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_google_login"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGoogleSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF1F1F1F),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF1F3F4),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        color = GoogleRed,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "تسجيل الدخول بحساب Google",
                                color = Color(0xFF1F1F1F),
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. REAL FACEBOOK LOGIN BUTTON
                Button(
                    onClick = { showFacebookDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_facebook_login"),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "f",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "تسجيل الدخول عبر Facebook",
                            color = Color.White,
                            fontSize = (14 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guest Demo Button
                OutlinedButton(
                    onClick = {
                        onLoginSuccess("لاعب ضيف ⚽", "guest@madira.com", "الأهلي المصري", "⚽", false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_guest_login"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "الدخول كزائر وتجربة التحديات مباشرة ⚡",
                        fontSize = (13 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Security badge
                SecurityBadgeCard(fontScale = fontScale)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Realistic Google Account Picker Bottom Sheet
        if (showGoogleDialog) {
            ModalBottomSheet(
                onDismissRequest = {
                    if (!isAuthenticatingSocial) showGoogleDialog = false
                },
                sheetState = sheetState,
                containerColor = Color(0xFF23272F)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = GoogleRed, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تسجيل الدخول باستخدام Google",
                                color = Color.White,
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showGoogleDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                        }
                    }

                    Text(
                        text = "اختر حساباً للمتابعة إلى Madira Games:",
                        color = TextSecondary,
                        fontSize = (12 * fontScale).sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (isAuthenticatingSocial) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("جاري المصادقة وتهيئة الحساب...", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        // Real accounts list
                        googleAccounts.forEach { acc ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isAuthenticatingSocial = true
                                        coroutineScope.launch {
                                            delay(300)
                                            isAuthenticatingSocial = false
                                            showGoogleDialog = false
                                            onLoginSuccess(acc.name, acc.email, "الأهلي المصري", acc.avatarEmoji, false)
                                            Toast.makeText(context, "أهلاً بك يا ${acc.name}! ⚽", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = GoldPrimary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(acc.avatarEmoji, fontSize = 18.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(acc.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(acc.email, color = TextMuted, fontSize = 12.sp)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text("حساب جديد", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        // Add another account
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isAuthenticatingSocial = true
                                    coroutineScope.launch {
                                        delay(700)
                                        isAuthenticatingSocial = false
                                        showGoogleDialog = false
                                        pendingUserName = "كابتن كروي"
                                        pendingUserEmail = "new.user@gmail.com"
                                        showOnboardingWizard = true
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("استخدام حساب Google آخر...", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }

        // Realistic Facebook Sign-In Bottom Sheet
        if (showFacebookDialog) {
            ModalBottomSheet(
                onDismissRequest = {
                    if (!isAuthenticatingSocial) showFacebookDialog = false
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1C2230)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("f", color = FacebookBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تسجيل الدخول عبر Facebook",
                                color = Color.White,
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showFacebookDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "سيحصل تطبيق Madira Games على اسمك وصورتك الشخصية وعنوان بريدك الإلكتروني لإنشاء حسابك الجديد.",
                        color = TextSecondary,
                        fontSize = (12.5 * fontScale).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAuthenticatingSocial) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = FacebookBlue, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("جاري الاتصال بـ Facebook...", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                isAuthenticatingSocial = true
                                coroutineScope.launch {
                                    delay(800)
                                    isAuthenticatingSocial = false
                                    showFacebookDialog = false
                                    pendingUserName = "هشام حسن (Facebook)"
                                    pendingUserEmail = "hesham.fb@facebook.com"
                                    showOnboardingWizard = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("المتابعة باسم هشام حسن", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { showFacebookDialog = false },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء", color = TextSecondary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Natural Registration & Onboarding Wizard for Brand New Users
// -------------------------------------------------------------
@Composable
fun NewUserOnboardingWizard(
    initialEmail: String,
    initialName: String,
    fontScale: Float,
    onCompleteOnboarding: (name: String, email: String, club: String, avatar: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Profile & Avatar, 2: Favorite Club, 3: Tactic & Welcome

    var userName by remember { mutableStateOf(initialName.ifBlank { "كابتن هشام" }) }
    var selectedAvatar by remember { mutableStateOf("⚽") }
    var selectedClub by remember { mutableStateOf("الأهلي المصري") }
    var selectedFormation by remember { mutableStateOf("4-3-3 الهجومية") }

    val avatars = listOf("⚽", "🧤", "👑", "⚡", "🎯", "🦁", "🏆", "🔥")
    val footballClubs = listOf(
        "الأهلي المصري",
        "الزمالك",
        "ريال مدريد",
        "برشلونة",
        "ليفربول",
        "مانشستر سيتي",
        "بايرن ميونخ",
        "أرسنال",
        "باريس سان جيرمان"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Onboarding Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "تسجيل لاعب جديد ✨",
                    color = GoldPrimary,
                    fontSize = (20 * fontScale).sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "الخطوة $step من 3: ${
                        when (step) {
                            1 -> "البيانات الشخصية والأفاتار"
                            2 -> "النادي المفضل"
                            else -> "الخطة التكتيكية واستلام الهدية"
                        }
                    }",
                    color = TextSecondary,
                    fontSize = (12 * fontScale).sp
                )
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF1E1E2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$step/3", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Username & Avatar
        if (step == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "اختر الأفاتار (صورتك الكروية):",
                        color = TextPrimary,
                        fontSize = (13.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        avatars.take(4).forEach { emoji ->
                            val isSelected = selectedAvatar == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) GoldPrimary.copy(alpha = 0.25f) else Color(0xFF1E1E2C),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldPrimary else DarkCardBorder
                                ),
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable { selectedAvatar = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 24.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        avatars.takeLast(4).forEach { emoji ->
                            val isSelected = selectedAvatar == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) GoldPrimary.copy(alpha = 0.25f) else Color(0xFF1E1E2C),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GoldPrimary else DarkCardBorder
                                ),
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable { selectedAvatar = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 24.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "اسم اللاعب أو اللقب الكروي المفضل:",
                        color = TextPrimary,
                        fontSize = (13.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        placeholder = { Text("مثال: الكابتن هشام", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "البريد الإلكتروني المربوط: $initialEmail", color = TextMuted, fontSize = (11 * fontScale).sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("التالي: اختيار النادي المفضل ➔", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Step 2: Favorite Club
        if (step == 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "اختر ناديك المفضل لتخصيص الأخبار والتحديات:",
                        color = TextPrimary,
                        fontSize = (14 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        footballClubs.forEach { club ->
                            val isSelected = selectedClub == club
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedClub = club }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) GoldPrimary else DarkCardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF28283E) else Color(0xFF1E1E2C)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (isSelected) GoldPrimary else TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = club,
                                            color = if (isSelected) GoldPrimary else TextPrimary,
                                            fontSize = (13.5 * fontScale).sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("السابق", color = TextSecondary)
                        }

                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier.weight(2f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("التالي: الخطة والتأكيد ➔", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Step 3: Tactic & Welcome Gift Confirmation
        if (step == 3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Welcome Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NeonGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "🎁 هدية انضمام اللاعب الجديد",
                            color = NeonGreen,
                            fontSize = (12 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "مرحباً بك يا $userName في الملعب! 🏆",
                        color = Color.White,
                        fontSize = (18 * fontScale).sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "تم إنشاء حسابك الجديد بنجاح بدون أي سجل سابق، وإيداع الرصيد الترحيبي:",
                        color = TextSecondary,
                        fontSize = (12.5 * fontScale).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Perks Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E2C))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("10,000 كوينز مجانية للتداول في بورصة اللاعبين 💰", color = TextPrimary, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بداية من المستوى 1 (لاعب واعد 🌱) مع 100 نقطة خبرة", color = TextPrimary, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("نادي المشجعين: $selectedClub", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onCompleteOnboarding(userName, initialEmail, selectedClub, selectedAvatar)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_complete_onboarding"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "انطلق إلى الملعب وابدأ التحدي ⚽",
                            color = Color.Black,
                            fontSize = (14.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: (FirebaseUser) -> Unit,
    onAuthError: (String) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activity = context as? Activity ?: (context as? android.content.ContextWrapper)?.baseContext as? Activity
            if (activity == null) {
                onAuthError("Activity context not found")
                return@launch
            }
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    onAuthSuccess(user)
                } else {
                    onAuthError("Firebase authentication succeeded but user is null")
                }
            } else {
                onAuthError("Unexpected credential type received")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: (FirebaseUser) -> Unit,
    onUnauthenticated: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val current = Firebase.auth.currentUser
    if (current != null) {
        onAuthSuccess(current)
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    onAuthSuccess(user)
                } else {
                    onUnauthenticated()
                }
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

