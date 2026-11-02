package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class RechargePackage(
    val id: String,
    val titleAr: String,
    val coins: Int,
    val priceEgp: String,
    val perksAr: String,
    val badge: String
)

val RECHARGE_PACKAGES = listOf(
    RechargePackage(
        id = "pkg_bronze",
        titleAr = "الباقة البرونزية 🥉",
        coins = 350,
        priceEgp = "30 جنيه",
        perksAr = "350 عملة + فتح التحدي الثاني (شهر)",
        badge = "اقتصادي"
    ),
    RechargePackage(
        id = "pkg_silver",
        titleAr = "الباقة الفضية 🥈",
        coins = 1000,
        priceEgp = "70 جنيه",
        perksAr = "1000 عملة + فتح التحدي الثاني (مدى الحياة)",
        badge = "شائع"
    ),
    RechargePackage(
        id = "pkg_gold",
        titleAr = "الباقة الذهبية 🥇",
        coins = 4000,
        priceEgp = "220 جنيه",
        perksAr = "4000 عملة + فتح التحدي 2 و 3 (مدى الحياة) + التحدي 4 (شهر)",
        badge = "أفضل قيمة 🔥"
    ),
    RechargePackage(
        id = "pkg_diamond",
        titleAr = "الباقة الألماسية 💎",
        coins = 12000,
        priceEgp = "550 جنيه",
        perksAr = "12000 عملة + فتح جميع التحديات مدى الحياة",
        badge = "VIP الأساطير 👑"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionRechargeScreen(
    currentUsername: String,
    currentUserTagId: String = "ID-89421",
    onSubmitRequest: (packageTitle: String, method: String, phone: String, receiptUri: String?) -> Unit,
    onBack: () -> Unit,
    fontScale: Float = 1.0f
) {
    BackHandler { onBack() }

    var selectedPackage by remember { mutableStateOf(RECHARGE_PACKAGES[1]) }
    var selectedMethod by remember { mutableStateOf("فودافون كاش") } // "فودافون كاش" أو "إنستاباي"
    var userPhoneInput by remember { mutableStateOf("") }
    var receiptImageUri by remember { mutableStateOf<Uri?>(null) }
    var submittedSuccessfully by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            receiptImageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("باقات الشحن والاشتراكات VIP 💳", color = TextPrimary) },
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
                .testTag("subscription_recharge_screen")
        ) {
            if (submittedSuccessfully) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(54.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "تم إرسال طلب الشحن بنجاح! 🚀",
                        color = TextPrimary,
                        fontSize = (20 * fontScale).sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "سيقوم الأدمن بمراجعة التحويل وإضافة الكوينز لحسابك قريباً جداً فور التحقق من الرقم والإيصال.",
                        color = TextSecondary,
                        fontSize = (13.5 * fontScale).sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("العودة إلى التطبيق", color = Color(0xFF1E1E2C), fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Price Summary Quick Overview Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💵 جدول أسعار باقات الشحن",
                                    color = GoldPrimary,
                                    fontSize = (13.5 * fontScale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "ID: #${if (currentUserTagId.startsWith("ID-")) currentUserTagId else "ID-$currentUserTagId"}",
                                        color = GoldPrimary,
                                        fontSize = (11.5 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("🥉 البرونزية: 30 ج", color = TextPrimary, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                                Text("🥈 الفضية: 70 ج", color = TextPrimary, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("🥇 الذهبية: 220 ج", color = TextPrimary, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                                Text("💎 الألماسية: 550 ج", color = TextPrimary, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "اختر باقة الشحن المفضلة لديك:",
                        color = TextPrimary,
                        fontSize = (16 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RECHARGE_PACKAGES.forEach { pkg ->
                        val isSelected = selectedPackage.id == pkg.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable { selectedPackage = pkg },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF2C2618) else DarkCardSurface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldPrimary else DarkCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPackage = pkg },
                                        colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = pkg.titleAr,
                                                color = TextPrimary,
                                                fontSize = (14.5 * fontScale).sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = GoldPrimary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = pkg.badge,
                                                    color = GoldPrimary,
                                                    fontSize = (10 * fontScale).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = pkg.perksAr,
                                            color = NeonGreen,
                                            fontSize = (11.5 * fontScale).sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GoldPrimary,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = pkg.priceEgp,
                                        color = Color(0xFF1E1E2C),
                                        fontSize = (14 * fontScale).sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "طريقة الدفع (فقط لا غير):",
                        color = TextPrimary,
                        fontSize = (15 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method Selection Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { selectedMethod = "فودافون كاش" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedMethod == "فودافون كاش") Color(0xFFE53935) else Color(0xFF242436)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("فودافون كاش", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { selectedMethod = "إنستاباي" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedMethod == "إنستاباي") Color(0xFF8E44AD) else Color(0xFF242436)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("إنستاباي", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Display Exact Transfer Number
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E1E2C),
                                border = BorderStroke(1.dp, GoldPrimary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (selectedMethod == "فودافون كاش") "حول المبلغ إلى رقم فودافون كاش التالي:" else "حول المبلغ إلى حساب إنستاباي التالي:",
                                        color = TextSecondary,
                                        fontSize = (12 * fontScale).sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (selectedMethod == "فودافون كاش") "01044581972" else "01228823207",
                                        color = GoldPrimary,
                                        fontSize = (20 * fontScale).sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("فقط لا غير", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "رقم هاتفك الذي قمت بالتحويل منه:",
                        color = TextPrimary,
                        fontSize = (13.5 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = userPhoneInput,
                        onValueChange = { userPhoneInput = it },
                        placeholder = { Text("مثال: 01012345678", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Receipt Image Uploader
                    Text(
                        text = "صورة إيصال التحويل (اختياري / إلزامي للتحقق):",
                        color = TextPrimary,
                        fontSize = (13.5 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (receiptImageUri != null) "تم إرفاق صورة الإيصال بنجاح ✅" else "اختر صورة إيصال التحويل من المعرض 📸",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onSubmitRequest(
                                "${selectedPackage.titleAr} (${selectedPackage.priceEgp})",
                                selectedMethod,
                                userPhoneInput.ifBlank { "01000000000" },
                                receiptImageUri?.toString()
                            )
                            submittedSuccessfully = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إرسال طلب الشحن إلى الأدمن 🚀", color = Color.Black, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
