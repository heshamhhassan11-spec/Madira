package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class RechargeRequest(
    val id: String,
    val username: String,
    val userTagId: String = "ID-89421",
    val amount: String,
    val method: String, // "فودافون كاش" أو "إنستاباي"
    val phone: String,
    val receiptImageUri: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRechargeScreen(
    requests: List<RechargeRequest>,
    onApprove: (String, String) -> Unit,
    onReject: (String, String) -> Unit,
    onBack: () -> Unit,
    fontScale: Float = 1.0f
) {
    BackHandler { onBack() }

    var isAuthenticated by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarColor by remember { mutableStateOf(Color.Green) }

    if (!isAuthenticated) {
        // PIN Code Verification Screen for Admin (Secret Code: 292004)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("تسجيل دخول الأدمن", color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
                )
            },
            containerColor = DarkAppBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkAppBackground)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "أدخل الكود السري الخاص بالأدمن",
                            color = TextPrimary,
                            fontSize = (15 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it
                                pinError = false
                            },
                            placeholder = { Text("أدخل الكود السري...", color = TextMuted) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = pinError,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (pinError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الكود السري غير صحيح!", color = Color(0xFFE53935), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (pinInput.trim() == "292004") {
                                    isAuthenticated = true
                                } else {
                                    pinError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("دخول لوحة التحكم", color = Color(0xFF1E1E2C), fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "لوحة تحكم الأدمن - طلبات الشحن والاشتراكات",
                            color = TextPrimary,
                            fontSize = (16 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
            )
        },
        containerColor = DarkAppBackground,
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(
                    containerColor = snackbarColor,
                    contentColor = Color.Black,
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("إغلاق", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(snackbarMessage!!, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkAppBackground)
                .testTag("admin_recharge_screen")
        ) {
            if (requests.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "عاش يا وحش! مفيش طلبات شحن معلقة حالياً 🎉",
                        color = TextPrimary,
                        fontSize = (16 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(requests, key = { it.id }) { request ->
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "اسم المستخدم: ${request.username}",
                                            color = TextPrimary,
                                            fontSize = (15 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ID: #${request.userTagId}",
                                            color = GoldPrimary,
                                            fontSize = (12 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = request.method,
                                            color = NeonGreen,
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "المبلغ / الباقة: ${request.amount}",
                                    color = GoldPrimary,
                                    fontSize = (13.5 * fontScale).sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "رقم المحول منه: ${request.phone}",
                                    color = TextSecondary,
                                    fontSize = (13 * fontScale).sp
                                )

                                if (request.receiptImageUri != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, GoldPrimary)
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = request.receiptImageUri,
                                            contentDescription = "صورة إيصال التحويل",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // WhatsApp Contact
                                OutlinedButton(
                                    onClick = { /* Handle WhatsApp link */ },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                                    border = BorderStroke(1.dp, NeonGreen)
                                ) {
                                    Text("تواصل مع العميل واتساب: 01228823207", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = DarkCardBorder)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onReject(request.id, request.username)
                                            snackbarMessage = "تم رفض طلب المستخدم: ${request.username}"
                                            snackbarColor = Color(0xFFE53935)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                        border = BorderStroke(1.dp, Color(0xFFE53935))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("رفض", fontSize = (12 * fontScale).sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            onApprove(request.id, request.username)
                                            snackbarMessage = "تمت الموافقة وشحن رصيد المستخدم: ${request.username} بنجاح! 💰"
                                            snackbarColor = Color(0xFF2ECC71)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("موافقة وشحن", color = Color.Black, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
