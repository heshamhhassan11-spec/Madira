package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClubBadgeView(
    badgeSymbol: String,
    primaryColor: Color,
    secondaryColor: Color,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14141E))
            .border(1.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.88f)) {
            val w = this.size.width
            val h = this.size.height

            when (badgeSymbol) {
                "real_madrid" -> drawRealMadridBadge(w, h)
                "barcelona" -> drawBarcelonaBadge(w, h)
                "ahly" -> drawAhlyBadge(w, h)
                "zamalek" -> drawZamalekBadge(w, h)
                "liverpool" -> drawLiverpoolBadge(w, h)
                "man_city" -> drawManCityBadge(w, h)
                "bayern" -> drawBayernBadge(w, h)
                "psg" -> drawPsgBadge(w, h)
                "juventus" -> drawJuventusBadge(w, h)
                "arsenal" -> drawArsenalBadge(w, h)
                "hilal" -> drawHilalBadge(w, h)
                "nassr" -> drawNassrBadge(w, h)
                "inter" -> drawInterBadge(w, h)
                "milan" -> drawMilanBadge(w, h)
                "chelsea" -> drawChelseaBadge(w, h)
                "dortmund" -> drawDortmundBadge(w, h)
                "atletico" -> drawAtleticoBadge(w, h)
                "ittihad" -> drawIttihadBadge(w, h)
                "pyramids" -> drawPyramidsBadge(w, h)
                "man_united" -> drawManUnitedBadge(w, h)
                "tottenham" -> drawTottenhamBadge(w, h)
                "roma" -> drawRomaBadge(w, h)
                "napoli" -> drawNapoliBadge(w, h)
                "porto" -> drawPortoBadge(w, h)
                "benfica" -> drawBenficaBadge(w, h)
                "sporting" -> drawSportingBadge(w, h)
                "inter_miami" -> drawInterMiamiBadge(w, h)
                "galatasaray" -> drawGalatasarayBadge(w, h)
                "fenerbahce" -> drawFenerbahceBadge(w, h)
                "besiktas" -> drawBesiktasBadge(w, h)
                else -> drawGenericClubBadge(w, h, primaryColor, secondaryColor)
            }
        }
    }
}

// ==========================================
// ACCURATE CLUB CREST DRAWINGS
// ==========================================

private fun DrawScope.drawRealMadridBadge(w: Float, h: Float) {
    // Golden crown on top
    val crownPath = Path().apply {
        moveTo(w * 0.25f, h * 0.22f)
        lineTo(w * 0.35f, h * 0.08f)
        lineTo(w * 0.5f, h * 0.16f)
        lineTo(w * 0.65f, h * 0.08f)
        lineTo(w * 0.75f, h * 0.22f)
        close()
    }
    drawPath(crownPath, color = Color(0xFFFFD700), style = Fill)
    drawPath(crownPath, color = Color(0xFFCC9900), style = Stroke(width = 2f))
    drawCircle(Color(0xFFE53935), radius = w * 0.03f, center = Offset(w * 0.5f, h * 0.08f))

    // Main Circle body
    val center = Offset(w * 0.5f, h * 0.60f)
    val radius = w * 0.35f
    drawCircle(Color(0xFFEEEEEE), radius = radius, center = center)
    drawCircle(Color(0xFFFFD700), radius = radius, center = center, style = Stroke(width = 5f))

    // Purple diagonal sash
    val sashPath = Path().apply {
        moveTo(center.x - radius * 0.7f, center.y - radius * 0.7f)
        lineTo(center.x - radius * 0.3f, center.y - radius * 0.95f)
        lineTo(center.x + radius * 0.95f, center.y + radius * 0.3f)
        lineTo(center.x + radius * 0.7f, center.y + radius * 0.7f)
        close()
    }
    drawPath(sashPath, color = Color(0xFF5E35B1))

    // Inner golden monogram circle
    drawCircle(Color(0xFFFFD700), radius = radius * 0.5f, center = center, style = Stroke(width = 3.5f))
}

private fun DrawScope.drawBarcelonaBadge(w: Float, h: Float) {
    // Shield outer shape
    val shield = Path().apply {
        moveTo(w * 0.15f, h * 0.15f)
        lineTo(w * 0.85f, h * 0.15f)
        lineTo(w * 0.85f, h * 0.55f)
        cubicTo(w * 0.85f, h * 0.82f, w * 0.60f, h * 0.92f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.40f, h * 0.92f, w * 0.15f, h * 0.82f, w * 0.15f, h * 0.55f)
        close()
    }
    drawPath(shield, color = Color(0xFFFFD700), style = Fill)
    drawPath(shield, color = Color(0xFF8B0000), style = Stroke(width = 4f))

    // Upper Left: St George's red cross on white
    drawRect(Color.White, topLeft = Offset(w * 0.18f, h * 0.18f), size = Size(w * 0.30f, h * 0.30f))
    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.30f, h * 0.18f), size = Size(w * 0.06f, h * 0.30f))
    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.18f, h * 0.30f), size = Size(w * 0.30f, h * 0.06f))

    // Upper Right: Senyera Catalan yellow/red stripes
    drawRect(Color(0xFFFFEB3B), topLeft = Offset(w * 0.52f, h * 0.18f), size = Size(w * 0.30f, h * 0.30f))
    for (i in 0..3) {
        drawRect(Color(0xFFE53935), topLeft = Offset(w * (0.55f + i * 0.07f), h * 0.18f), size = Size(w * 0.035f, h * 0.30f))
    }

    // Horizontal golden bar in the middle
    drawRect(Color(0xFFFFD700), topLeft = Offset(w * 0.18f, h * 0.48f), size = Size(w * 0.64f, h * 0.08f))

    // Lower half: Blaugrana vertical stripes (Blue and Maroon)
    val stripeColors = listOf(Color(0xFF004D98), Color(0xFFA50044), Color(0xFF004D98), Color(0xFFA50044))
    stripeColors.forEachIndexed { idx, color ->
        drawRect(color, topLeft = Offset(w * (0.22f + idx * 0.14f), h * 0.56f), size = Size(w * 0.14f, h * 0.30f))
    }

    // Vintage golden ball at center bottom
    drawCircle(Color(0xFFFFC107), radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.72f))
    drawCircle(Color(0xFF5D4037), radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.72f), style = Stroke(width = 2f))
}

private fun DrawScope.drawAhlyBadge(w: Float, h: Float) {
    // Red shield with black trim
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.60f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.55f, h * 0.95f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.45f, h * 0.95f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.60f)
        close()
    }
    drawPath(shield, color = Color(0xFFD32F2F))
    drawPath(shield, color = Color(0xFFFFD700), style = Stroke(width = 4f))

    // 4 Gold Stars on Top (representing 40+ Egyptian League titles)
    for (i in 0..3) {
        drawCircle(Color(0xFFFFD700), radius = w * 0.025f, center = Offset(w * (0.32f + i * 0.12f), h * 0.10f))
    }

    // The soaring Golden & White Eagle
    val eagleBody = Path().apply {
        moveTo(w * 0.50f, h * 0.32f) // Head
        lineTo(w * 0.78f, h * 0.42f) // Right wing tip
        lineTo(w * 0.58f, h * 0.55f)
        lineTo(w * 0.62f, h * 0.75f) // Tail
        lineTo(w * 0.50f, h * 0.65f)
        lineTo(w * 0.38f, h * 0.75f)
        lineTo(w * 0.42f, h * 0.55f)
        lineTo(w * 0.22f, h * 0.42f) // Left wing tip
        close()
    }
    drawPath(eagleBody, color = Color.White)
    drawPath(eagleBody, color = Color(0xFFFFD700), style = Stroke(width = 2.5f))

    // Lower African Golden Stars
    drawCircle(Color(0xFFFFD700), radius = w * 0.03f, center = Offset(w * 0.50f, h * 0.85f))
    drawCircle(Color(0xFFFFD700), radius = w * 0.025f, center = Offset(w * 0.38f, h * 0.82f))
    drawCircle(Color(0xFFFFD700), radius = w * 0.025f, center = Offset(w * 0.62f, h * 0.82f))
}

private fun DrawScope.drawZamalekBadge(w: Float, h: Float) {
    // Pure White Shield with golden border
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.58f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.55f, h * 0.95f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.45f, h * 0.95f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.58f)
        close()
    }
    drawPath(shield, color = Color.White)
    drawPath(shield, color = Color(0xFFC0392B), style = Stroke(width = 3.5f))

    // Two Iconic Red Horizontal Stripes
    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.20f, h * 0.32f), size = Size(w * 0.60f, h * 0.06f))
    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.20f, h * 0.44f), size = Size(w * 0.60f, h * 0.06f))

    // Pharaonic Archer (The Iconic Bowman)
    val archerBody = Path().apply {
        // Head
        addOval(androidx.compose.ui.geometry.Rect(w * 0.46f, h * 0.52f, w * 0.54f, h * 0.60f))
        // Torso & Aiming Bow
        moveTo(w * 0.50f, h * 0.60f)
        lineTo(w * 0.50f, h * 0.75f)
        lineTo(w * 0.40f, h * 0.85f)
        moveTo(w * 0.50f, h * 0.75f)
        lineTo(w * 0.60f, h * 0.85f)
    }
    drawPath(archerBody, color = Color(0xFFC0392B), style = Stroke(width = 3.5f))

    // The Bow arc
    val bow = Path().apply {
        moveTo(w * 0.65f, h * 0.55f)
        cubicTo(w * 0.72f, h * 0.64f, w * 0.72f, h * 0.72f, w * 0.65f, h * 0.80f)
    }
    drawPath(bow, color = Color(0xFFC0392B), style = Stroke(width = 3f))

    // Golden Arrow
    drawLine(Color(0xFFFFD700), start = Offset(w * 0.42f, h * 0.67f), end = Offset(w * 0.74f, h * 0.67f), strokeWidth = 3f)
}

private fun DrawScope.drawLiverpoolBadge(w: Float, h: Float) {
    // Red shield with teal/green Shankly gates top
    val shield = Path().apply {
        moveTo(w * 0.20f, h * 0.22f)
        lineTo(w * 0.80f, h * 0.22f)
        lineTo(w * 0.80f, h * 0.65f)
        cubicTo(w * 0.80f, h * 0.85f, w * 0.55f, h * 0.95f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.45f, h * 0.95f, w * 0.20f, h * 0.85f, w * 0.20f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFFC8102E))
    drawPath(shield, color = Color(0xFFFFD700), style = Stroke(width = 3.5f))

    // Top Shankly Gates (Teal green)
    drawRect(Color(0xFF00B2A9), topLeft = Offset(w * 0.25f, h * 0.12f), size = Size(w * 0.50f, h * 0.08f))

    // Liver Bird in Gold/White
    val bird = Path().apply {
        moveTo(w * 0.50f, h * 0.36f)
        lineTo(w * 0.56f, h * 0.42f)
        lineTo(w * 0.62f, h * 0.55f)
        lineTo(w * 0.55f, h * 0.72f)
        lineTo(w * 0.50f, h * 0.78f)
        lineTo(w * 0.45f, h * 0.72f)
        lineTo(w * 0.38f, h * 0.55f)
        lineTo(w * 0.44f, h * 0.42f)
        close()
    }
    drawPath(bird, color = Color(0xFFFFD700))

    // Twin Eternal Flames on sides
    drawCircle(Color(0xFFFF9800), radius = w * 0.04f, center = Offset(w * 0.12f, h * 0.45f))
    drawCircle(Color(0xFFFF9800), radius = w * 0.04f, center = Offset(w * 0.88f, h * 0.45f))
}

private fun DrawScope.drawManCityBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f

    // Outer sky blue roundel
    drawCircle(Color(0xFF6CABDD), radius = r, center = center)
    drawCircle(Color.White, radius = r * 0.85f, center = center)
    drawCircle(Color(0xFF1C2C5B), radius = r * 0.70f, center = center)

    // Inner shield: Ship and Red Rose
    val shield = Path().apply {
        moveTo(w * 0.32f, h * 0.35f)
        lineTo(w * 0.68f, h * 0.35f)
        lineTo(w * 0.68f, h * 0.62f)
        cubicTo(w * 0.68f, h * 0.75f, w * 0.50f, h * 0.82f, w * 0.50f, h * 0.82f)
        cubicTo(w * 0.50f, h * 0.82f, w * 0.32f, h * 0.75f, w * 0.32f, h * 0.62f)
        close()
    }
    drawPath(shield, color = Color(0xFF6CABDD))

    // Golden Ship in upper half
    val ship = Path().apply {
        moveTo(w * 0.38f, h * 0.48f)
        lineTo(w * 0.62f, h * 0.48f)
        lineTo(w * 0.58f, h * 0.54f)
        lineTo(w * 0.42f, h * 0.54f)
        close()
    }
    drawPath(ship, color = Color(0xFFFFD700))

    // Red Rose of Lancashire
    drawCircle(Color(0xFFE53935), radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.68f))
}

private fun DrawScope.drawBayernBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f

    // Red outer ring
    drawCircle(Color(0xFFDC052D), radius = r, center = center)
    drawCircle(Color(0xFF0066B2), radius = r * 0.90f, center = center, style = Stroke(width = 4f))

    // White circle inside
    drawCircle(Color.White, radius = r * 0.65f, center = center)

    // Bavarian Blue and White Diamonds Checkers
    val diamondCenter = center
    val diamondR = r * 0.55f
    drawCircle(Color(0xFF0066B2), radius = diamondR, center = diamondCenter)

    // Draw white diamond pattern
    for (i in -3..3) {
        drawLine(
            color = Color.White,
            start = Offset(center.x + i * 15f - diamondR * 0.7f, center.y - diamondR * 0.7f),
            end = Offset(center.x + i * 15f + diamondR * 0.7f, center.y + diamondR * 0.7f),
            strokeWidth = 5f
        )
        drawLine(
            color = Color.White,
            start = Offset(center.x - i * 15f + diamondR * 0.7f, center.y - diamondR * 0.7f),
            end = Offset(center.x - i * 15f - diamondR * 0.7f, center.y + diamondR * 0.7f),
            strokeWidth = 5f
        )
    }
}

private fun DrawScope.drawPsgBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f

    // Navy Blue Circle
    drawCircle(Color(0xFF004170), radius = r, center = center)
    drawCircle(Color(0xFFDA291C), radius = r * 0.88f, center = center, style = Stroke(width = 4f))

    // Red Eiffel Tower silhouette
    val tower = Path().apply {
        moveTo(w * 0.50f, h * 0.22f)
        lineTo(w * 0.53f, h * 0.50f)
        lineTo(w * 0.64f, h * 0.78f)
        lineTo(w * 0.58f, h * 0.78f)
        lineTo(w * 0.50f, h * 0.62f)
        lineTo(w * 0.42f, h * 0.78f)
        lineTo(w * 0.36f, h * 0.78f)
        lineTo(w * 0.47f, h * 0.50f)
        close()
    }
    drawPath(tower, color = Color(0xFFDA291C))

    // Golden Fleur-de-lis at base
    drawCircle(Color(0xFFFFD700), radius = w * 0.05f, center = Offset(w * 0.50f, h * 0.70f))
}

private fun DrawScope.drawJuventusBadge(w: Float, h: Float) {
    // Deep black shield
    val shield = Path().apply {
        moveTo(w * 0.20f, h * 0.12f)
        lineTo(w * 0.80f, h * 0.12f)
        lineTo(w * 0.80f, h * 0.65f)
        cubicTo(w * 0.80f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.20f, h * 0.85f, w * 0.20f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFF0F0F16))
    drawPath(shield, color = Color.White, style = Stroke(width = 3f))

    // Modern iconic "JJ" lines in pure white
    drawLine(Color.White, start = Offset(w * 0.38f, h * 0.25f), end = Offset(w * 0.38f, h * 0.68f), strokeWidth = 14f)
    drawLine(Color.White, start = Offset(w * 0.38f, h * 0.68f), end = Offset(w * 0.46f, h * 0.76f), strokeWidth = 14f)

    drawLine(Color.White, start = Offset(w * 0.58f, h * 0.25f), end = Offset(w * 0.58f, h * 0.68f), strokeWidth = 14f)
    drawLine(Color.White, start = Offset(w * 0.58f, h * 0.68f), end = Offset(w * 0.66f, h * 0.76f), strokeWidth = 14f)

    // 3 Golden Stars on top
    for (i in 0..2) {
        drawCircle(Color(0xFFFFD700), radius = w * 0.025f, center = Offset(w * (0.36f + i * 0.14f), h * 0.08f))
    }
}

private fun DrawScope.drawArsenalBadge(w: Float, h: Float) {
    // Red shield with navy/gold trim
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.62f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.62f)
        close()
    }
    drawPath(shield, color = Color(0xFFDB0007))
    drawPath(shield, color = Color(0xFF023474), style = Stroke(width = 4f))

    // Golden Cannon pointing right
    val barrel = Path().apply {
        moveTo(w * 0.28f, h * 0.52f)
        lineTo(w * 0.74f, h * 0.48f)
        lineTo(w * 0.74f, h * 0.56f)
        lineTo(w * 0.28f, h * 0.58f)
        close()
    }
    drawPath(barrel, color = Color(0xFFFFD700))

    // Wheel of cannon
    drawCircle(Color(0xFFFFD700), radius = w * 0.12f, center = Offset(w * 0.42f, h * 0.62f))
    drawCircle(Color(0xFF023474), radius = w * 0.05f, center = Offset(w * 0.42f, h * 0.62f))
}

private fun DrawScope.drawHilalBadge(w: Float, h: Float) {
    // Royal Blue Shield
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.62f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.62f)
        close()
    }
    drawPath(shield, color = Color(0xFF0038A8))
    drawPath(shield, color = Color.White, style = Stroke(width = 3.5f))

    // White Crescent Moon wrapped around ball
    val crescent = Path().apply {
        moveTo(w * 0.58f, h * 0.32f)
        cubicTo(w * 0.32f, h * 0.40f, w * 0.32f, h * 0.70f, w * 0.58f, h * 0.78f)
        cubicTo(w * 0.42f, h * 0.68f, w * 0.42f, h * 0.42f, w * 0.58f, h * 0.32f)
        close()
    }
    drawPath(crescent, color = Color.White)

    // Silver/White Soccer Ball
    drawCircle(Color.White, radius = w * 0.10f, center = Offset(w * 0.56f, h * 0.55f))
    drawCircle(Color(0xFF0038A8), radius = w * 0.04f, center = Offset(w * 0.56f, h * 0.55f))
}

private fun DrawScope.drawNassrBadge(w: Float, h: Float) {
    // Golden Yellow Shield with Royal Blue Crown
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.22f)
        lineTo(w * 0.82f, h * 0.22f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFFFFD100))
    drawPath(shield, color = Color(0xFF002D72), style = Stroke(width = 3.5f))

    // Palm Tree and Crossed Swords in Blue
    // Palm fronds
    drawCircle(Color(0xFF002D72), radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.45f))
    drawLine(Color(0xFF002D72), start = Offset(w * 0.50f, h * 0.45f), end = Offset(w * 0.50f, h * 0.65f), strokeWidth = 4f)

    // Crossed Swords
    drawLine(Color(0xFF002D72), start = Offset(w * 0.35f, h * 0.68f), end = Offset(w * 0.65f, h * 0.78f), strokeWidth = 3f)
    drawLine(Color(0xFF002D72), start = Offset(w * 0.65f, h * 0.68f), end = Offset(w * 0.35f, h * 0.78f), strokeWidth = 3f)

    // Royal Blue Crown on top
    val crown = Path().apply {
        moveTo(w * 0.35f, h * 0.20f)
        lineTo(w * 0.42f, h * 0.10f)
        lineTo(w * 0.50f, h * 0.16f)
        lineTo(w * 0.58f, h * 0.10f)
        lineTo(w * 0.65f, h * 0.20f)
        close()
    }
    drawPath(crown, color = Color(0xFF002D72))
}

private fun DrawScope.drawInterBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f
    drawCircle(Color(0xFF001EA0), radius = r, center = center)
    drawCircle(Color.Black, radius = r * 0.88f, center = center)
    drawCircle(Color(0xFF001EA0), radius = r * 0.70f, center = center)

    // Interlocking "IM" in white & gold
    drawLine(Color.White, start = Offset(w * 0.40f, h * 0.32f), end = Offset(w * 0.40f, h * 0.68f), strokeWidth = 8f)
    drawLine(Color.White, start = Offset(w * 0.40f, h * 0.32f), end = Offset(w * 0.50f, h * 0.50f), strokeWidth = 8f)
    drawLine(Color.White, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.60f, h * 0.32f), strokeWidth = 8f)
    drawLine(Color.White, start = Offset(w * 0.60f, h * 0.32f), end = Offset(w * 0.60f, h * 0.68f), strokeWidth = 8f)
}

private fun DrawScope.drawMilanBadge(w: Float, h: Float) {
    val shield = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(w * 0.22f, h * 0.12f, w * 0.78f, h * 0.88f))
    }
    drawPath(shield, color = Color.White)
    drawPath(shield, color = Color.Black, style = Stroke(width = 3.5f))

    // Left Half: Red and Black Stripes
    for (i in 0..2) {
        drawRect(Color(0xFFFB090B), topLeft = Offset(w * (0.26f + i * 0.08f), h * 0.20f), size = Size(w * 0.04f, h * 0.60f))
        drawRect(Color.Black, topLeft = Offset(w * (0.30f + i * 0.08f), h * 0.20f), size = Size(w * 0.04f, h * 0.60f))
    }

    // Right Half: St George's Cross
    drawRect(Color.White, topLeft = Offset(w * 0.50f, h * 0.20f), size = Size(w * 0.25f, h * 0.60f))
    drawRect(Color(0xFFFB090B), topLeft = Offset(w * 0.60f, h * 0.20f), size = Size(w * 0.05f, h * 0.60f))
    drawRect(Color(0xFFFB090B), topLeft = Offset(w * 0.50f, h * 0.47f), size = Size(w * 0.25f, h * 0.06f))

    // Gold Star on Top
    drawCircle(Color(0xFFFFD700), radius = w * 0.035f, center = Offset(w * 0.50f, h * 0.08f))
}

private fun DrawScope.drawChelseaBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f
    drawCircle(Color(0xFF034694), radius = r, center = center)
    drawCircle(Color.White, radius = r * 0.85f, center = center)
    drawCircle(Color(0xFF034694), radius = r * 0.70f, center = center, style = Stroke(width = 4f))

    // Golden/White Rampant Lion holding staff
    val lion = Path().apply {
        moveTo(w * 0.50f, h * 0.32f)
        lineTo(w * 0.58f, h * 0.40f)
        lineTo(w * 0.52f, h * 0.52f)
        lineTo(w * 0.60f, h * 0.68f)
        lineTo(w * 0.45f, h * 0.68f)
        close()
    }
    drawPath(lion, color = Color(0xFF034694))
    drawLine(Color(0xFFE53935), start = Offset(w * 0.42f, h * 0.30f), end = Offset(w * 0.42f, h * 0.70f), strokeWidth = 3f)
}

private fun DrawScope.drawDortmundBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f
    drawCircle(Color(0xFFFDE100), radius = r, center = center)
    drawCircle(Color.Black, radius = r, center = center, style = Stroke(width = 5f))

    // "BVB 09" in bold black
    drawCircle(Color.Black, radius = w * 0.08f, center = Offset(w * 0.38f, h * 0.45f), style = Stroke(width = 6f))
    drawCircle(Color.Black, radius = w * 0.08f, center = Offset(w * 0.62f, h * 0.45f), style = Stroke(width = 6f))
    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.62f), style = Stroke(width = 5f))

    // 2 Championship stars on top
    drawCircle(Color(0xFFFFD700), radius = w * 0.03f, center = Offset(w * 0.42f, h * 0.12f))
    drawCircle(Color(0xFFFFD700), radius = w * 0.03f, center = Offset(w * 0.58f, h * 0.12f))
}

private fun DrawScope.drawAtleticoBadge(w: Float, h: Float) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFF272E61))

    // Lower half: Red and White Stripes
    for (i in 0..4) {
        val color = if (i % 2 == 0) Color(0xFFCB3524) else Color.White
        drawRect(color, topLeft = Offset(w * (0.24f + i * 0.11f), h * 0.45f), size = Size(w * 0.11f, h * 0.45f))
    }

    // Upper Blue area with Bear and Strawberry Tree
    drawCircle(Color(0xFF272E61), radius = w * 0.12f, center = Offset(w * 0.40f, h * 0.28f))
    drawCircle(Color(0xFF2E7D32), radius = w * 0.06f, center = Offset(w * 0.58f, h * 0.25f)) // Tree
}

private fun DrawScope.drawIttihadBadge(w: Float, h: Float) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFFFFD100))
    drawPath(shield, color = Color.Black, style = Stroke(width = 4f))

    // Black stripes
    drawRect(Color.Black, topLeft = Offset(w * 0.32f, h * 0.25f), size = Size(w * 0.08f, h * 0.55f))
    drawRect(Color.Black, topLeft = Offset(w * 0.48f, h * 0.25f), size = Size(w * 0.08f, h * 0.55f))
    drawRect(Color.Black, topLeft = Offset(w * 0.64f, h * 0.25f), size = Size(w * 0.08f, h * 0.55f))

    // Roaring Tiger head silhouette
    drawCircle(Color.Black, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.50f))
    drawCircle(Color(0xFFFFD100), radius = w * 0.04f, center = Offset(w * 0.45f, h * 0.48f))
    drawCircle(Color(0xFFFFD100), radius = w * 0.04f, center = Offset(w * 0.55f, h * 0.48f))
}

private fun DrawScope.drawPyramidsBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f
    drawCircle(Color(0xFF0F1E36), radius = r, center = center)
    drawCircle(Color(0xFF29B6F6), radius = r, center = center, style = Stroke(width = 3.5f))

    // 3 Golden Pyramids
    val pyr1 = Path().apply {
        moveTo(w * 0.50f, h * 0.28f)
        lineTo(w * 0.72f, h * 0.68f)
        lineTo(w * 0.28f, h * 0.68f)
        close()
    }
    drawPath(pyr1, color = Color(0xFFFFD700))

    val pyr2 = Path().apply {
        moveTo(w * 0.50f, h * 0.28f)
        lineTo(w * 0.50f, h * 0.68f)
        lineTo(w * 0.72f, h * 0.68f)
        close()
    }
    drawPath(pyr2, color = Color(0xFFC5A000))
}

private fun DrawScope.drawManUnitedBadge(w: Float, h: Float) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFFFFD100))
    drawPath(shield, color = Color(0xFFC70101), style = Stroke(width = 4f))

    // Ship on top
    val ship = Path().apply {
        moveTo(w * 0.35f, h * 0.25f)
        lineTo(w * 0.65f, h * 0.25f)
        lineTo(w * 0.60f, h * 0.32f)
        lineTo(w * 0.40f, h * 0.32f)
        close()
    }
    drawPath(ship, color = Color(0xFFC70101))

    // Red Devil with pitchfork
    drawCircle(Color(0xFFC70101), radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.58f))
    drawLine(Color(0xFFC70101), start = Offset(w * 0.32f, h * 0.45f), end = Offset(w * 0.32f, h * 0.75f), strokeWidth = 3f)
}

private fun DrawScope.drawTottenhamBadge(w: Float, h: Float) {
    // Elegant cockerel on classic ball
    drawCircle(Color(0xFF132257), radius = w * 0.45f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 3f))

    // Cockerel silhouette in navy
    val cock = Path().apply {
        moveTo(w * 0.50f, h * 0.20f)
        lineTo(w * 0.58f, h * 0.28f)
        lineTo(w * 0.52f, h * 0.48f)
        lineTo(w * 0.46f, h * 0.62f)
        lineTo(w * 0.42f, h * 0.48f)
        close()
    }
    drawPath(cock, color = Color(0xFF132257))

    // Ball underneath
    drawCircle(Color(0xFF132257), radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.75f), style = Stroke(width = 3f))
}

private fun DrawScope.drawRomaBadge(w: Float, h: Float) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFF8E1F2F)) // Imperial red

    // Golden yellow bottom half
    drawRect(Color(0xFFF0BC42), topLeft = Offset(w * 0.20f, h * 0.50f), size = Size(w * 0.60f, h * 0.45f))

    // Capitoline She-Wolf silhouette in upper half
    drawCircle(Color(0xFF212121), radius = w * 0.10f, center = Offset(w * 0.50f, h * 0.35f))
}

private fun DrawScope.drawNapoliBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.45f
    drawCircle(Color(0xFF003B6F), radius = r, center = center)
    drawCircle(Color(0xFF12A0D7), radius = r * 0.90f, center = center)
    drawCircle(Color.White, radius = r * 0.75f, center = center, style = Stroke(width = 3f))

    // Bold "N"
    drawLine(Color.White, start = Offset(w * 0.38f, h * 0.30f), end = Offset(w * 0.38f, h * 0.70f), strokeWidth = 10f)
    drawLine(Color.White, start = Offset(w * 0.38f, h * 0.30f), end = Offset(w * 0.62f, h * 0.70f), strokeWidth = 10f)
    drawLine(Color.White, start = Offset(w * 0.62f, h * 0.30f), end = Offset(w * 0.62f, h * 0.70f), strokeWidth = 10f)
}

private fun DrawScope.drawPortoBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(Color(0xFF0038A8), radius = w * 0.42f, center = center)
    drawCircle(Color.White, radius = w * 0.38f, center = center)

    // Portuguese red/green arms in center
    drawRect(Color(0xFF006600), topLeft = Offset(w * 0.38f, h * 0.35f), size = Size(w * 0.12f, h * 0.30f))
    drawRect(Color(0xFFCC0000), topLeft = Offset(w * 0.50f, h * 0.35f), size = Size(w * 0.12f, h * 0.30f))

    // Golden Dragon on top
    val dragon = Path().apply {
        moveTo(w * 0.50f, h * 0.15f)
        lineTo(w * 0.62f, h * 0.22f)
        lineTo(w * 0.55f, h * 0.30f)
        lineTo(w * 0.45f, h * 0.30f)
        lineTo(w * 0.38f, h * 0.22f)
        close()
    }
    drawPath(dragon, color = Color(0xFFFFD700))
}

private fun DrawScope.drawBenficaBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(Color(0xFFE53935), radius = w * 0.42f, center = center)
    drawCircle(Color.White, radius = w * 0.38f, center = center)

    // Bicycle wheel spokes
    for (i in 0..5) {
        val angle = i * (Math.PI / 3)
        drawLine(
            Color(0xFF757575),
            start = center,
            end = Offset((center.x + w * 0.32f * Math.cos(angle)).toFloat(), (center.y + w * 0.32f * Math.sin(angle)).toFloat()),
            strokeWidth = 2f
        )
    }

    // Eagle with open wings on top
    val eagle = Path().apply {
        moveTo(w * 0.50f, h * 0.15f)
        lineTo(w * 0.72f, h * 0.25f)
        lineTo(w * 0.50f, h * 0.32f)
        lineTo(w * 0.28f, h * 0.25f)
        close()
    }
    drawPath(eagle, color = Color(0xFFFFD700))
}

private fun DrawScope.drawSportingBadge(w: Float, h: Float) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color(0xFF006633)) // Green

    // Horizontal white stripes
    drawRect(Color.White, topLeft = Offset(w * 0.20f, h * 0.32f), size = Size(w * 0.60f, h * 0.08f))
    drawRect(Color.White, topLeft = Offset(w * 0.20f, h * 0.52f), size = Size(w * 0.60f, h * 0.08f))

    // Golden Rampant Lion in center
    val lion = Path().apply {
        moveTo(w * 0.50f, h * 0.35f)
        lineTo(w * 0.58f, h * 0.45f)
        lineTo(w * 0.52f, h * 0.65f)
        lineTo(w * 0.44f, h * 0.65f)
        close()
    }
    drawPath(lion, color = Color(0xFFFFD700))
}

private fun DrawScope.drawInterMiamiBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(Color.Black, radius = w * 0.44f, center = center)
    drawCircle(Color(0xFFF7B5CD), radius = w * 0.40f, center = center, style = Stroke(width = 3.5f))

    // Twin white Herons forming "M"
    val herons = Path().apply {
        moveTo(w * 0.35f, h * 0.35f)
        lineTo(w * 0.50f, h * 0.52f)
        lineTo(w * 0.65f, h * 0.35f)
        lineTo(w * 0.60f, h * 0.70f)
        lineTo(w * 0.50f, h * 0.62f)
        lineTo(w * 0.40f, h * 0.70f)
        close()
    }
    drawPath(herons, color = Color.White)

    // Pink Eclipse Sun
    drawCircle(Color(0xFFF7B5CD), radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.30f))
}

private fun DrawScope.drawGalatasarayBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(Color(0xFFA32638), radius = w * 0.45f, center = center) // Red
    drawCircle(Color(0xFFFFA300), radius = w * 0.38f, center = center) // Yellow

    // Intertwined "G" & "S"
    drawCircle(Color(0xFFA32638), radius = w * 0.20f, center = Offset(w * 0.45f, h * 0.45f), style = Stroke(width = 6f))
    drawCircle(Color(0xFFFFA300), radius = w * 0.16f, center = Offset(w * 0.55f, h * 0.55f), style = Stroke(width = 6f))

    // 4 Stars
    for (i in 0..3) {
        drawCircle(Color(0xFFFFD700), radius = w * 0.025f, center = Offset(w * (0.32f + i * 0.12f), h * 0.12f))
    }
}

private fun DrawScope.drawFenerbahceBadge(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(Color(0xFF002D72), radius = w * 0.45f, center = center) // Navy
    drawCircle(Color(0xFFFFEB3B), radius = w * 0.40f, center = center) // Yellow
    drawCircle(Color.White, radius = w * 0.28f, center = center)

    // Green Oak Leaf
    val leaf = Path().apply {
        moveTo(w * 0.50f, h * 0.32f)
        lineTo(w * 0.60f, h * 0.45f)
        lineTo(w * 0.52f, h * 0.65f)
        lineTo(w * 0.48f, h * 0.65f)
        lineTo(w * 0.40f, h * 0.45f)
        close()
    }
    drawPath(leaf, color = Color(0xFF2E7D32))
}

private fun DrawScope.drawBesiktasBadge(w: Float, h: Float) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = Color.White)
    drawPath(shield, color = Color.Black, style = Stroke(width = 4f))

    // Vertical black and white stripes
    drawRect(Color.Black, topLeft = Offset(w * 0.30f, h * 0.20f), size = Size(w * 0.08f, h * 0.60f))
    drawRect(Color.Black, topLeft = Offset(w * 0.62f, h * 0.20f), size = Size(w * 0.08f, h * 0.60f))

    // Turkish Crescent & Star in red square in center
    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.42f, h * 0.38f), size = Size(w * 0.16f, h * 0.24f))
    drawCircle(Color.White, radius = w * 0.04f, center = Offset(w * 0.50f, h * 0.50f))
}

private fun DrawScope.drawGenericClubBadge(w: Float, h: Float, pColor: Color, sColor: Color) {
    val shield = Path().apply {
        moveTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.15f)
        lineTo(w * 0.82f, h * 0.65f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.50f, h * 0.98f, w * 0.50f, h * 0.98f)
        cubicTo(w * 0.50f, h * 0.98f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.65f)
        close()
    }
    drawPath(shield, color = pColor)
    drawPath(shield, color = sColor, style = Stroke(width = 4f))

    // Star and Soccer Ball in center
    drawCircle(sColor, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f))
    drawCircle(pColor, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.5f))
}
