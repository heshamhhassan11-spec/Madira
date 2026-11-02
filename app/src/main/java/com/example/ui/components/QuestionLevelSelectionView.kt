package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.PlayMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkAppBackground
import com.example.ui.theme.DarkAppBarColor
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuizLevelUiConfig(
    val count: Int,
    val levelNameAr: String,
    val titleAr: String,
    val descriptionAr: String,
    val icon: ImageVector,
    val accentColor: Color,
    val secondaryColor: Color,
    val starsCount: Int,
    val estimatedTime: String,
    val rewardLabel: String,
    val difficultyBadge: String
)

val DEFAULT_QUIZ_LEVELS = listOf(
    QuizLevelUiConfig(
        count = 10,
        levelNameAr = "المستوى البرونزي ⚡",
        titleAr = "10 أسئلة عشوائية",
        descriptionAr = "جولة سريعة وممتعة لاختبار البديهة الكروية في دقائق معدودة.",
        icon = Icons.Default.Bolt,
        accentColor = Color(0xFF2ECC71),
        secondaryColor = Color(0xFF27AE60),
        starsCount = 1,
        estimatedTime = "~3 دقائق",
        rewardLabel = "+100 نقطة ماكس (10 ن لكل إجابة)",
        difficultyBadge = "سريع • إحماء"
    ),
    QuizLevelUiConfig(
        count = 25,
        levelNameAr = "المستوى الفضي 🎯",
        titleAr = "25 سؤال عشوائي",
        descriptionAr = "تحدي متوسط يختبر معلوماتك عن كبار النجوم والأندية ومستويات دقيقة.",
        icon = Icons.Default.SportsScore,
        accentColor = Color(0xFF00D2D3),
        secondaryColor = Color(0xFF0984E3),
        starsCount = 2,
        estimatedTime = "~7 دقائق",
        rewardLabel = "+250 نقطة ماكس (10 ن لكل إجابة)",
        difficultyBadge = "متوسط • هدافين"
    ),
    QuizLevelUiConfig(
        count = 50,
        levelNameAr = "المستوى الذهبي 🏆",
        titleAr = "50 سؤال عشوائي",
        descriptionAr = "نصف مئوية كروية حماسية تضعك بين مصاف المحترفين والمتابعين الكبار.",
        icon = Icons.Default.EmojiEvents,
        accentColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFF39C12),
        starsCount = 3,
        estimatedTime = "~15 دقيقة",
        rewardLabel = "+500 نقطة ماكس (10 ن لكل إجابة)",
        difficultyBadge = "متقدم • محترفين"
    ),
    QuizLevelUiConfig(
        count = 100,
        levelNameAr = "المستوى الماسي الأسطوري 👑",
        titleAr = "100 سؤال عشوائي",
        descriptionAr = "الماراثون الكروي الأضخم والأقوى لأصحاب المعرفة الشاملة وعمالقة كرة القدم.",
        icon = Icons.Default.WorkspacePremium,
        accentColor = Color(0xFFE056FD),
        secondaryColor = Color(0xFF9B59B6),
        starsCount = 4,
        estimatedTime = "~30 دقيقة",
        rewardLabel = "+1,000 نقطة ماكس (10 ن لكل إجابة)",
        difficultyBadge = "أسطوري • ماراثون"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionLevelSelectionView(
    challengeTitle: String,
    challengeSubtitle: String,
    testTagPrefix: String,
    fontScale: Float,
    onBack: () -> Unit,
    onSelectCount: (Int) -> Unit,
    onSelectCountWithMode: ((Int, PlayMode) -> Unit)? = null,
    levels: List<QuizLevelUiConfig> = DEFAULT_QUIZ_LEVELS
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAppBackground)
            .testTag("${testTagPrefix}_select_count")
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_madira_logo),
                            contentDescription = "Madira Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = challengeTitle,
                        color = TextPrimary,
                        fontSize = (16.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = GoldPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle banner with points formula
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E1E2C),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NeonGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "10", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نظام النقاط الموحد: +10 نقاط لكل إجابة صحيحة",
                            color = GoldPrimary,
                            fontSize = (13.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = challengeSubtitle,
                        color = TextSecondary,
                        fontSize = (12 * fontScale).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            var selectedPlayMode by remember { mutableStateOf(PlayMode.SOLO) }

            // Mode Selection Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اختر نظام وضع التحدي الكروي:",
                        color = GoldPrimary,
                        fontSize = (13.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedPlayMode = PlayMode.SOLO },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPlayMode == PlayMode.SOLO) GoldPrimary else Color(0xFF2B2B3D)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "فردي (سولو) 👤",
                                color = if (selectedPlayMode == PlayMode.SOLO) Color(0xFF1E1E2C) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = (12 * fontScale).sp
                            )
                        }

                        Button(
                            onClick = { selectedPlayMode = PlayMode.ONLINE },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPlayMode == PlayMode.ONLINE) NeonGreen else Color(0xFF2B2B3D)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "أونلاين (مباشر) 🌐",
                                color = if (selectedPlayMode == PlayMode.ONLINE) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = (12 * fontScale).sp
                            )
                        }
                    }

                    if (selectedPlayMode == PlayMode.ONLINE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🌐 وضع الأونلاين المباشر: تنافس لحظي ضد خصم أونلاين بـ ID حقيقي واحتساب النقاط بالثواني!",
                                color = NeonGreen,
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "اختر مستوى وعدد أسئلة التحدي:",
                color = TextPrimary,
                fontSize = (18 * fontScale).sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(10.dp))

            // The 4 Distinct Level Cards
            levels.forEach { level ->
                LevelSelectionCard(
                    config = level,
                    fontScale = fontScale,
                    testTag = "btn_select_${testTagPrefix}_count_${level.count}",
                    onClick = {
                        onSelectCount(level.count)
                        onSelectCountWithMode?.invoke(level.count, selectedPlayMode)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LevelSelectionCard(
    config: QuizLevelUiConfig,
    fontScale: Float,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = BorderStroke(1.5.dp, config.accentColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            config.accentColor.copy(alpha = 0.12f),
                            Color(0xFF161622)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Row 1: Header with Level Name, Stars, and Difficulty Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = config.levelNameAr,
                        color = config.accentColor,
                        fontSize = (14 * fontScale).sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Stars
                    Row {
                        repeat(config.starsCount) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = config.accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = config.accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, config.accentColor)
                ) {
                    Text(
                        text = config.difficultyBadge,
                        color = config.accentColor,
                        fontSize = (10.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Large Icon, Title, and Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vibrant Icon Box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(config.accentColor.copy(alpha = 0.35f), Color(0xFF1E1E2C))
                            )
                        )
                        .border(1.5.dp, config.accentColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = config.icon,
                        contentDescription = null,
                        tint = config.accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = config.titleAr,
                            color = TextPrimary,
                            fontSize = (16.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF242436)
                        ) {
                            Text(
                                text = "${config.count} س",
                                color = config.accentColor,
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = config.descriptionAr,
                        color = TextSecondary,
                        fontSize = (11.5 * fontScale).sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Pills (Time, Reward, and CTA Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Time pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1E2C)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = config.estimatedTime,
                                color = TextSecondary,
                                fontSize = (10.5 * fontScale).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Reward pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonGreen.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, NeonGreen.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = config.rewardLabel,
                            color = NeonGreen,
                            fontSize = (10.5 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }

                // Start button badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = config.accentColor,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ابدأ",
                            color = Color(0xFF101018),
                            fontSize = (12.5 * fontScale).sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "ابدأ",
                            tint = Color(0xFF101018),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
