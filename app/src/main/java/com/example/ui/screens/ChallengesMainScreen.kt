package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.AppNotificationItem
import com.example.ui.components.NotificationsSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeIconType
import com.example.data.model.ChallengeItem
import com.example.ui.theme.DarkAppBackground
import com.example.ui.theme.DarkAppBarColor
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesMainScreen(
    challenges: List<ChallengeItem>,
    notifications: List<AppNotificationItem> = emptyList(),
    onMarkNotificationAsRead: (String) -> Unit = {},
    onMarkAllNotificationsAsRead: () -> Unit = {},
    onAddNotification: (String, String, String, String?) -> Unit = { _, _, _, _ -> },
    onChallengeSelected: (ChallengeItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    userPoints: Int = 2850,
    userCoins: Double = 100.0,
    unlockedChallengeIds: List<String> = listOf("guess_player"),
    onNavigateToSubscription: () -> Unit = {},
    onDeductCoins: (String) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var lockedChallengeAlertItem by remember { mutableStateOf<ChallengeItem?>(null) }
    var insufficientCoinsAlertItem by remember { mutableStateOf<ChallengeItem?>(null) }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    if (showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            fontScale = fontScale,
            onDismiss = { showNotificationsSheet = false },
            onMarkAsRead = onMarkNotificationAsRead,
            onMarkAllAsRead = onMarkAllNotificationsAsRead,
            onAddNotification = onAddNotification
        )
    }

    // Sort strictly by orderIndex: 1, 2, 3, 4
    val sortedChallenges = remember(challenges, searchQuery) {
        challenges.sortedBy { it.orderIndex }.filter { challenge ->
            searchQuery.isBlank() ||
                challenge.title.contains(searchQuery, ignoreCase = true) ||
                challenge.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkAppBackground)
            .testTag("challenges_main_screen")
    ) {
        // Top AppBar with Madira Logo and Notifications Badge
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .border(1.2.dp, GoldPrimary, RoundedCornerShape(9.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_madira_logo),
                            contentDescription = "Madira Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Madira Games 🏆",
                                color = TextPrimary,
                                fontSize = (17 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "v1.3",
                                    color = NeonGreen,
                                    fontSize = (9.5 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "4 تحديات كروية أسطورية بالترتيب",
                            color = TextSecondary,
                            fontSize = (11 * fontScale).sp
                        )
                    }
                }
            },
            actions = {
                // Notifications Icon with BadgedBox
                IconButton(
                    onClick = { showNotificationsSheet = true },
                    modifier = Modifier.testTag("btn_open_notifications")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(
                                    containerColor = NeonGreen,
                                    contentColor = Color(0xFF101018)
                                ) {
                                    Text(text = "$unreadNotificationsCount", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "الإشعارات والتحديثات",
                            tint = if (unreadNotificationsCount > 0) NeonGreen else GoldPrimary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val nextScale = when {
                            fontScale < 1.1f -> 1.15f
                            fontScale < 1.25f -> 1.3f
                            else -> 1.0f
                        }
                        onFontScaleChange(nextScale)
                    },
                    modifier = Modifier.testTag("btn_font_size_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "تغيير حجم الخط",
                        tint = GoldPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1E2C),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${userCoins.toInt()} 🪙",
                            color = NeonGreen,
                            fontSize = (11.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$userPoints ن",
                            color = GoldPrimary,
                            fontSize = (11.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
        )

        // Upper Section: Search bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkAppBarColor)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "ابحث بين التحديات (لاعبين، أندية، كأس العالم، تشكيلات)...",
                        color = TextMuted,
                        fontSize = (13 * fontScale).sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = GoldPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_challenges_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E2C),
                    unfocusedContainerColor = Color(0xFF1E1E2C),
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Challenge Cards List in strict 1, 2, 3, 4 order
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("challenges_list_view"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Hero Card with Madira Logo & Version 1.2
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("madira_hero_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161624)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, GoldPrimary, RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_madira_logo),
                                contentDescription = "Madira App Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مرحباً بك في Madira Games 🏆",
                                    color = GoldPrimary,
                                    fontSize = (15 * fontScale).sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "تحديث الإصدار v1.2: شعار جديد كلياً، مستويات وتحديات متنوعة وإعدادات صوت.",
                                color = TextSecondary,
                                fontSize = (11.5 * fontScale).sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldPrimary.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldPrimary),
                                    modifier = Modifier.clickable { showNotificationsSheet = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "الإشعارات والتحديثات",
                                            color = GoldPrimary,
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة التحديات الرسمية:",
                        color = TextPrimary,
                        fontSize = (15.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "10 نقاط لكل سؤال صحيح ⚡",
                        color = NeonGreen,
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(
                items = sortedChallenges,
                key = { it.id }
            ) { challenge ->
                val isUnlocked = challenge.id in unlockedChallengeIds
                StrictChallengeCardItem(
                    item = challenge,
                    isUnlocked = isUnlocked,
                    fontScale = fontScale,
                    onClick = {
                        if (!isUnlocked) {
                            lockedChallengeAlertItem = challenge
                        } else {
                            if (userCoins < challenge.coinCostPerRound) {
                                insufficientCoinsAlertItem = challenge
                            } else {
                                val success = onDeductCoins(challenge.id)
                                if (success) {
                                    onChallengeSelected(challenge)
                                } else {
                                    insufficientCoinsAlertItem = challenge
                                }
                            }
                        }
                    },
                    onToggleFavorite = { onToggleFavorite(challenge.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (lockedChallengeAlertItem != null) {
        AlertDialog(
            onDismissRequest = { lockedChallengeAlertItem = null },
            containerColor = DarkCardSurface,
            title = {
                Text("🔒 التحدي مغلق!", color = GoldPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "عذراً! التحدي \"${lockedChallengeAlertItem?.title}\" مغلق للمستخدمين الجدد. يرجى الشحن أو الاشتراك في إحدى باقات VIP لفتح باقي التحديات والكسب السريع!",
                    color = TextPrimary,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        lockedChallengeAlertItem = null
                        onNavigateToSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("شحن واشتراك VIP 🌟", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedChallengeAlertItem = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }

    if (insufficientCoinsAlertItem != null) {
        AlertDialog(
            onDismissRequest = { insufficientCoinsAlertItem = null },
            containerColor = DarkCardSurface,
            title = {
                Text("🪙 رصيد العملات غير كافٍ!", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "يتطلب بدء دور في \"${insufficientCoinsAlertItem?.title}\" وجود ${insufficientCoinsAlertItem?.coinCostPerRound} عملة داخلية. رصيدك الحالي: ${userCoins.toInt()} عملة.\n\nيمكنك شحن كوينز جديدة أو استبدال نقاطك الكروية بعملات من شاشة الملف الشخصي!",
                    color = TextPrimary,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        insufficientCoinsAlertItem = null
                        onNavigateToSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("شحن عملات جديدة 💳", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { insufficientCoinsAlertItem = null }) {
                    Text("إغلاق", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun StrictChallengeCardItem(
    item: ChallengeItem,
    isUnlocked: Boolean = true,
    fontScale: Float,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val challengeIcon: ImageVector = when (item.iconType) {
        ChallengeIconType.PERSON -> Icons.Default.Person
        ChallengeIconType.SOCCER -> Icons.Default.SportsSoccer
        ChallengeIconType.TROPHY -> Icons.Default.EmojiEvents
        ChallengeIconType.GROUPS -> Icons.Default.Groups
    }

    val iconAccentColor = if (isUnlocked) {
        when (item.orderIndex) {
            1 -> GoldPrimary
            2 -> NeonGreen
            3 -> Color(0xFFFFD54F)
            else -> Color(0xFF64B5F6)
        }
    } else {
        Color.Gray
    }

    val heartColor by animateColorAsState(
        targetValue = if (item.isFavorite) Color(0xFFFF5252) else TextMuted,
        label = "favoriteColorAnim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                onClick = onClick,
                indication = ripple(bounded = true, color = GoldPrimary),
                interactionSource = remember { MutableInteractionSource() }
            )
            .border(1.2.dp, if (isUnlocked) iconAccentColor.copy(alpha = 0.5f) else DarkCardBorder, RoundedCornerShape(16.dp))
            .testTag("challenge_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) DarkCardSurface else Color(0xFF14141E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order Badge, Icon, Titles, and Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number badge
                Surface(
                    shape = CircleShape,
                    color = iconAccentColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, iconAccentColor),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = challengeIcon,
                            contentDescription = item.title,
                            tint = iconAccentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Order
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = iconAccentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "التحدي ${item.orderIndex}",
                                color = iconAccentColor,
                                fontSize = (10.5 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (!isUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE53935).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🔒 مغلق (يتطلب الاشتراك)",
                                    color = Color(0xFFE53935),
                                    fontSize = (10.5 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (item.highlightTag != null) {
                            Text(
                                text = item.highlightTag,
                                color = TextSecondary,
                                fontSize = (11 * fontScale).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.title,
                        color = if (isUnlocked) TextPrimary else TextMuted,
                        fontSize = (17 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("btn_fav_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = heartColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = item.description,
                color = if (isUnlocked) TextSecondary else TextMuted,
                fontSize = (12.5 * fontScale).sp,
                lineHeight = (18 * fontScale).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cost per round & Points per correct answer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⚡ +${item.pointsPerCorrectAnswer} نقطة/إجابة",
                        color = NeonGreen,
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🪙 ${item.coinCostPerRound} عملة / دور",
                        color = GoldPrimary,
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUnlocked) iconAccentColor else GoldPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isUnlocked) "ابدأ التحدي (${item.coinCostPerRound} 🪙)" else "فتح التحدي 🔒",
                            color = Color.Black,
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
