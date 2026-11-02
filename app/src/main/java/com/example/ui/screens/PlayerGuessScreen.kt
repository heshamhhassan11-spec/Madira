package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerGuessQuestion
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
fun PlayerGuessScreen(
    questions: List<PlayerGuessQuestion>,
    onBack: () -> Unit,
    onAddScore: (Int) -> Unit,
    fontScale: Float = 1.0f
) {
    BackHandler { onBack() }

    // Mode state: null = Question count selection screen; Int = Active quiz session
    var activeCountChoice by remember { mutableStateOf<Int?>(null) }
    var activeQuestions by remember { mutableStateOf<List<PlayerGuessQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var hintRevealed by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var sessionCompleted by remember { mutableStateOf(false) }
    var currentPlayMode by remember { mutableStateOf(com.example.data.model.PlayMode.SOLO) }
    var opponentScore by remember { mutableIntStateOf(0) }

    fun startQuizWithCount(count: Int, mode: com.example.data.model.PlayMode = com.example.data.model.PlayMode.SOLO) {
        val countToTake = count.coerceAtMost(questions.size)
        activeQuestions = questions.shuffled().take(countToTake)
        activeCountChoice = countToTake
        currentIndex = 0
        selectedOption = null
        isAnswerSubmitted = false
        hintRevealed = false
        correctCount = 0
        sessionCompleted = false
        currentPlayMode = mode
        opponentScore = 0
    }

    if (activeCountChoice == null) {
        // ==========================================
        // 1. ATTRACTIVE QUESTION LEVEL SELECTION SCREEN
        // ==========================================
        com.example.ui.components.QuestionLevelSelectionView(
            challengeTitle = "خمن اسم اللاعب ⚽",
            challengeSubtitle = "اختبر معرفتك بأساطير ونجوم الساحرة المستديرة مع احتساب 10 نقاط لكل إجابة صحيحة.",
            testTagPrefix = "player_guess",
            fontScale = fontScale,
            onBack = onBack,
            onSelectCount = { count -> startQuizWithCount(count) },
            onSelectCountWithMode = { count, mode -> startQuizWithCount(count, mode) }
        )
    } else if (sessionCompleted) {
        // ==========================================
        // 2. QUIZ COMPLETION SUMMARY
        // ==========================================
        val totalScore = correctCount * 10
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkAppBackground)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(54.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "أحسنت! اكتمل التحدي ⚽",
                color = TextPrimary,
                fontSize = (22 * fontScale).sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "لقد أجبت بشكل صحيح على $correctCount من أصل ${activeQuestions.size} سؤال!",
                color = TextSecondary,
                fontSize = (14 * fontScale).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("النقاط المكتسبة في هذا التحدي:", color = TextSecondary, fontSize = (13 * fontScale).sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("+$totalScore نقطة", color = GoldPrimary, fontSize = (32 * fontScale).sp, fontWeight = FontWeight.Black)
                    Text("تمت إضافة النقاط بنجاح إلى رصيدك العام 🌟", color = NeonGreen, fontSize = (11.5 * fontScale).sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { startQuizWithCount(activeCountChoice ?: 10) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعادة التحدي بنفس العدد", color = Color.Black, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { activeCountChoice = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تغيير عدد الأسئلة", color = GoldPrimary, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2418)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("العودة لقائمة التحديات الرئيسية", color = GoldPrimary, fontSize = (13 * fontScale).sp)
            }
        }
    } else {
        // ==========================================
        // 3. ACTIVE QUIZ PLAY SCREEN
        // ==========================================
        val currentQuestion = activeQuestions.getOrNull(currentIndex) ?: activeQuestions.first()
        val isCorrect = selectedOption == currentQuestion.correctAnswer

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkAppBackground)
                .testTag("player_guess_play_screen")
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "خمن اسم اللاعب (${currentIndex + 1}/${activeQuestions.size})",
                        color = TextPrimary,
                        fontSize = (16 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activeCountChoice = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "تغيير", tint = GoldPrimary)
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E1E2C),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "+${correctCount * 10} ن",
                            color = GoldPrimary,
                            fontSize = (12 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
            )

            if (currentPlayMode == com.example.data.model.PlayMode.ONLINE) {
                com.example.ui.components.OnlineMatchTopHeader(
                    userScore = correctCount * 10,
                    opponentName = "كابتن أونلاين ⚔️",
                    opponentTagId = "ID-48201",
                    opponentAvatar = "🔥",
                    opponentScore = opponentScore,
                    fontScale = fontScale
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / activeQuestions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = GoldPrimary,
                trackColor = DarkCardBorder
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Difficulty Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (currentQuestion.difficulty) {
                            com.example.data.model.ChallengeDifficulty.EASY -> NeonGreen.copy(alpha = 0.2f)
                            com.example.data.model.ChallengeDifficulty.MEDIUM -> GoldPrimary.copy(alpha = 0.2f)
                            com.example.data.model.ChallengeDifficulty.HARD -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            com.example.data.model.ChallengeDifficulty.LEGEND -> Color(0xFFBA68C8).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = "المستوى: ${currentQuestion.difficulty.labelAr}",
                            color = when (currentQuestion.difficulty) {
                                com.example.data.model.ChallengeDifficulty.EASY -> NeonGreen
                                com.example.data.model.ChallengeDifficulty.MEDIUM -> GoldPrimary
                                com.example.data.model.ChallengeDifficulty.HARD -> Color(0xFFFF9800)
                                com.example.data.model.ChallengeDifficulty.LEGEND -> Color(0xFFBA68C8)
                            },
                            fontSize = (11 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "القيمة: 10 نقاط",
                        color = GoldPrimary,
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clues Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بيانات ومسيرة اللاعب الغامض:",
                                color = TextPrimary,
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clue Rows
                        ClueDetailRow("الجنسية", currentQuestion.hintNationality, fontScale)
                        ClueDetailRow("المركز في الملعب", currentQuestion.hintPosition, fontScale)
                        ClueDetailRow("الرقم المفضل", "#${currentQuestion.hintNumber}", fontScale)
                        ClueDetailRow("أبرز الأندية التي لعب لها", currentQuestion.hintClubs.joinToString(" ⬅️ "), fontScale)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Special Clue Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1E2C),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بصمة تاريخية مميزة:", color = GoldPrimary, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentQuestion.specialClue,
                                    color = TextSecondary,
                                    fontSize = (12.5 * fontScale).sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Options
                Text(
                    text = "من هو هذا اللاعب؟",
                    color = TextPrimary,
                    fontSize = (15 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                currentQuestion.options.forEach { option ->
                    val isSelected = selectedOption == option
                    val isCorrectOption = option == currentQuestion.correctAnswer

                    val cardBg = when {
                        !isAnswerSubmitted && isSelected -> GoldPrimary.copy(alpha = 0.2f)
                        isAnswerSubmitted && isCorrectOption -> NeonGreen.copy(alpha = 0.25f)
                        isAnswerSubmitted && isSelected && !isCorrectOption -> Color(0xFFE53935).copy(alpha = 0.25f)
                        else -> DarkCardSurface
                    }

                    val borderColor = when {
                        !isAnswerSubmitted && isSelected -> GoldPrimary
                        isAnswerSubmitted && isCorrectOption -> NeonGreen
                        isAnswerSubmitted && isSelected && !isCorrectOption -> Color(0xFFE53935)
                        else -> DarkCardBorder
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable(enabled = !isAnswerSubmitted) {
                                selectedOption = option
                            }
                            .testTag("option_$option"),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                color = if (isAnswerSubmitted && isCorrectOption) NeonGreen else TextPrimary,
                                fontSize = (14.5 * fontScale).sp,
                                fontWeight = if (isSelected || (isAnswerSubmitted && isCorrectOption)) FontWeight.Bold else FontWeight.Normal
                            )

                            if (isAnswerSubmitted) {
                                if (isCorrectOption) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "إجابة صحيحة", tint = NeonGreen)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = "إجابة خاطئة", tint = Color(0xFFE53935))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                if (!isAnswerSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                isAnswerSubmitted = true
                                if (selectedOption == currentQuestion.correctAnswer) {
                                    correctCount++
                                    onAddScore(10) // Exactly 10 points per correct answer
                                }
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_submit_player_answer"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "تأكيد الإجابة",
                            color = Color(0xFF1E1E2C),
                            fontSize = (14 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentIndex + 1 < activeQuestions.size) {
                                currentIndex++
                                selectedOption = null
                                isAnswerSubmitted = false
                                hintRevealed = false
                            } else {
                                sessionCompleted = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_next_player_question"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentIndex + 1 < activeQuestions.size) "السؤال التالي" else "عرض نتيجة التحدي 🏆",
                                color = Color.Black,
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClueDetailRow(label: String, value: String, fontScale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = (12 * fontScale).sp)
        Text(text = value, color = TextPrimary, fontSize = (13 * fontScale).sp, fontWeight = FontWeight.SemiBold)
    }
}
