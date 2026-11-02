package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SquadPlayer
import com.example.data.model.TournamentSimulationChoice
import com.example.data.model.TournamentSimulationReport
import com.example.ui.theme.*

data class FormationSlot(
    val id: String,
    val titleAr: String,
    val shortPos: String, // GK, DEF, MID, FWD
    val order: Int
)

val FORMATION_11_SLOTS = listOf(
    FormationSlot("slot_gk", "حارس مرمى", "GK", 1),
    FormationSlot("slot_lb", "ظهير أيسر", "DEF", 2),
    FormationSlot("slot_cb1", "قلب دفاع 1", "DEF", 3),
    FormationSlot("slot_cb2", "قلب دفاع 2", "DEF", 4),
    FormationSlot("slot_rb", "ظهير أيمن", "DEF", 5),
    FormationSlot("slot_cm1", "وسط ميدان 1", "MID", 6),
    FormationSlot("slot_cm2", "وسط ميدان 2", "MID", 7),
    FormationSlot("slot_cam", "صانع ألعاب", "MID", 8),
    FormationSlot("slot_lw", "جناح أيسر", "FWD", 9),
    FormationSlot("slot_st", "رأس حربة", "FWD", 10),
    FormationSlot("slot_rw", "جناح أيمن", "FWD", 11)
)

val AVAILABLE_10_LEAGUES = listOf(
    "الكل",
    "الدوري الإنجليزي",
    "الدوري الإسباني",
    "الدوري الإيطالي",
    "الدوري الألماني",
    "الدوري الفرنسي",
    "الدوري السعودي",
    "الدوري المصري",
    "الدوري الأمريكي",
    "الدوري التركي",
    "الدوري البرتغالي"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadBuilderScreen(
    playersPool: List<SquadPlayer>,
    tournamentChoices: List<TournamentSimulationChoice>,
    onSimulate: (TournamentSimulationChoice, List<SquadPlayer>) -> TournamentSimulationReport,
    onBack: () -> Unit,
    fontScale: Float = 1.0f
) {
    BackHandler { onBack() }

    val selectedSquad = remember { mutableStateMapOf<String, SquadPlayer>() }

    var activePickingSlot by remember { mutableStateOf<FormationSlot?>(null) }
    var viewingPlayerStats by remember { mutableStateOf<SquadPlayer?>(null) } // Detailed Player Stats Modal
    var selectedLeagueFilter by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }

    var showTournamentPicker by remember { mutableStateOf(false) }
    var activeSimulationReport by remember { mutableStateOf<TournamentSimulationReport?>(null) }

    val totalBudget = 200.0
    val spentBudget = selectedSquad.values.sumOf { it.priceMillions }
    val remainingBudget = (totalBudget - spentBudget).coerceAtLeast(0.0)
    val isBudgetExceeded = spentBudget > totalBudget
    val isSquadComplete = selectedSquad.size == 11 && !isBudgetExceeded

    // Detailed Player Stats Bottom Sheet / Dialog
    if (viewingPlayerStats != null) {
        val player = viewingPlayerStats!!
        ModalBottomSheet(
            onDismissRequest = { viewingPlayerStats = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkAppBarColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.2f))
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚽", fontSize = 34.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = player.name,
                    color = TextPrimary,
                    fontSize = (20 * fontScale).sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "${player.club} • ${player.league} (${player.nationality})",
                    color = GoldPrimary,
                    fontSize = (13 * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Player Stats Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 بطاقة الإحصائيات الفنية والمالية:",
                            color = GoldPrimary,
                            fontSize = (14 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            PlayerStatItem("المركز", player.position, NeonGreen, fontScale)
                            PlayerStatItem("التقييم الإجمالي", "${player.rating}", GoldPrimary, fontScale)
                            PlayerStatItem("القيمة السوقية", "${player.priceMillions}M €", Color(0xFF64B5F6), fontScale)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DarkCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            PlayerStatItem("الجنسية", player.nationality, TextPrimary, fontScale)
                            PlayerStatItem("الدوري", player.league, TextPrimary, fontScale)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewingPlayerStats = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إغلاق الإحصائيات", color = Color.Black, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (activeSimulationReport != null) {
        val report = activeSimulationReport!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkAppBackground)
                .testTag("simulation_report_screen")
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "تقرير محاكاة الذكاء الاصطناعي 🤖",
                        color = TextPrimary,
                        fontSize = (17 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activeSimulationReport = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (report.isChampion) GoldPrimary.copy(alpha = 0.2f) else Color(0xFF2C2418)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (report.isChampion) "🏆" else "🥈", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = report.tournamentName,
                    color = GoldPrimary,
                    fontSize = (20 * fontScale).sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = report.finalPlacement,
                    color = if (report.isChampion) NeonGreen else TextSecondary,
                    fontSize = (16 * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatBadge("الهجوم", "${report.teamAttackRating}", GoldPrimary, fontScale)
                        StatBadge("الوسط", "${report.teamMidfieldRating}", NeonGreen, fontScale)
                        StatBadge("الدفاع", "${report.teamDefenseRating}", Color(0xFF64B5F6), fontScale)
                        StatBadge("التناغم", "${report.teamOverallChemistry}%", Color(0xFFBA68C8), fontScale)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, GoldPrimary.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("التقرير الفني الذكي:", color = GoldPrimary, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = report.tacticalAiVerdict,
                            color = TextPrimary,
                            fontSize = (13 * fontScale).sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("تمت إضافة +${report.xpEarned} نقطة لرصيدك العام 🌟", color = NeonGreen, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showTournamentPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF1E1E2C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("محاكاة في بطولة أخرى 🏆", color = Color(0xFF1E1E2C), fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { activeSimulationReport = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("العودة لتعديل التشكيلة", color = GoldPrimary, fontSize = (13.5 * fontScale).sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkAppBackground)
                .testTag("squad_builder_screen")
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "التحدي الرابع: كوّن فريق (11 لاعباً) ⚽",
                            color = TextPrimary,
                            fontSize = (16 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ميزانية 200 مليون • عرض إحصائيات اللاعبين",
                            color = TextSecondary,
                            fontSize = (11 * fontScale).sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldPrimary)
                    }
                },
                actions = {
                    if (selectedSquad.isNotEmpty()) {
                        IconButton(onClick = { selectedSquad.clear() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "إفراغ التشكيلة", tint = Color(0xFFFF5252))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkAppBarColor)
            )

            var squadPlayMode by remember { mutableStateOf(com.example.data.model.PlayMode.SOLO) }

            // Mode Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { squadPlayMode = com.example.data.model.PlayMode.SOLO },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (squadPlayMode == com.example.data.model.PlayMode.SOLO) GoldPrimary else Color(0xFF2B2B3D)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تشكيلة فردية 👤", color = if (squadPlayMode == com.example.data.model.PlayMode.SOLO) Color(0xFF1E1E2C) else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { squadPlayMode = com.example.data.model.PlayMode.ONLINE },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (squadPlayMode == com.example.data.model.PlayMode.ONLINE) NeonGreen else Color(0xFF2B2B3D)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("مواجهة تشكيلة أونلاين 🌐", color = if (squadPlayMode == com.example.data.model.PlayMode.ONLINE) Color.Black else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (squadPlayMode == com.example.data.model.PlayMode.ONLINE) {
                com.example.ui.components.OnlineMatchTopHeader(
                    userScore = (selectedSquad.values.sumOf { it.rating } * 0.9).toInt(),
                    opponentName = "تشكيلة الكابتن صلاح ⚔️",
                    opponentTagId = "ID-48201",
                    opponentAvatar = "⚡",
                    opponentScore = 910,
                    fontScale = fontScale
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isBudgetExceeded) Color(0xFFE53935) else DarkCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("اللاعبين المختارين:", color = TextMuted, fontSize = (11.5 * fontScale).sp)
                        Text(
                            text = "${selectedSquad.size} / 11 لاعب",
                            color = if (selectedSquad.size == 11) NeonGreen else GoldPrimary,
                            fontSize = (16 * fontScale).sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("الميزانية المتبقية:", color = TextMuted, fontSize = (11.5 * fontScale).sp)
                        Text(
                            text = String.format("%.1fM / 200.0M €", remainingBudget),
                            color = if (isBudgetExceeded) Color(0xFFE53935) else NeonGreen,
                            fontSize = (16 * fontScale).sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // The 11 Formation Slots List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FORMATION_11_SLOTS) { slot ->
                    val assignedPlayer = selectedSquad[slot.id]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activePickingSlot = slot }
                            .testTag("slot_${slot.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (assignedPlayer != null) DarkCardSurface else Color(0xFF1A1A26)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (assignedPlayer != null) GoldPrimary.copy(alpha = 0.7f) else DarkCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (slot.shortPos) {
                                        "GK" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                        "DEF" -> Color(0xFF29B6F6).copy(alpha = 0.2f)
                                        "MID" -> NeonGreen.copy(alpha = 0.2f)
                                        else -> Color(0xFFE53935).copy(alpha = 0.2f)
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = slot.shortPos,
                                            color = when (slot.shortPos) {
                                                "GK" -> Color(0xFFFF9800)
                                                "DEF" -> Color(0xFF29B6F6)
                                                "MID" -> NeonGreen
                                                else -> Color(0xFFE53935)
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = slot.titleAr,
                                        color = TextMuted,
                                        fontSize = (11 * fontScale).sp
                                    )
                                    if (assignedPlayer != null) {
                                        Text(
                                            text = assignedPlayer.name,
                                            color = TextPrimary,
                                            fontSize = (14.5 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${assignedPlayer.club} • تقييم ${assignedPlayer.rating}",
                                            color = NeonGreen,
                                            fontSize = (11 * fontScale).sp
                                        )
                                    } else {
                                        Text(
                                            text = "انقر لإضافة لاعب أو سحب وإفلات",
                                            color = GoldPrimary,
                                            fontSize = (13 * fontScale).sp
                                        )
                                    }
                                }
                            }

                            if (assignedPlayer != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Info button to view player stats
                                    IconButton(
                                        onClick = { viewingPlayerStats = assignedPlayer },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "إحصائيات اللاعب", tint = GoldPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${assignedPlayer.priceMillions}M €",
                                            color = GoldPrimary,
                                            fontSize = (13.5 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { selectedSquad.remove(slot.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color(0xFFFF5252))
                                    }
                                }
                            } else {
                                Icon(Icons.Default.Add, contentDescription = "إضافة", tint = GoldPrimary)
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkAppBarColor,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showTournamentPicker = true },
                        enabled = isSquadComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_start_ai_simulation"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedSquad.size < 11) "اكتمل ${selectedSquad.size}/11 لاعباً لبدء المحاكاة"
                                else if (isBudgetExceeded) "تجاوزت الميزانية! قلل تكلفة التشكيلة"
                                else "محاكاة الفريق بالذكاء الاصطناعي 🤖🏆",
                                color = Color.Black,
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Player Picker Modal Bottom Sheet with Detailed Stats View
    if (activePickingSlot != null) {
        val slot = activePickingSlot!!
        ModalBottomSheet(
            onDismissRequest = { activePickingSlot = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkAppBarColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "اختيار ${slot.titleAr} (${slot.shortPos}) مع عرض الإحصائيات",
                    color = TextPrimary,
                    fontSize = (17 * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث باسم اللاعب أو النادي...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E2C),
                        unfocusedContainerColor = Color(0xFF1E1E2C),
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AVAILABLE_10_LEAGUES) { league ->
                        FilterChip(
                            selected = selectedLeagueFilter == league,
                            onClick = { selectedLeagueFilter = league },
                            label = { Text(league, fontSize = (11 * fontScale).sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E1E2C),
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val filteredPool = playersPool.filter { player ->
                    val posMatches = player.position == slot.shortPos
                    val leagueMatches = selectedLeagueFilter == "الكل" || player.league == selectedLeagueFilter
                    val searchMatches = searchQuery.isBlank() || player.name.contains(searchQuery, ignoreCase = true) || player.club.contains(searchQuery, ignoreCase = true)
                    posMatches && leagueMatches && searchMatches
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredPool) { player ->
                        val isAlreadyChosen = selectedSquad.values.any { it.id == player.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAlreadyChosen) {
                                    selectedSquad[slot.id] = player
                                    activePickingSlot = null
                                    searchQuery = ""
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAlreadyChosen) Color(0xFF151520) else DarkCardSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isAlreadyChosen) DarkCardBorder else GoldPrimary.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = player.name,
                                        color = if (isAlreadyChosen) TextMuted else TextPrimary,
                                        fontSize = (14 * fontScale).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${player.club} • ${player.league}",
                                        color = TextSecondary,
                                        fontSize = (11 * fontScale).sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewingPlayerStats = player },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "إحصائيات", tint = GoldPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${player.priceMillions}M €",
                                            color = GoldPrimary,
                                            fontSize = (13.5 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "تقييم ${player.rating}",
                                            color = NeonGreen,
                                            fontSize = (11 * fontScale).sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isAlreadyChosen) {
                                        Text("مختار", color = TextMuted, fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.Add, contentDescription = "اختيار", tint = GoldPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTournamentPicker) {
        ModalBottomSheet(
            onDismissRequest = { showTournamentPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkAppBarColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "اختر البطولة لمحاكاة فريقك بالذكاء الاصطناعي 🏆",
                    color = TextPrimary,
                    fontSize = (16.5 * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tournamentChoices) { tournament ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showTournamentPicker = false
                                    activeSimulationReport = onSimulate(tournament, selectedSquad.values.toList())
                                }
                                .testTag("tournament_${tournament.id}"),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tournament.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tournament.nameAr,
                                            color = TextPrimary,
                                            fontSize = (14.5 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = tournament.description,
                                            color = TextSecondary,
                                            fontSize = (11 * fontScale).sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Icon(Icons.Default.PlayArrow, contentDescription = "بدء المحاكاة", tint = NeonGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerStatItem(label: String, value: String, color: Color, fontScale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = (11.5 * fontScale).sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = color, fontSize = (15 * fontScale).sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatBadge(title: String, value: String, color: Color, fontScale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextMuted, fontSize = (11 * fontScale).sp)
        Text(value, color = color, fontSize = (18 * fontScale).sp, fontWeight = FontWeight.Black)
    }
}
