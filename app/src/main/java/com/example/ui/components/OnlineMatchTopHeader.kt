package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnlineMatchTopHeader(
    userScore: Int,
    opponentName: String = "كابتن أونلاين ⚔️",
    opponentTagId: String = "ID-48201",
    opponentAvatar: String = "🔥",
    opponentScore: Int,
    fontScale: Float = 1.0f
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, NeonGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Score (Left)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "نقاطك 👤",
                    color = TextSecondary,
                    fontSize = (11 * fontScale).sp
                )
                Text(
                    text = "$userScore",
                    color = NeonGreen,
                    fontSize = (18 * fontScale).sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Live Match Badge (Center)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NeonGreen.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, NeonGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تحدي أونلاين 🌐",
                        color = NeonGreen,
                        fontSize = (11.5 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Opponent Profile & Score (Right)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$opponentName (#$opponentTagId)",
                        color = TextPrimary,
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$opponentScore نقطة",
                        color = GoldPrimary,
                        fontSize = (16 * fontScale).sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(opponentAvatar, fontSize = 20.sp)
            }
        }
    }
}
