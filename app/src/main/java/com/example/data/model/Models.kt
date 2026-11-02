package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ChallengeIconType {
    PERSON,
    SOCCER,
    TROPHY,
    GROUPS
}

enum class ChallengeDifficulty(val labelAr: String) {
    EASY("سهل"),
    MEDIUM("متوسط"),
    HARD("صعب"),
    LEGEND("أساطير")
}

enum class ChallengeCategory(val labelAr: String) {
    ALL("الكل"),
    TRIVIA("معلومات كروية"),
    TACTICS("تكتيك وتشكيل"),
    WORLD_CUP("كأس العالم")
}

data class ChallengeItem(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val description: String,
    val iconType: ChallengeIconType,
    val difficulty: ChallengeDifficulty,
    val category: ChallengeCategory,
    val isFavorite: Boolean = false,
    val rewardXp: Int = 100,
    val activePlayers: Int = 1240,
    val highlightTag: String? = null,
    val allowedQuestionCounts: List<Int> = listOf(10, 25, 50, 100),
    val coinCostPerRound: Int = 25,
    val pointsPerCorrectAnswer: Int = 10
)

data class FootballRankTier(
    val tierId: String,
    val titleAr: String,
    val badgeIconEmoji: String,
    val minPoints: Int,
    val maxPoints: Int,
    val badgeColorHex: Long = 0xFFFFD700,
    val perkDescriptionAr: String
)

val FOOTBALL_TIERS = listOf(
    FootballRankTier(
        tierId = "tier_rookie",
        titleAr = "لاعب مبتدئ",
        badgeIconEmoji = "🌱",
        minPoints = 0,
        maxPoints = 999,
        badgeColorHex = 0xFF81C784,
        perkDescriptionAr = "فتح التحديات الكروية الأساسية وتجربة المسابقات"
    ),
    FootballRankTier(
        tierId = "tier_talent",
        titleAr = "لاعب واعد",
        badgeIconEmoji = "⚡",
        minPoints = 1000,
        maxPoints = 2499,
        badgeColorHex = 0xFF64B5F6,
        perkDescriptionAr = "مكافأة +5% نقاط إضافية وتكتيكات متقدمة"
    ),
    FootballRankTier(
        tierId = "tier_star",
        titleAr = "نجم الفريق",
        badgeIconEmoji = "🌟",
        minPoints = 2500,
        maxPoints = 4499,
        badgeColorHex = 0xFFFFD54F,
        perkDescriptionAr = "شارات تميز ومسابقات كروية حصرية"
    ),
    FootballRankTier(
        tierId = "tier_captain",
        titleAr = "قائد التشكيلة",
        badgeIconEmoji = "🛡️",
        minPoints = 4500,
        maxPoints = 7499,
        badgeColorHex = 0xFFFF7043,
        perkDescriptionAr = "شارة القيادة وفتح محاكاة متطورة"
    ),
    FootballRankTier(
        tierId = "tier_legend",
        titleAr = "أسطورة الملاعب",
        badgeIconEmoji = "👑",
        minPoints = 7500,
        maxPoints = 11999,
        badgeColorHex = 0xFFBA68C8,
        perkDescriptionAr = "شارة التاج الذهبي وصدارة الأساطير"
    ),
    FootballRankTier(
        tierId = "tier_ballon_dor",
        titleAr = "الكرة الذهبية",
        badgeIconEmoji = "🏆",
        minPoints = 12000,
        maxPoints = Int.MAX_VALUE,
        badgeColorHex = 0xFFFFD700,
        perkDescriptionAr = "المرتبة الفخرية العليا للمتصدرين الدائمين"
    )
)

data class UserLevelProgress(
    val level: Int,
    val currentTier: FootballRankTier,
    val nextTier: FootballRankTier?,
    val currentPoints: Int,
    val levelStartPoints: Int,
    val levelNextPoints: Int,
    val levelProgressRatio: Float,
    val pointsToNextLevel: Int,
    val tierProgressRatio: Float,
    val pointsToNextTier: Int
)

fun calculateUserLevelProgress(points: Int): UserLevelProgress {
    val pointsPerLevel = 250
    val calculatedLevel = 1 + (points / pointsPerLevel)
    val levelStart = (calculatedLevel - 1) * pointsPerLevel
    val levelNext = calculatedLevel * pointsPerLevel
    val ptsInLevel = (points - levelStart).coerceAtLeast(0)
    val levelRatio = (ptsInLevel.toFloat() / pointsPerLevel.toFloat()).coerceIn(0f, 1f)
    val ptsToNextLvl = (levelNext - points).coerceAtLeast(0)

    val currentTierIndex = FOOTBALL_TIERS.indexOfLast { points >= it.minPoints }.coerceAtLeast(0)
    val currentTier = FOOTBALL_TIERS[currentTierIndex]
    val nextTier = FOOTBALL_TIERS.getOrNull(currentTierIndex + 1)

    val tierRange = if (currentTier.maxPoints == Int.MAX_VALUE) 5000 else (currentTier.maxPoints - currentTier.minPoints + 1)
    val ptsInTier = (points - currentTier.minPoints).coerceAtLeast(0)
    val tierRatio = (ptsInTier.toFloat() / tierRange.toFloat()).coerceIn(0f, 1f)
    val ptsToNextTier = if (nextTier != null) (nextTier.minPoints - points).coerceAtLeast(0) else 0

    return UserLevelProgress(
        level = calculatedLevel,
        currentTier = currentTier,
        nextTier = nextTier,
        currentPoints = points,
        levelStartPoints = levelStart,
        levelNextPoints = levelNext,
        levelProgressRatio = levelRatio,
        pointsToNextLevel = ptsToNextLvl,
        tierProgressRatio = tierRatio,
        pointsToNextTier = ptsToNextTier
    )
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userTagId: String = "ID-89421",
    val name: String = "كابتن جديد",
    val email: String = "player@madira.com",
    val avatarEmoji: String = "⚽",
    val points: Int = 0,
    val rank: Int = 1,
    val winStreak: Int = 0,
    val favoriteClub: String = "الأهلي المصري",
    val virtualCoins: Double = 100.0,
    val unlockedChallengeIds: List<String> = listOf("guess_player"),
    val subscribedPackageId: String = "none",
    val favoriteChallenges: List<String> = listOf("guess_player", "guess_club", "squad_builder"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val levelProgress: UserLevelProgress
        get() = calculateUserLevelProgress(points)

    val formattedTagId: String
        get() = if (userTagId.startsWith("ID-")) "#$userTagId" else "#ID-$userTagId"
}

@IgnoreExtraProperties
data class LeaderboardEntry(
    val rank: Int = 1,
    val name: String = "",
    val points: Int = 0,
    val avatarEmoji: String = "⚽",
    val favoriteClub: String = "الأهلي المصري",
    val isCurrentUser: Boolean = false,
    val weeklyWinRate: String = "85%",
    val winStreak: Int = 0,
    val userId: String = "",
    @DocumentId val id: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class PlayerGuessQuestion(
    val id: Int,
    val difficulty: ChallengeDifficulty,
    val hintNationality: String,
    val hintClubs: List<String>,
    val hintNumber: Int,
    val hintPosition: String,
    val specialClue: String,
    val correctAnswer: String,
    val options: List<String>
)

data class ClubGuessQuestion(
    val id: Int,
    val difficulty: ChallengeDifficulty,
    val clubName: String,
    val country: String,
    val league: String,
    val stadium: String,
    val titlesCount: String,
    val famousLegend: String,
    val nickname: String,
    val badgeSymbol: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val correctAnswer: String,
    val options: List<String>
)

data class WorldCupQuestion(
    val id: Int,
    val difficulty: ChallengeDifficulty,
    val edition: String,
    val question: String,
    val hint: String,
    val correctAnswer: String,
    val options: List<String>,
    val triviaFact: String
)

data class SquadPlayer(
    val id: String,
    val name: String,
    val position: String, // GK, DEF, MID, FWD
    val rating: Int,
    val priceMillions: Double,
    val nationality: String,
    val club: String,
    val league: String // One of the 10 leagues
)

data class TournamentSimulationChoice(
    val id: String,
    val nameAr: String,
    val iconEmoji: String,
    val category: String, // "قاري" or "دوري محلي"
    val description: String,
    val difficultyStars: Int
)

data class MatchSimulationEvent(
    val minute: Int,
    val title: String,
    val description: String,
    val isGoal: Boolean = false,
    val isUserTeam: Boolean = false
)

data class MatchSimulationStep(
    val stageName: String,
    val opponentName: String,
    val opponentLogoSymbol: String,
    val userGoals: Int,
    val opponentGoals: Int,
    val userScorers: List<String>,
    val opponentScorers: List<String>,
    val events: List<MatchSimulationEvent>,
    val isWin: Boolean
)

data class TournamentSimulationReport(
    val tournamentName: String,
    val isChampion: Boolean,
    val finalPlacement: String,
    val matches: List<MatchSimulationStep>,
    val teamAttackRating: Int,
    val teamMidfieldRating: Int,
    val teamDefenseRating: Int,
    val teamOverallChemistry: Int,
    val tacticalAiVerdict: String,
    val xpEarned: Int
)

data class SoundSettings(
    val soundEffectsEnabled: Boolean = true,
    val stadiumAmbienceEnabled: Boolean = true,
    val volume: Float = 0.8f,
    val hapticFeedbackEnabled: Boolean = true
)

data class AppNotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "تحديث الإصدار", "تحدي كروي", "مكافأة", "إشعار مخصص"
    val timestamp: String,
    val isRead: Boolean = false,
    val versionTag: String? = null,
    val iconEmoji: String = "📢",
    val isImportant: Boolean = false
)

data class FriendUser(
    val id: String,
    val userTagId: String,
    val name: String,
    val avatarEmoji: String,
    val favoriteClub: String,
    val points: Int,
    val isOnline: Boolean = true,
    val statusText: String = "متصل أونلاين 🟢"
)

data class FriendRequestItem(
    val requestId: String,
    val senderTagId: String,
    val senderName: String,
    val senderAvatar: String,
    val timestamp: String = "الآن"
)

enum class PlayMode {
    SOLO,
    ONLINE
}

data class OnlineOpponentState(
    val name: String = "كابتن أونلاين ⚔️",
    val userTagId: String = "ID-99281",
    val avatarEmoji: String = "🔥",
    val score: Int = 0,
    val isConnected: Boolean = true,
    val status: String = "يلعب الآن ⚡"
)

data class TransactionRecord(
    val id: String = "",
    val userId: String = "",
    val userTagId: String = "",
    val amount: String = "",
    val method: String = "",
    val status: String = "معلق", // "معلق", "مقبول", "مرفوض"
    val timestamp: Long = System.currentTimeMillis()
)

