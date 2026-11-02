package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
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
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    fontScale: Float = 1.0f
) {
    val topThree = leaderboard.take(3)
    val remaining = leaderboard.drop(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAppBackground)
            .testTag("leaderboard_screen")
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "لوحة صدارة الأصدقاء والمتصدرين",
                        color = TextPrimary,
                        fontSize = (18 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Top 3 Podium
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👑 منصة التتويج الأسبوعية",
                            color = GoldPrimary,
                            fontSize = (15 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // 2nd Place
                            if (topThree.size >= 2) {
                                PodiumColumn(
                                    entry = topThree[1],
                                    badgeColor = Color(0xFFC0C0C0), // Silver
                                    badgeText = "2",
                                    height = 100.dp,
                                    fontScale = fontScale
                                )
                            }

                            // 1st Place (Center and Highest)
                            if (topThree.isNotEmpty()) {
                                PodiumColumn(
                                    entry = topThree[0],
                                    badgeColor = GoldPrimary, // Gold
                                    badgeText = "1 👑",
                                    height = 130.dp,
                                    fontScale = fontScale
                                )
                            }

                            // 3rd Place
                            if (topThree.size >= 3) {
                                PodiumColumn(
                                    entry = topThree[2],
                                    badgeColor = Color(0xFFCD7F32), // Bronze
                                    badgeText = "3",
                                    height = 80.dp,
                                    fontScale = fontScale
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ترتيب باقي اللاعبين والأصدقاء:",
                    color = TextSecondary,
                    fontSize = (13 * fontScale).sp,
                    fontWeight = FontWeight.Medium
                )
            }

            items(remaining) { entry ->
                LeaderboardRow(entry = entry, fontScale = fontScale)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PodiumColumn(
    entry: LeaderboardEntry,
    badgeColor: Color,
    badgeText: String,
    height: androidx.compose.ui.unit.Dp,
    fontScale: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E2C))
                .border(2.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = entry.avatarEmoji, fontSize = 26.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.name,
            color = TextPrimary,
            fontSize = (12 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = "${entry.points} ن",
            color = GoldPrimary,
            fontSize = (11 * fontScale).sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(badgeColor.copy(alpha = 0.25f))
                .border(1.dp, badgeColor, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = (18 * fontScale).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    fontScale: Float
) {
    val isCurrentUser = entry.isCurrentUser

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isCurrentUser) 1.5.dp else 1.dp,
                color = if (isCurrentUser) GoldPrimary else DarkCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("leaderboard_row_${entry.rank}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF383852) else DarkCardSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank number
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        color = if (isCurrentUser) GoldPrimary else TextSecondary,
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(text = entry.avatarEmoji, fontSize = 22.sp)

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.name,
                            color = if (isCurrentUser) GoldPrimary else TextPrimary,
                            fontSize = (14 * fontScale).sp,
                            fontWeight = if (isCurrentUser) FontWeight.Black else FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${entry.favoriteClub} • نسبة الفوز ${entry.weeklyWinRate}",
                        color = TextMuted,
                        fontSize = (11 * fontScale).sp
                    )
                }
            }

            // Points
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1E2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrentUser) GoldPrimary else DarkCardBorder)
            ) {
                Text(
                    text = "${entry.points} نقطة",
                    color = GoldPrimary,
                    fontSize = (12.5 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
