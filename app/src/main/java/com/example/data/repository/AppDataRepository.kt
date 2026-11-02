package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class AppDataRepository(
    val firestore: FirebaseFirestore? = null
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    val db: FirebaseFirestore
        get() = firestore ?: throw IllegalStateException("Firestore is not configured")

    private val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (e: Exception) {
            null
        }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun requireUserId(): String {
        return auth?.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // Current logged-in user profile
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "usr_initial",
            userId = "usr_initial",
            name = "كابتن جديد",
            email = "player@madira.com",
            avatarEmoji = "⚽",
            points = 500,
            rank = 4,
            winStreak = 3,
            favoriteClub = "الأهلي المصري",
            virtualCoins = 100.0,
            unlockedChallengeIds = listOf("guess_player"),
            favoriteChallenges = listOf("guess_player")
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // 4 Strict Challenges in exact order with exact coin costs and point values
    private val _challenges = MutableStateFlow(
        listOf(
            ChallengeItem(
                id = "guess_player",
                orderIndex = 1,
                title = "خمن اسم اللاعب",
                description = "اختبر معرفتك بأساطير ونجوم كرة القدم من خلال الجنسية، الأندية السابقة، المركز، والأرقام التاريخية.",
                iconType = ChallengeIconType.PERSON,
                difficulty = ChallengeDifficulty.EASY,
                category = ChallengeCategory.TRIVIA,
                isFavorite = true,
                rewardXp = 150,
                activePlayers = 5820,
                highlightTag = "متاح مجاناً 🔥",
                allowedQuestionCounts = listOf(10, 25, 50, 100),
                coinCostPerRound = 25,
                pointsPerCorrectAnswer = 10
            ),
            ChallengeItem(
                id = "guess_club",
                orderIndex = 2,
                title = "خمن شعار او اسم النادي",
                description = "شاهد شعارات الأندية بدقة متناهية وتعرف على كبار الأندية العربية والعالمية، الملاعب، والألقاب الشهيرة.",
                iconType = ChallengeIconType.SOCCER,
                difficulty = ChallengeDifficulty.MEDIUM,
                category = ChallengeCategory.TRIVIA,
                isFavorite = true,
                rewardXp = 200,
                activePlayers = 4910,
                highlightTag = "التحدي الثاني 🛡️",
                allowedQuestionCounts = listOf(10, 25, 50, 100),
                coinCostPerRound = 40,
                pointsPerCorrectAnswer = 15
            ),
            ChallengeItem(
                id = "world_cup",
                orderIndex = 3,
                title = "كأس العالم",
                description = "موسوعة المونديال الشاملة لجميع بطولات كأس العالم للمنتخبات من 1930 حتى 2022، الهدافين والنهائيات الخالدة.",
                iconType = ChallengeIconType.TROPHY,
                difficulty = ChallengeDifficulty.HARD,
                category = ChallengeCategory.WORLD_CUP,
                isFavorite = false,
                rewardXp = 350,
                activePlayers = 6430,
                highlightTag = "التحدي الثالث 🏆",
                allowedQuestionCounts = listOf(10, 25, 50),
                coinCostPerRound = 50,
                pointsPerCorrectAnswer = 25
            ),
            ChallengeItem(
                id = "squad_builder",
                orderIndex = 4,
                title = "كوّن فريق",
                description = "اختر 11 لاعباً بميزانية 200 مليون من أفضل 10 دوريات عالمية وعربية درجة أولى، ثم حاكِ بطولتك بالذكاء الاصطناعي!",
                iconType = ChallengeIconType.GROUPS,
                difficulty = ChallengeDifficulty.LEGEND,
                category = ChallengeCategory.TACTICS,
                isFavorite = true,
                rewardXp = 500,
                activePlayers = 8920,
                highlightTag = "التحدي الرابع 🤖",
                allowedQuestionCounts = emptyList(),
                coinCostPerRound = 100,
                pointsPerCorrectAnswer = 50
            )
        )
    )
    val challenges: StateFlow<List<ChallengeItem>> = _challenges.asStateFlow()

    // Leaderboard entries
    private val _leaderboard = MutableStateFlow(
        listOf(
            LeaderboardEntry(1, "محمود كهرباوي", 5840, "👑", "الأهلي المصري", weeklyWinRate = "94%"),
            LeaderboardEntry(2, "سلطان التكتيك", 4920, "⭐", "الهلال السعودي", weeklyWinRate = "91%"),
            LeaderboardEntry(3, "عمر المدريدي", 3780, "🔥", "ريال مدريد", weeklyWinRate = "88%"),
            LeaderboardEntry(4, "كابتن زياد (أنت)", 2850, "⚽", "الأهلي المصري", isCurrentUser = true, weeklyWinRate = "85%"),
            LeaderboardEntry(5, "ياسين الكتلوني", 2430, "🎯", "برشلونة", weeklyWinRate = "82%"),
            LeaderboardEntry(6, "أنس الزملكاوي", 2150, "🏹", "الزمالك", weeklyWinRate = "79%"),
            LeaderboardEntry(7, "فهد الأندلسي", 1890, "⚡", "النصر السعودي", weeklyWinRate = "76%"),
            LeaderboardEntry(8, "كريم الريدز", 1620, "🔴", "ليفربول", weeklyWinRate = "72%")
        )
    )
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Sound settings (Effects, Stadium ambience, volume, haptic feedback)
    private val _soundSettings = MutableStateFlow(SoundSettings())
    val soundSettings: StateFlow<SoundSettings> = _soundSettings.asStateFlow()

    // App Notifications & Updates feed
    private val _notifications = MutableStateFlow(
        listOf(
            AppNotificationItem(
                id = "notif_v13_release",
                title = "إطلاق إصدار Madira Games v1.3 🚀",
                description = "مرحباً بكم في الإصدار 1.3! إضافة نظام الأصدقاء، تحديات أونلاين مباشرة، تاريخ معاملات الشحن، ودعم فني مباشر عبر واتساب.",
                category = "تحديث رئيسي",
                timestamp = "الآن",
                isRead = false,
                versionTag = "v1.3",
                iconEmoji = "🔥",
                isImportant = true
            ),
            AppNotificationItem(
                id = "notif_friends_system",
                title = "نظام الأصدقاء والأونلاين 🌐",
                description = "يمكنك الآن إضافة أصدقائك عبر رقم الـ ID والمنافسة في وضع الأونلاين المباشر في جميع التحديات.",
                category = "ميزة جديدة",
                timestamp = "اليوم",
                isRead = false,
                versionTag = "v1.3",
                iconEmoji = "👥"
            ),
            AppNotificationItem(
                id = "notif_billing_history",
                title = "تاريخ المعاملات والدعم الفني 📜",
                description = "تتبع حالة شحن رصيدك بكل شفافية وتواصل مع الدعم الفني مباشرة عبر واتساب من شاشة الملف الشخصي.",
                category = "تحسينات",
                timestamp = "اليوم",
                isRead = false,
                versionTag = "v1.3",
                iconEmoji = "🛠️"
            )
        )
    )
    val notifications: StateFlow<List<AppNotificationItem>> = _notifications.asStateFlow()

    // Friends & Friend Requests State Flow
    private val _friends = MutableStateFlow(
        listOf<FriendUser>()
    )
    val friends: StateFlow<List<FriendUser>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow(
        listOf<FriendRequestItem>()
    )
    val friendRequests: StateFlow<List<FriendRequestItem>> = _friendRequests.asStateFlow()

    fun sendFriendRequest(targetTagId: String): String {
        val cleanTag = targetTagId.trim().replace("#", "").uppercase()
        if (cleanTag.isBlank()) return "يرجى إدخال الرقم التعريفي للمستخدم (مثال: ID-89421)"
        val existing = _friends.value.find { it.userTagId.equals(cleanTag, ignoreCase = true) }
        if (existing != null) return "هذا المستخدم موجود في قائمة أصدقائك بالفعل!"
        
        return "تم إرسال طلب الصداقة بنجاح إلى الرقم التعريفي #$cleanTag! 🚀"
    }

    fun acceptFriendRequest(requestId: String) {
        val req = _friendRequests.value.find { it.requestId == requestId }
        if (req != null) {
            val newFriend = FriendUser(
                id = "usr_${req.senderTagId}",
                userTagId = req.senderTagId,
                name = req.senderName,
                avatarEmoji = req.senderAvatar,
                favoriteClub = "الأهلي المصري",
                points = 1500,
                isOnline = true,
                statusText = "أصبح صديقك الآن 🤝"
            )
            _friends.update { it + newFriend }
            _friendRequests.update { list -> list.filter { it.requestId != requestId } }
        }
    }

    fun rejectFriendRequest(requestId: String) {
        _friendRequests.update { list -> list.filter { it.requestId != requestId } }
    }

    // Transaction History
    private val _transactions = MutableStateFlow(
        listOf<TransactionRecord>()
    )
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    fun recordTransaction(tx: TransactionRecord) {
        _transactions.update { it + tx }
    }


    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else {
                        val data = snapshot.data ?: return@map null
                        @Suppress("UNCHECKED_CAST")
                        val favs = (data["favoriteChallenges"] as? List<String>) ?: emptyList()
                        UserProfile(
                            id = snapshot.id,
                            userId = (data["userId"] as? String) ?: snapshot.id,
                            name = (data["name"] as? String) ?: "",
                            email = (data["email"] as? String) ?: "",
                            avatarEmoji = (data["avatarEmoji"] as? String) ?: "⚽",
                            points = ((data["points"] as? Number)?.toInt()) ?: 0,
                            winStreak = ((data["winStreak"] as? Number)?.toInt()) ?: 0,
                            favoriteClub = (data["favoriteClub"] as? String) ?: "الأهلي المصري",
                            virtualCoins = ((data["virtualCoins"] as? Number)?.toDouble()) ?: 200.0,
                            favoriteChallenges = favs
                        )
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, "users/$userId")
                    throw error
                }
        )
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        emitAll(
            db.collection("leaderboard")
                .snapshots()
                .map { snapshot ->
                    val currentUid = auth?.currentUser?.uid
                    snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        LeaderboardEntry(
                            id = doc.id,
                            userId = (data["userId"] as? String) ?: doc.id,
                            name = (data["name"] as? String) ?: "",
                            avatarEmoji = (data["avatarEmoji"] as? String) ?: "⚽",
                            favoriteClub = (data["favoriteClub"] as? String) ?: "الأهلي المصري",
                            points = ((data["points"] as? Number)?.toInt()) ?: 0,
                            winStreak = ((data["winStreak"] as? Number)?.toInt()) ?: 0,
                            weeklyWinRate = (data["weeklyWinRate"] as? String) ?: "85%",
                            isCurrentUser = ((data["userId"] as? String) ?: doc.id) == currentUid
                        )
                    }.sortedByDescending { it.points }
                    .mapIndexed { idx, item -> item.copy(rank = idx + 1) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, "leaderboard")
                    throw error
                }
        )
    }

    suspend fun createUserProfile(profile: UserProfile): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        require(profile.userId == uid) { "User ID mismatch" }
        val userDocRef = db.collection("users").document(uid)
        val lbDocRef = db.collection("leaderboard").document(uid)
        return try {
            val existing = userDocRef.get().await()
            if (existing.exists()) {
                val updatePayload = mapOf(
                    "name" to profile.name,
                    "email" to profile.email,
                    "avatarEmoji" to profile.avatarEmoji,
                    "favoriteClub" to profile.favoriteClub,
                    "points" to profile.points,
                    "winStreak" to profile.winStreak,
                    "virtualCoins" to profile.virtualCoins,
                    "favoriteChallenges" to profile.favoriteChallenges,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.update(updatePayload).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "name" to profile.name,
                    "email" to profile.email,
                    "avatarEmoji" to profile.avatarEmoji,
                    "favoriteClub" to profile.favoriteClub,
                    "points" to profile.points,
                    "winStreak" to profile.winStreak,
                    "virtualCoins" to profile.virtualCoins,
                    "favoriteChallenges" to profile.favoriteChallenges,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(payload).await()
            }

            val lbPayload = mapOf(
                "userId" to uid,
                "name" to profile.name,
                "avatarEmoji" to profile.avatarEmoji,
                "favoriteClub" to profile.favoriteClub,
                "points" to profile.points,
                "winStreak" to profile.winStreak,
                "weeklyWinRate" to "85%",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            lbDocRef.set(lbPayload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userDocRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                Result.failure(NoSuchElementException("User profile not found"))
            } else {
                val data = snapshot.data ?: throw IllegalStateException("Empty user document")
                @Suppress("UNCHECKED_CAST")
                val favs = (data["favoriteChallenges"] as? List<String>) ?: emptyList()
                val profile = UserProfile(
                    id = snapshot.id,
                    userId = (data["userId"] as? String) ?: snapshot.id,
                    name = (data["name"] as? String) ?: "",
                    email = (data["email"] as? String) ?: "",
                    avatarEmoji = (data["avatarEmoji"] as? String) ?: "⚽",
                    points = ((data["points"] as? Number)?.toInt()) ?: 0,
                    winStreak = ((data["winStreak"] as? Number)?.toInt()) ?: 0,
                    favoriteClub = (data["favoriteClub"] as? String) ?: "الأهلي المصري",
                    virtualCoins = ((data["virtualCoins"] as? Number)?.toDouble()) ?: 200.0,
                    favoriteChallenges = favs
                )
                Result.success(profile)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun syncWithFirestore(userId: String, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                val profileResult = getUserProfile(userId)
                if (profileResult.isSuccess) {
                    val p = profileResult.getOrThrow()
                    _currentUser.value = p
                    _challenges.update { list ->
                        list.map { it.copy(isFavorite = p.favoriteChallenges.contains(it.id)) }
                    }
                } else {
                    // Create initial profile in Firestore
                    val initial = _currentUser.value.copy(userId = userId, id = userId)
                    createUserProfile(initial)
                }
            } catch (e: Exception) {
                // Ignore initial sync failure and rely on local state
            }
        }

        coroutineScope.launch {
            observeUserProfile(userId).collect { profile ->
                if (profile != null) {
                    _currentUser.value = profile
                    _challenges.update { list ->
                        list.map { it.copy(isFavorite = profile.favoriteChallenges.contains(it.id)) }
                    }
                }
            }
        }

        coroutineScope.launch {
            observeLeaderboard().collect { list ->
                if (list.isNotEmpty()) {
                    _leaderboard.value = list
                }
            }
        }
    }

    fun login(email: String, name: String, club: String, avatar: String, isNewUser: Boolean) {
        val uid = auth?.currentUser?.uid ?: "usr_${System.currentTimeMillis()}"
        _currentUser.update {
            it.copy(
                id = uid,
                userId = uid,
                email = email,
                name = name.ifBlank { it.name },
                favoriteClub = club.ifBlank { it.favoriteClub },
                avatarEmoji = avatar.ifBlank { it.avatarEmoji }
            )
        }

        if (auth?.currentUser != null && firestore != null) {
            scope.launch {
                try {
                    val userDoc = db.collection("users").document(uid).get().await()
                    if (!userDoc.exists()) {
                        createUserProfile(_currentUser.value)
                    } else {
                        // Update with latest login info
                        db.collection("users").document(uid).update(
                            mapOf(
                                "name" to _currentUser.value.name,
                                "avatarEmoji" to _currentUser.value.avatarEmoji,
                                "favoriteClub" to _currentUser.value.favoriteClub,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.WRITE, "users/$uid")
                }
            }
        }
    }

    fun addPoints(amount: Int) {
        _currentUser.update { current ->
            val newPoints = current.points + amount
            val updatedStreak = if (amount > 0) current.winStreak + 1 else current.winStreak
            current.copy(points = newPoints, winStreak = updatedStreak)
        }
        _leaderboard.update { list ->
            list.map { entry ->
                if (entry.isCurrentUser) entry.copy(points = _currentUser.value.points) else entry
            }.sortedByDescending { it.points }.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
        }

        val uid = auth?.currentUser?.uid
        if (uid != null && firestore != null) {
            scope.launch {
                try {
                    val p = _currentUser.value.points
                    val s = _currentUser.value.winStreak
                    db.collection("users").document(uid).update(
                        mapOf(
                            "points" to p,
                            "winStreak" to s,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    db.collection("leaderboard").document(uid).update(
                        mapOf(
                            "points" to p,
                            "winStreak" to s,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
                }
            }
        }
    }

    fun deductCoinsForChallenge(challengeId: String): Boolean {
        val challenge = _challenges.value.find { it.id == challengeId } ?: return false
        val currentCoins = _currentUser.value.virtualCoins
        if (currentCoins >= challenge.coinCostPerRound) {
            val newCoins = currentCoins - challenge.coinCostPerRound
            _currentUser.update { it.copy(virtualCoins = newCoins) }
            return true
        }
        return false
    }

    fun exchangePointsForCoins(requiredPoints: Int, grantedCoins: Double): Boolean {
        val currentPoints = _currentUser.value.points
        if (currentPoints >= requiredPoints) {
            val newPoints = currentPoints - requiredPoints
            val newCoins = _currentUser.value.virtualCoins + grantedCoins
            _currentUser.update { it.copy(points = newPoints, virtualCoins = newCoins) }
            return true
        }
        return false
    }

    fun grantPackageSubscription(packageTitle: String) {
        val current = _currentUser.value
        when {
            packageTitle.contains("البرونزية") || packageTitle.contains("30") -> {
                val newCoins = current.virtualCoins + 350
                val newUnlocked = (current.unlockedChallengeIds + "guess_club").distinct()
                _currentUser.update { it.copy(virtualCoins = newCoins, unlockedChallengeIds = newUnlocked, subscribedPackageId = "pkg_bronze") }
            }
            packageTitle.contains("الفضية") || packageTitle.contains("70") -> {
                val newCoins = current.virtualCoins + 1000
                val newUnlocked = (current.unlockedChallengeIds + "guess_club").distinct()
                _currentUser.update { it.copy(virtualCoins = newCoins, unlockedChallengeIds = newUnlocked, subscribedPackageId = "pkg_silver") }
            }
            packageTitle.contains("الذهبية") || packageTitle.contains("220") -> {
                val newCoins = current.virtualCoins + 4000
                val newUnlocked = (current.unlockedChallengeIds + listOf("guess_club", "world_cup", "squad_builder")).distinct()
                _currentUser.update { it.copy(virtualCoins = newCoins, unlockedChallengeIds = newUnlocked, subscribedPackageId = "pkg_gold") }
            }
            packageTitle.contains("الألماسية") || packageTitle.contains("550") -> {
                val newCoins = current.virtualCoins + 12000
                val newUnlocked = listOf("guess_player", "guess_club", "world_cup", "squad_builder")
                _currentUser.update { it.copy(virtualCoins = newCoins, unlockedChallengeIds = newUnlocked, subscribedPackageId = "pkg_diamond") }
            }
            else -> {
                val newCoins = current.virtualCoins + 500
                val newUnlocked = (current.unlockedChallengeIds + "guess_club").distinct()
                _currentUser.update { it.copy(virtualCoins = newCoins, unlockedChallengeIds = newUnlocked) }
            }
        }
    }

    fun toggleFavorite(challengeId: String) {
        _challenges.update { list ->
            list.map { item ->
                if (item.id == challengeId) item.copy(isFavorite = !item.isFavorite) else item
            }
        }
        val currentFavs = _challenges.value.filter { it.isFavorite }.map { it.id }
        _currentUser.update { it.copy(favoriteChallenges = currentFavs) }

        val uid = auth?.currentUser?.uid
        if (uid != null && firestore != null) {
            scope.launch {
                try {
                    db.collection("users").document(uid).update(
                        mapOf(
                            "favoriteChallenges" to currentFavs,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
                }
            }
        }
    }

    fun updateUserProfile(name: String, avatarEmoji: String, favoriteClub: String) {
        val trimmedName = name.trim().ifBlank { _currentUser.value.name }
        val finalAvatar = avatarEmoji.ifBlank { _currentUser.value.avatarEmoji }
        val finalClub = favoriteClub.ifBlank { _currentUser.value.favoriteClub }

        _currentUser.update { current ->
            current.copy(
                name = trimmedName,
                avatarEmoji = finalAvatar,
                favoriteClub = finalClub
            )
        }

        _leaderboard.update { list ->
            list.map { entry ->
                if (entry.isCurrentUser) {
                    entry.copy(name = trimmedName, avatarEmoji = finalAvatar, favoriteClub = finalClub)
                } else entry
            }
        }

        val uid = auth?.currentUser?.uid
        if (uid != null && firestore != null) {
            scope.launch {
                try {
                    db.collection("users").document(uid).update(
                        mapOf(
                            "name" to trimmedName,
                            "avatarEmoji" to finalAvatar,
                            "favoriteClub" to finalClub,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    db.collection("leaderboard").document(uid).update(
                        mapOf(
                            "name" to trimmedName,
                            "avatarEmoji" to finalAvatar,
                            "favoriteClub" to finalClub,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
                }
            }
        }
    }

    fun updateSoundSettings(settings: SoundSettings) {
        _soundSettings.value = settings
    }

    fun toggleSoundEffects(enabled: Boolean) {
        _soundSettings.update { it.copy(soundEffectsEnabled = enabled) }
    }

    fun toggleStadiumAmbience(enabled: Boolean) {
        _soundSettings.update { it.copy(stadiumAmbienceEnabled = enabled) }
    }

    fun setSoundVolume(volume: Float) {
        _soundSettings.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _soundSettings.update { it.copy(hapticFeedbackEnabled = enabled) }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun addCustomNotification(title: String, description: String, category: String, versionTag: String? = "v1.3", icon: String = "📢") {
        val newNotif = AppNotificationItem(
            id = "notif_custom_${System.currentTimeMillis()}",
            title = title,
            description = description,
            category = category,
            timestamp = "الآن",
            isRead = false,
            versionTag = versionTag,
            iconEmoji = icon,
            isImportant = true
        )
        _notifications.update { listOf(newNotif) + it }
    }

    // ==========================================
    // 100+ PLAYER GUESS QUESTIONS
    // ==========================================
    val playerQuestions: List<PlayerGuessQuestion> by lazy {
        generatePlayerQuestionsPool()
    }

    // ==========================================
    // 100+ CLUB GUESS QUESTIONS (WITH BADGES)
    // ==========================================
    val clubQuestions: List<ClubGuessQuestion> by lazy {
        generateClubQuestionsPool()
    }

    // ==========================================
    // 50+ WORLD CUP QUESTIONS
    // ==========================================
    val worldCupQuestions: List<WorldCupQuestion> by lazy {
        generateWorldCupQuestionsPool()
    }

    // ==========================================
    // SQUAD PLAYERS POOL (10 LEAGUES, 200M BUDGET)
    // ==========================================
    val squadPlayersPool: List<SquadPlayer> by lazy {
        generateSquadPlayersPool()
    }

    // ==========================================
    // 12 TOURNAMENTS FOR AI SIMULATION
    // ==========================================
    val tournamentChoices = listOf(
        TournamentSimulationChoice("ucl", "دوري أبطال أوروبا", "🏆", "قاري", "أقوى وأعرق بطولة قارية للأندية في العالم", 5),
        TournamentSimulationChoice("caf_cl", "دوري أبطال إفريقيا", "🌍", "قاري", "أميرة إفريقيا السمراء والصراع الشرس على عرش القارة", 5),
        TournamentSimulationChoice("epl", "الدوري الإنجليزي", "🦁", "دوري محلي", "البريميرليغ الأكثر إثارة وسرعة وتنافساً في العالم", 5),
        TournamentSimulationChoice("laliga", "الدوري الإسباني", "🇪🇸", "دوري محلي", "صراع الليغا الساحر وكلاسيكيات كرة القدم الفنية", 4),
        TournamentSimulationChoice("serie_a", "الدوري الإيطالي", "🇮🇹", "دوري محلي", "الكالتشيو التكتيكي والدفاعي العريق والندية الشديدة", 4),
        TournamentSimulationChoice("bundesliga", "الدوري الألماني", "🇩🇪", "دوري محلي", "كرة قدم هجومية سريعة وملاعب ممتلئة وأهداف غزيرة", 4),
        TournamentSimulationChoice("ligue_1", "الدوري الفرنسي", "🇫🇷", "دوري محلي", "دوري المواهب والسرعات الفائقة والمهارات العالية", 3),
        TournamentSimulationChoice("saudi_pl", "الدوري السعودي", "🇸🇦", "دوري محلي", "دوري روشن مع كوكبة من ألمع أساطير ونجوم العالم", 4),
        TournamentSimulationChoice("egyptian_pl", "الدوري المصري", "🇪🇬", "دوري محلي", "أعرق الدوريات العربية والإفريقية والمنافسة الجماهيرية", 3),
        TournamentSimulationChoice("mls", "الدوري الأمريكي", "🇺🇸", "دوري محلي", "الدوري الأمريكي المتصاعد بقوة مع حضور نجوم عالميين", 3),
        TournamentSimulationChoice("super_lig", "الدوري التركي", "🇹🇷", "دوري محلي", "أجواء جماهيرية نارية في إسطنبول ومنافسة ملحمية", 3),
        TournamentSimulationChoice("primeira_liga", "الدوري البرتغالي", "🇵🇹", "دوري محلي", "مصنع النجوم الأوروبي وصراع كبار لشبونة وبورتو", 3)
    )

    // ==========================================
    // AI TOURNAMENT SIMULATION ENGINE
    // ==========================================
    fun simulateTournament(
        tournament: TournamentSimulationChoice,
        squad: List<SquadPlayer>
    ): TournamentSimulationReport {
        val fwdPlayers = squad.filter { it.position == "FWD" }
        val midPlayers = squad.filter { it.position == "MID" }
        val defPlayers = squad.filter { it.position == "DEF" }
        val gkPlayers = squad.filter { it.position == "GK" }

        val attackRating = if (fwdPlayers.isNotEmpty()) (fwdPlayers.map { it.rating }.average()).toInt() else 75
        val midRating = if (midPlayers.isNotEmpty()) (midPlayers.map { it.rating }.average()).toInt() else 75
        val defRating = if (defPlayers.isNotEmpty()) (defPlayers.map { it.rating }.average()).toInt() else 75
        val gkRating = gkPlayers.firstOrNull()?.rating ?: 75

        // Chemistry bonus: leagues diversity and quality
        val uniqueLeagues = squad.map { it.league }.distinct().size
        val chemistry = ((100 - (uniqueLeagues * 3)) + (squad.size * 2)).coerceIn(60, 99)

        val opponents = getOpponentsForTournament(tournament.id)
        val stages = if (tournament.category == "قاري") {
            listOf("الدور ربع النهائي", "الدور نصف النهائي", "المباراة النهائية 🏆")
        } else {
            listOf("الجولة الحاسمة 1", "ديربي القمة 2", "موقعة حسم اللقب 3 🏆")
        }

        var isChampion = true
        val matches = mutableListOf<MatchSimulationStep>()

        stages.forEachIndexed { index, stage ->
            val opp = opponents.getOrElse(index) { "المنافس الشرس" }
            val oppLogo = getOpponentLogo(opp)

            // Calculate match chances
            val squadPower = (attackRating * 0.35 + midRating * 0.35 + defRating * 0.20 + gkRating * 0.10) + (chemistry * 0.1)
            val oppPower = 82 + (index * 3) + Random.nextInt(0, 5)

            val userGoals = if (squadPower >= oppPower) Random.nextInt(2, 4) else Random.nextInt(0, 2)
            val oppGoals = if (squadPower >= oppPower) Random.nextInt(0, userGoals) else Random.nextInt(userGoals + 1, userGoals + 3)

            val win = userGoals > oppGoals
            if (!win) isChampion = false

            val scorersList = mutableListOf<String>()
            repeat(userGoals) {
                val candidate = (fwdPlayers + midPlayers).shuffled().firstOrNull()?.name ?: "نجم الهجوم"
                scorersList.add(candidate)
            }

            val oppScorers = mutableListOf<String>()
            repeat(oppGoals) {
                oppScorers.add("هداف $opp")
            }

            val events = listOf(
                MatchSimulationEvent(
                    minute = 18,
                    title = "انطلاقة هجومية واعدة",
                    description = "ضغط هجومي عالي وتناغم رائع بين خط الوسط والهجوم."
                ),
                MatchSimulationEvent(
                    minute = 34,
                    title = if (userGoals > 0) "هدف محقق لفريقك! ⚽" else "فرصة ضائعة خطيرة",
                    description = if (userGoals > 0) "تسديدة متقنة من ${scorersList.firstOrNull() ?: "المهاجم"} تهز الشباك!" else "تألق حارس المنافس في إبعاد تسديدة صاروخية.",
                    isGoal = userGoals > 0,
                    isUserTeam = true
                ),
                MatchSimulationEvent(
                    minute = 65,
                    title = "تبديل وتدخل تكتيكي",
                    description = "تعديل في التمركز وضبط الإيقاع للسيطرة على وسط الملعب."
                ),
                MatchSimulationEvent(
                    minute = 88,
                    title = if (win) "حسم الموقعة بنجاح! 🔥" else "صافرة النهاية وضغط هائل",
                    description = if (win) "تنظيم دفاعي حديدي واستبسال للحفاظ على التقدم والتأهل." else "مباراة ملحمية كادت أن تنتهي بالتعادل في اللحظات الأخيرة."
                )
            )

            matches.add(
                MatchSimulationStep(
                    stageName = stage,
                    opponentName = opp,
                    opponentLogoSymbol = oppLogo,
                    userGoals = userGoals,
                    opponentGoals = oppGoals,
                    userScorers = scorersList,
                    opponentScorers = oppScorers,
                    events = events,
                    isWin = win
                )
            )
        }

        val verdict = if (isChampion) {
            "تحليل الذكاء الاصطناعي التكتيكي: أداء أسطوري وتناغم مثالي بين خطوط التشكيلة! استطاع فريقك السيطرة على الاستحواذ بنسبة 61% وخلق فرص نوعية حاسمة. خط الدفاع كان صلبًا للغاية، وتألق الهجوم في إنهاء الهجمات من أنصاف الفرص ليحصد لقب ${tournament.nameAr} بكل جدارة واستحقاق!"
        } else {
            "تحليل الذكاء الاصطناعي التكتيكي: قدم فريقك بطولة ملحمية ووصل لمراحل متقدمة جداً في ${tournament.nameAr}. نقطة القوة كانت في حيوية خط الوسط، لكن بعض الهجمات المرتدة للمنافس في اللحظات الحرجة صنعت الفارق. بالعمل على رفع الكيمستري ودعم عمق الدفاع ستكون البطل القادم دون شك!"
        }

        val xp = if (isChampion) 500 else 250
        addPoints(xp)

        return TournamentSimulationReport(
            tournamentName = tournament.nameAr,
            isChampion = isChampion,
            finalPlacement = if (isChampion) "بطل المسابقة وحامل اللقب 🏆🥇" else "وصيف البطولة والمركز الثاني 🥈",
            matches = matches,
            teamAttackRating = attackRating,
            teamMidfieldRating = midRating,
            teamDefenseRating = defRating,
            teamOverallChemistry = chemistry,
            tacticalAiVerdict = verdict,
            xpEarned = xp
        )
    }

    private fun getOpponentsForTournament(id: String): List<String> = when (id) {
        "ucl" -> listOf("بايرن ميونخ", "مانشستر سيتي", "ريال مدريد")
        "caf_cl" -> listOf("ماميلودي صن داونز", "الترجي التونسي", "الأهلي المصري")
        "epl" -> listOf("أرسنال", "ليفربول", "مانشستر سيتي")
        "laliga" -> listOf("أتلتيكو مدريد", "برشلونة", "ريال مدريد")
        "serie_a" -> listOf("يوفنتوس", "إيه سي ميلان", "إنتر ميلان")
        "bundesliga" -> listOf("لايبزيغ", "بوروسيا دورتموند", "بايرن ميونخ")
        "ligue_1" -> listOf("مارسيليا", "موناكو", "باريس سان جيرمان")
        "saudi_pl" -> listOf("الاتحاد السعودي", "النصر السعودي", "الهلال السعودي")
        "egyptian_pl" -> listOf("بيراميدز", "الزمالك", "الأهلي المصري")
        "mls" -> listOf("لوس أنجلوس إف سي", "كولومبوس كرو", "إنتر ميامي")
        "super_lig" -> listOf("بشكتاش", "فنربخشة", "غلطة سراي")
        "primeira_liga" -> listOf("سبورتينغ لشبونة", "إف سي بورتو", "بنفيكا")
        else -> listOf("المنافس الأول", "المنافس الثاني", "المنافس النهائي")
    }

    private fun getOpponentLogo(name: String): String = when {
        name.contains("ريال مدريد") -> "real_madrid"
        name.contains("برشلونة") -> "barcelona"
        name.contains("الأهلي") -> "ahly"
        name.contains("الزمالك") -> "zamalek"
        name.contains("سيتي") -> "man_city"
        name.contains("ليفربول") -> "liverpool"
        name.contains("بايرن") -> "bayern"
        name.contains("باريس") -> "psg"
        name.contains("الهلال") -> "hilal"
        name.contains("النصر") -> "nassr"
        name.contains("الاتحاد") -> "ittihad"
        name.contains("إنتر ميلان") -> "inter"
        name.contains("ميلان") -> "milan"
        name.contains("يوفنتوس") -> "juventus"
        name.contains("أرسنال") -> "arsenal"
        name.contains("دورتموند") -> "dortmund"
        name.contains("أتلتيكو") -> "atletico"
        name.contains("بيراميدز") -> "pyramids"
        name.contains("ميامي") -> "inter_miami"
        name.contains("غلطة سراي") -> "galatasaray"
        name.contains("فنربخشة") -> "fenerbahce"
        name.contains("بورتو") -> "porto"
        name.contains("بنفيكا") -> "benfica"
        name.contains("سبورتينغ") -> "sporting"
        else -> "generic"
    }

    // ==========================================
    // POOL GENERATORS IMPLEMENTATION
    // ==========================================
    private fun generatePlayerQuestionsPool(): List<PlayerGuessQuestion> {
        val list = mutableListOf<PlayerGuessQuestion>()

        fun add(
            diff: ChallengeDifficulty,
            nat: String,
            clubs: List<String>,
            num: Int,
            pos: String,
            clue: String,
            correct: String,
            opts: List<String>
        ) {
            val fullOpts = (opts + correct).distinct().shuffled()
            list.add(
                PlayerGuessQuestion(
                    id = list.size + 1,
                    difficulty = diff,
                    hintNationality = nat,
                    hintClubs = clubs,
                    hintNumber = num,
                    hintPosition = pos,
                    specialClue = clue,
                    correctAnswer = correct,
                    options = fullOpts
                )
            )
        }

        // 1 - 20 (EASY & ICONS)
        add(ChallengeDifficulty.EASY, "مصر 🇪🇬", listOf("المقاولون", "بازل", "تشيلسي", "روما", "ليفربول"), 11, "جناح أيمن", "الملك المصري والهداف التاريخي لليفربول في الدوري الإنجليزي", "محمد صلاح", listOf("عمر مرموش", "محمود تريزيجيه", "رياض محرز"))
        add(ChallengeDifficulty.EASY, "الأرجنتين 🇦🇷", listOf("برشلونة", "باريس سان جيرمان", "إنتر ميامي"), 10, "مهاجم / صانع ألعاب", "البرغوث الحاصل على 8 كرات ذهبية وبطل مونديال 2022", "ليونيل ميسي", listOf("أنخيل دي ماريا", "سيرجيو أغويرو", "باولو ديبالا"))
        add(ChallengeDifficulty.EASY, "البرتغال 🇵🇹", listOf("سبورتينغ", "مانشستر يونايتد", "ريال مدريد", "يوفنتوس", "النصر"), 7, "مهاجم", "الدون صاحب الرقم القياسي لأكبر عدد أهداف في تاريخ كرة القدم", "كريستيانو رونالدو", listOf("برونو فيرنانديز", "لويس فيغو", "برناردو سيلفا"))
        add(ChallengeDifficulty.EASY, "النرويج 🇳🇴", listOf("مولده", "سالزبورغ", "دورتموند", "مانشستر سيتي"), 9, "رأس حربة", "الغول التهديفي محطم الأرقام القياسية في البريميرليغ", "إيرلينغ هالاند", listOf("مارتن أوديغارد", "ألكسندر إيزاك", "فيكتور جيوكيريس"))
        add(ChallengeDifficulty.EASY, "فرنسا 🇫🇷", listOf("موناكو", "باريس سان جيرمان", "ريال مدريد"), 9, "جناح / مهاجم", "الصاروخ الفرنسي الفائز بمونديال 2018 وصاحب هاتريك نهائي 2022", "كيليان مبابي", listOf("عثمان ديمبيلي", "أنطوان غريزمان", "أوريليان تشواميني"))
        add(ChallengeDifficulty.EASY, "البرازيل 🇧🇷", listOf("فلامنغو", "ريال مدريد"), 7, "جناح أيسر", "الساحر البرازيلي الحاسم في نهائيات دوري أبطال أوروبا لريال مدريد", "فينيسيوس جونيور", listOf("رودريغو", "رافينيا", "نيمار داسيلفا"))
        add(ChallengeDifficulty.EASY, "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", listOf("برمنغهام", "دورتموند", "ريال مدريد"), 5, "لاعب وسط", "الفتى الذهبي صاحب هدف الكلاسيكو القاتل ورقم الأسطورة زيدان", "جود بيلينغهام", listOf("فيل فودين", "ديكلان رايس", "بوكايو ساكا"))
        add(ChallengeDifficulty.EASY, "إسبانيا 🇪🇸", listOf("أكاديمية لا ماسيا", "برشلونة"), 19, "جناح أيمن", "أصغر لاعب يسجل ويصنع في تاريخ اليورو وبطل أمم أوروبا 2024", "لامين يامال", listOf("غافي", "بيدري", "نيكو ويليامز"))
        add(ChallengeDifficulty.EASY, "بلجيكا 🇧🇪", listOf("جينك", "تشيلسي", "فولفسبورغ", "مانشستر سيتي"), 17, "صانع ألعاب", "مهندس التمريرات السحرية وأفضل صانع ألعاب في العقد الأخير", "كيفين دي بروين", listOf("إيدين هازارد", "لوكاكو", "لياندرو تروسار"))
        add(ChallengeDifficulty.EASY, "كرواتيا 🇭🇷", listOf("دينامو زغرب", "توتنهام", "ريال مدريد"), 10, "لاعب وسط", "مايسترو الكرة الذهبية 2018 وقائد خط وسط الريال التاريخي", "لوكا مودريتش", listOf("إيفان راكيتيتش", "ماتيو كوفاسيتش", "بروزوفيتش"))
        add(ChallengeDifficulty.EASY, "بولندا 🇵🇱", listOf("ليخ بوزنان", "دورتموند", "بايرن ميونخ", "برشلونة"), 9, "رأس حربة", "صاحب أسرع خماسية في تاريخ البوندسليغا في 9 دقائق فقط", "روبرت ليفاندوفسكي", listOf("بيوتر زيلينسكي", "أركاديوش ميليك", "فويتشيك تشيزني"))
        add(ChallengeDifficulty.EASY, "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", listOf("توتنهام", "ليستر سيتي", "بايرن ميونخ"), 9, "مهاجم", "الهداف التاريخي لمنتخب إنجلترا وقناص البايرن المتوج بالحذاء الذهبي", "هاري كين", listOf("واين روني", "ماركوس راشفورد", "جاك غريليش"))
        add(ChallengeDifficulty.EASY, "الجزائر 🇩🇿", listOf("لوهافر", "ليستر سيتي", "مانشستر سيتي", "الأهلي السعودي"), 26, "جناح أيمن", "رياض محرز فخر العرب وصانع معجزة ليستر سيتي 2016", "رياض محرز", listOf("سفيان فيغولي", "إسماعيل بن ناصر", "يوسف بلايلي"))
        add(ChallengeDifficulty.EASY, "المغرب 🇲🇦", listOf("أياكس", "تشيلسي", "غلطة سراي"), 22, "صانع ألعاب", "الساحر المغربي صاحب التسديدات القاتلة ونجم مونديال 2022", "حكيم زياش", listOf("أشرف حكيمي", "عز الدين أوناحي", "سفيان أمرابط"))
        add(ChallengeDifficulty.EASY, "المغرب 🇲🇦", listOf("ريال مدريد", "دورتموند", "إنتر ميلان", "باريس سان جيرمان"), 2, "ظهير أيمن", "القطار المغربي السريع وأفضل ظهير أيمن في العالم حالياً", "أشرف حكيمي", listOf("نصير مزراوي", "نايف أكرد", "يحيى عطية الله"))
        add(ChallengeDifficulty.EASY, "مصر 🇪🇬", listOf("وادي دجلة", "فولفسبورغ", "سانت باولي", "فرانكفورت"), 7, "مهاجم / جناح", "نجم الدوري الألماني المتألق بالركلات الحرة والأهداف الصاروخية", "عمر مرموش", listOf("مصطفى محمد", "أحمد حسن كوكا", "رمضان صبحي"))
        add(ChallengeDifficulty.EASY, "السعودية 🇸🇦", listOf("الهلال", "فياريال"), 10, "جناح / صانع ألعاب", "التورنيدو صاحب الهدف التاريخي أمام الأرجنتين في مونديال قطر", "سالم الدوسري", listOf("فهد المولد", "سلمان الفرج", "ياسر القحطاني"))
        add(ChallengeDifficulty.EASY, "مصر 🇪🇬", listOf("غزل المحلة", "حرس الحدود", "ميتلاند", "الزمالك", "الأهلي"), 22, "لاعب وسط", "دينامو خط الوسط المصري وصاحب التسديدات القوية والمهارات العالية", "إمام عاشور", listOf("مروان عطية", "محمد النني", "طارق حامد"))
        add(ChallengeDifficulty.EASY, "مصر 🇪🇬", listOf("ليرس", "ناسيونال ماديرا", "الزمالك"), 25, "جناح أيمن", "أفضل صانع ألعاب في الدوري المصري والهداف الحاسم للقلعة البيضاء", "أحمد مصطفى زيزو", listOf("شيكابالا", "ناصر ماهر", "مصطفى فتحي"))
        add(ChallengeDifficulty.EASY, "مصر 🇪🇬", listOf("طلائع الجيش", "بتروجت", "الأهلي"), 1, "حارس مرمى", "سد مصر المنيع وقائد الأهلي بطل إفريقيا التاريخي ورجل مباريات الحسم", "محمد الشناوي", listOf("محمد أبو جبل", "مصطفى شوبير", "أحمد الشناوي"))

        // 21 - 50 (MEDIUM: STARS & CONTEMPORARY)
        add(ChallengeDifficulty.MEDIUM, "المغرب 🇲🇦", listOf("الوداد", "أتلتيكو مدريد", "إشبيلية", "الهلال"), 37, "حارس مرمى", "حامي عرين أسود الأطلس وصاحب جائزة زامورا وأفضل حارس في إفريقيا", "ياسين بونو", listOf("منير المحمدي", "إدوارد ميندي", "أندريه أونانا"))
        add(ChallengeDifficulty.MEDIUM, "البرازيل 🇧🇷", listOf("سانتوس", "برشلونة", "باريس سان جيرمان", "الهلال"), 10, "جناح / صانع ألعاب", "الساحر البرازيلي والهداف التاريخي لمنتخب السامبا مع بيليه", "نيمار داسيلفا", listOf("فيليب كوتينيو", "روبرتو فيرمينو", "غابرييل جيسوس"))
        add(ChallengeDifficulty.MEDIUM, "فرنسا 🇫🇷", listOf("ليون", "ريال مدريد", "الاتحاد"), 9, "رأس حربة", "الحكومة كريم الفائز بالكرة الذهبية 2022 وقائد ريمونتادات الريال الخالدة", "كريم بنزيما", listOf("أوليفييه جيرو", "ألكسندر لاكازيت", "وسام بن يدر"))
        add(ChallengeDifficulty.MEDIUM, "ألمانيا 🇩🇪", listOf("شالكه", "بايرن ميونخ"), 1, "حارس مرمى", "مبتكر أسلوب حارس القشاش (Sweeper-Keeper) وبطل مونديال 2014", "مانويل نوير", listOf("تير شتيغن", "أوليفر كان", "ينس ليمان"))
        add(ChallengeDifficulty.MEDIUM, "هولندا 🇳🇱", listOf("جرونينجن", "سيلتيك", "ساوثهامبتون", "ليفربول"), 4, "قلب دفاع", "الصخرة الهولندية وأفضل مدافع في العالم الفائز بجائزة أفضل لاعب بأوروبا", "فيرجيل فان دايك", listOf("ماتيس دي ليخت", "ناثان أكي", "ستيفان دي فري"))
        add(ChallengeDifficulty.MEDIUM, "إسبانيا 🇪🇸", listOf("إشبيلية", "ريال مدريد", "باريس سان جيرمان"), 4, "قلب دفاع", "صاحب رأسية الدقيقة 92:48 التاريخية وقائد الريال وإسبانيا التاريخي", "سيرجيو راموس", listOf("جيرارد بيكيه", "كارليس بويول", "إيمريك لابورت"))
        add(ChallengeDifficulty.MEDIUM, "ألمانيا 🇩🇪", listOf("بايرن ميونخ", "باير ليفركوزن", "ريال مدريد"), 8, "لاعب وسط", "مهندس التمريرات الدقيقة بدقة 95% الفائز بدوري الأبطال 6 مرات", "توني كروس", listOf("باستيان شفاينشتايغر", "إلكاي غوندوغان", "جوشوا كيميش"))
        add(ChallengeDifficulty.MEDIUM, "فرنسا 🇫🇷", listOf("بولوني", "كان", "ليستر سيتي", "تشيلسي", "الاتحاد"), 7, "لاعب وسط ارتكاز", "الرئة التي تغطي كوكب الأرض وبطل البريميرليغ مع فريقين ومونديال 2018", "نغولو كانتي", listOf("بول بوغبا", "بليز ماتويدي", "أدريان رابيو"))
        add(ChallengeDifficulty.MEDIUM, "كوريا الجنوبية 🇰🇷", listOf("هامبورغ", "باير ليفركوزن", "توتنهام"), 7, "جناح / هداف", "نجم آسيا الأول وهداف الدوري الإنجليزي الحاصل على جائزة بوشكاش", "سون هيونغ مين", listOf("كاورو ميتوما", "تاكيهيرو تومياسو", "كيم مين جاي"))
        add(ChallengeDifficulty.MEDIUM, "الأوروغواي 🇺🇾", listOf("ناسيونال", "أياكس", "ليفربول", "برشلونة", "أتلتيكو", "إنتر ميامي"), 9, "مهاجم", "السفاح الأوروغوياني الحائز على الحذاء الذهبي الأوروبي مرتين", "لويس سواريز", listOf("إدينسون كافاني", "دييغو فورلان", "داروين نونيز"))
        add(ChallengeDifficulty.MEDIUM, "البرتغال 🇵🇹", listOf("بنفيكا", "موناكو", "مانشستر سيتي"), 20, "صانع ألعاب", "عقل بيب غوارديولا المفكر ورجل الثلاثية التاريخية للسيتيزنز", "برناردو سيلفا", listOf("برونو فيرنانديز", "جواو فيليكس", "ديوغو جوتا"))
        add(ChallengeDifficulty.MEDIUM, "مصر 🇪🇬", listOf("الأهلي", "أندرلخت", "قاسم باشا", "أستون فيلا", "طرابزون سبور", "الريان"), 7, "جناح أيسر", "الفهد المصري صاحب الأهداف الحاسمة في تصفيات المونديال وكأس إفريقيا", "محمود حسن تريزيجيه", listOf("رمضان صبحي", "حسين الشحات", "طاهر محمد طاهر"))
        add(ChallengeDifficulty.MEDIUM, "تونس 🇹🇳", listOf("النجم الساحلي", "فرانكفورت", "الدحيل"), 7, "صانع ألعاب", "نمس قرطاج والهداف التاريخي البارع في صناعة الألعاب", "يوسف المساكني", listOf("حنبعل المجبري", "إلياس السخيري", "وهبي الخزري"))
        add(ChallengeDifficulty.MEDIUM, "المغرب 🇲🇦", listOf("أولمبيك آسفي", "النصر", "الاتحاد", "الشباب"), 9, "رأس حربة", "الجلاد المغربي صاحب الرقم القياسي التاريخي لأكثر أهداف في موسم واحد بالدوري السعودي", "عبد الرزاق حمد الله", listOf("يوسف النصيري", "أيوب الكعبي", "سفيان رحيمي"))
        add(ChallengeDifficulty.MEDIUM, "المغرب 🇲🇦", listOf("الرجاء", "العين الإماراتي"), 21, "جناح / مهاجم", "هداف أولمبياد باريس 2024 وقائد العين لتحقيق دوري أبطال آسيا", "سفيان رحيمي", listOf("إبراهيم دياز", "أمين عدلي", "إلياس أخوماش"))
        add(ChallengeDifficulty.MEDIUM, "مصر 🇪🇬", listOf("الزمالك", "الغرافة", "نانت"), 14, "رأس حربة", "الأناكوندا مهاجم منتخب مصر ونجم الدوري الفرنسي بالضربات الرأسية", "مصطفى محمد", listOf("مروان محسن", "حسام حسن", "أحمد ياسر ريان"))
        add(ChallengeDifficulty.MEDIUM, "السنغال 🇸🇳", listOf("ميتز", "سالزبورغ", "ساوثهامبتون", "ليفربول", "بايرن ميونخ", "النصر"), 10, "جناح / مهاجم", "أسد التيرانغا الفائز بكأس إفريقيا وأفضل لاعب في إفريقيا مرتين", "ساديو ماني", listOf("إسماعيلا سار", "نيكولاس جاكسون", "باب غايي"))
        add(ChallengeDifficulty.MEDIUM, "إسبانيا 🇪🇸", listOf("فياريال", "أتلتيكو مدريد", "مانشستر سيتي"), 16, "لاعب وسط ارتكاز", "حجر أساس السيتي وإسبانيا والمتوج بجائزة أفضل لاعب في يورو 2024", "رودري", listOf("سيرجيو بوسكيتس", "فابيان رويز", "ميكيل ميرينو"))
        add(ChallengeDifficulty.MEDIUM, "البرازيل 🇧🇷", listOf("إنترناسيونال", "روما", "ليفربول"), 1, "حارس مرمى", "الحارس البرازيلي صاحب الرأسية التاريخية في الدقيقة 95 مع ليفربول", "أليسون بيكر", listOf("إيدرسون مورايس", "ديدا", "خوليو سيزار"))
        add(ChallengeDifficulty.MEDIUM, "الأرجنتين 🇦🇷", listOf("أرسنال", "خيتافي", "أستون فيلا"), 23, "حارس مرمى", "ديبو الأخطبوط رجل ركلات الترجيح وبطل المونديال وأفضل حارس في العالم مرتين", "إيميليانو مارتينيز", listOf("فرانكو أرماني", "سيرجيو روميرو", "خيرونيمو رولي"))

        // 41 - 70 (HARD: LEGENDS & GLOBAL TITANS)
        add(ChallengeDifficulty.HARD, "فرنسا 🇫🇷", listOf("كان", "بوردو", "يوفنتوس", "ريال مدريد"), 5, "صانع ألعاب", "زيزو صاحب الفولي التاريخي في غلاسكو ونائط رأسيتي نهائي 1998", "زين الدين زيدان", listOf("ميشيل بلاتيني", "ديدييه ديشامب", "فرانك ريبيري"))
        add(ChallengeDifficulty.HARD, "البرازيل 🇧🇷", listOf("كروزيرو", "أيندهوفن", "برشلونة", "إنتر ميلان", "ريال مدريد", "ميلان"), 9, "رأس حربة", "الظاهرة الفائز بمونديال 1994 و2002 وهداف المونديال الأسطوري برصيد 15 هدفاً", "رونالدو نازاريو", listOf("روماريو", "ريفالدو", "أدريانو"))
        add(ChallengeDifficulty.HARD, "البرازيل 🇧🇷", listOf("غريميو", "باريس سان جيرمان", "برشلونة", "ميلان", "فلامنغو"), 10, "صانع ألعاب", "ساحر السامبا الذي صفق له جمهور سانتياغو برنابيو في كلاسيكو 2005", "رونالدينيو", listOf("كاكا", "روبينيو", "دينيلسون"))
        add(ChallengeDifficulty.HARD, "البرازيل 🇧🇷", listOf("ساو باولو", "ميلان", "ريال مدريد", "أورلاندو سيتي"), 22, "صانع ألعاب", "أمير ميلان الأنيق وآخر لاعب فاز بالكرة الذهبية قبل حقبة ميسي ورونالدو", "ريكاردو كاكا", listOf("باتو", "سيدورف", "ريفالدو"))
        add(ChallengeDifficulty.HARD, "إيطاليا 🇮🇹", listOf("بارما", "يوفنتوس", "باريس سان جيرمان"), 1, "حارس مرمى", "العنكبوت الإيطالي بطل كأس العالم 2006 والأكثر خوضاً للمباريات الدولية مع الآتزوري", "جانلويجي بوفون", listOf("دينو زوف", "والتر زينغا", "جانلويجي دوناروما"))
        add(ChallengeDifficulty.HARD, "إيطاليا 🇮🇹", listOf("بريشيا", "إنتر ميلان", "ميلان", "يوفنتوس", "نيويورك سيتي"), 21, "صانع ألعاب متأخر", "المايسترو أستاذ الضربات الحرة ومصمم التمريرات من العمق", "أندريا بيرلو", listOf("جينارو غاتوزو", "ديميتريو ألبرتيني", "دانييلي دي روسي"))
        add(ChallengeDifficulty.HARD, "إيطاليا 🇮🇹", listOf("ميلان"), 3, "مدافع / ظهير", "القائد التاريخي للميلان الذي قضى 25 موسماً بالقميص الروسونيري وحقق 5 دوري أبطال", "باولو مالديني", listOf("فرانكو باريزي", "أليساندرو نستا", "فابيو كانافارو"))
        add(ChallengeDifficulty.HARD, "إسبانيا 🇪🇸", listOf("ريال مدريد", "بورتو"), 1, "حارس مرمى", "القديس كاسياس بطل العالم 2008 و2010 و2012 وتصدياته الأسطورية ضد روبن", "إيكر كاسياس", listOf("فيكتور فالديز", "دافيد دي خيا", "سانتياغو كانيزاريس"))
        add(ChallengeDifficulty.HARD, "إسبانيا 🇪🇸", listOf("أكاديمية لا ماسيا", "برشلونة", "فيسيل كوبه", "الإمارات"), 8, "لاعب وسط", "الرسام صاحب الهدف التاريخي في نهائي مونديال 2010 في الدقيقة 116", "أندريس إنييستا", listOf("تشافي هيرنانديز", "سيسك فابريغاس", "ديفيد سيلفا"))
        add(ChallengeDifficulty.HARD, "إسبانيا 🇪🇸", listOf("أكاديمية لا ماسيا", "برشلونة", "السد القطري"), 6, "لاعب وسط", "مايسترو التيكي تاكا وأفضل صانع تمريرات قصيرة وتحكم في نسق اللعب بالتاريخ", "تشافي هيرنانديز", listOf("سيرجيو بوسكيتس", "تشابي ألونسو", "خوان ماتا"))
        add(ChallengeDifficulty.HARD, "الأرجنتين 🇦🇷", listOf("أرجنتينوس جونيورز", "بوكا جونيورز", "برشلونة", "نابولي", "إشبيلية"), 10, "صانع ألعاب", "أسطورة الأرجنتين ونابولي الخالدة صاحب هدف القرن ويد الرب في مونديال 1986", "دييغو مارادونا", listOf("ماريو كيمبس", "غابرييل باتيستوتا", "خوان رومان ريكيلمي"))
        add(ChallengeDifficulty.HARD, "البرازيل 🇧🇷", listOf("سانتوس", "نيويورك كوزموس"), 10, "مهاجم", "الجوهرة السوداء وبطل كأس العالم 3 مرات التاريخي والمسجل لأكثر من 1000 هدف", "بيليه", listOf("غارينشيا", "زيكو", "سوكراتيس"))
        add(ChallengeDifficulty.HARD, "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", listOf("مانشستر يونايتد", "ريال مدريد", "لوس أنجلوس غالاكسي", "ميلان", "باريس سان جيرمان"), 7, "لاعب وسط", "ملك العرضيات والركلات الحرة المقوسة وقائد منتخب إنجلترا الشهير", "ديفيد بيكهام", listOf("بول سكولز", "ستيفن جيرارد", "فرانك لامبارد"))
        add(ChallengeDifficulty.HARD, "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", listOf("ليفربول", "لوس أنجلوس غالاكسي"), 8, "لاعب وسط", "قائد ليفربول الأسطوري وبطل معجزة إسطنبول 2005 وصاحب التسديدات الصاروخية", "ستيفن جيرارد", listOf("فرانك لامبارد", "مايكل أوين", "روبي فاولر"))
        add(ChallengeDifficulty.HARD, "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", listOf("وست هام", "تشيلسي", "مانشستر سيتي", "نيويورك سيتي"), 8, "لاعب وسط", "الهداف التاريخي لنادي تشيلسي والوسط الأكثر تسجيلاً في تاريخ البريميرليغ", "فرانك لامبارد", listOf("جون تيري", "ديدييه دروغبا", "جو كول"))
        add(ChallengeDifficulty.HARD, "ساحل العاج 🇨🇮", listOf("لومان", "غانغان", "مارسيليا", "تشيلسي", "شنغهاي", "غلطة سراي"), 11, "رأس حربة", "الفيل الإيفواري الحاسم في نهائي الأبطال 2012 وقائد الجيل الذهبي لكوت ديفوار", "ديدييه دروغبا", listOf("يايا توريه", "سالومون كالو", "ويلفريد بوني"))
        add(ChallengeDifficulty.HARD, "ساحل العاج 🇨🇮", listOf("بيفيرين", "ميتالورغ", "أولمبياكوس", "موناكو", "برشلونة", "مانشستر سيتي"), 42, "لاعب وسط بوكس تو بوكس", "الدبابة الإيفوارية أفضل لاعب في إفريقيا 4 مرات متتالية ونجم السيتي التاريخي", "يايا توريه", listOf("كولو توريه", "شيخ إسماعيل تيوتي", "فرانك كيسييه"))
        add(ChallengeDifficulty.HARD, "فرنسا 🇫🇷", listOf("موناكو", "يوفنتوس", "أرسنال", "برشلونة", "نيويورك ريد بولز"), 14, "مهاجم", "الغزال الأسمر الهداف التاريخي لأرسنال وقائد موسم اللاهزيمة 2004 الذهبي", "تييري هنري", listOf("نيكولا أنيلكا", "ديفيد تريزيغيه", "روبرت بيريز"))
        add(ChallengeDifficulty.HARD, "هولندا 🇳🇱", listOf("أياكس", "إنتر ميلان", "أرسنال"), 10, "مهاجم / صانع ألعاب", "الهولندي غير الطائر صاحب أجمل هدف في تاريخ المونديال ضد الأرجنتين 1998", "دينيس بيركامب", listOf("باتريك كلايفرت", "روبن فان بيرسي", "رود فان نيستلروي"))
        add(ChallengeDifficulty.HARD, "هولندا 🇳🇱", listOf("خرونينغن", "أيندهوفن", "تشيلسي", "ريال مدريد", "بايرن ميونخ"), 10, "جناح أيمن", "الجناح الهولندي الطائر صاحب التسديدة اللولبية بالقدم اليسرى بعد التوغل للداخل", "أريين روبن", listOf("فرانك ريبيري", "ويسلي شنايدر", "ديرك كويت"))

        // 71 - 100 (LEGEND: CLASSICS & ARAB LEGENDS)
        add(ChallengeDifficulty.LEGEND, "مصر 🇪🇬", listOf("الترسانة", "الأهلي", "بني ياس"), 22, "صانع ألعاب", "أمير القلوب والماجيكو صانع أمجاد الأهلي ومنتخب مصر في ثلاثية أمم إفريقيا", "محمد أبو تريكة", listOf("محمد بركات", "حسام عاشور", "وليد سليمان"))
        add(ChallengeDifficulty.LEGEND, "مصر 🇪🇬", listOf("الأهلي", "باوك اليوناني", "نيوشاتيل السويسري", "العين", "الزمالك"), 9, "رأس حربة", "العميد والهداف التاريخي لمنتخب مصر وصاحب هدف التأهل لمونديال 1990", "حسام حسن", listOf("حازم إمام", "أحمد الكاس", "عماد متعب"))
        add(ChallengeDifficulty.LEGEND, "مصر 🇪🇬", listOf("الزمالك", "أودينيزي", "دي غرافشاب"), 14, "صانع ألعاب", "الثعلب الصغير وساحر الزمالك وأيقونة المهارة واللعب الجميل في الكرة المصرية", "حازم إمام", listOf("شيكابالا", "أيمن يونس", "فاروق جعفر"))
        add(ChallengeDifficulty.LEGEND, "مصر 🇪🇬", listOf("دمياط", "الأهلي", "سيون", "الإسماعيلي", "الزمالك", "المريخ", "التعاون"), 1, "حارس مرمى", "السد العالي وأفضل حارس في تاريخ إفريقيا وحامي عرين مصر التاريخي في المونديال", "عصام الحضري", listOf("نادر السيد", "إكرامي", "ثابت البطل"))
        add(ChallengeDifficulty.LEGEND, "السعودية 🇸🇦", listOf("الشباب"), 10, "صانع ألعاب", "صاحب أجمل أهداف المونديال ضد بلجيكا 1994 بعد مراوغة مارادونية من نصف الملعب", "سعيد العويران", listOf("ماجد عبد الله", "فؤاد أنور", "فهد الهريفي"))
        add(ChallengeDifficulty.LEGEND, "السعودية 🇸🇦", listOf("النصر"), 9, "رأس حربة", "بيليه الصحراء وهداف العرب التاريخي وقائد الأخضر لأول لقب آسيوي", "ماجد عبد الله", listOf("سامي الجابر", "يوسف الثنيان", "حمزة إدريس"))
        add(ChallengeDifficulty.LEGEND, "السعودية 🇸🇦", listOf("الهلال", "وولفرهامبتون"), 9, "مهاجم", "الذئب السعودي الذي سجل في 3 بطولات كأس عالم مختلفة (1994, 1998, 2006)", "سامي الجابر", listOf("ياسر القحطاني", "نواف التمياط", "محمد الشلهوب"))
        add(ChallengeDifficulty.LEGEND, "السعودية 🇸🇦", listOf("الهلال"), 10, "صانع ألعاب", "الفيلسوف صاحب المراوغات الساحرة وأفضل ممرر كرات في تاريخ الكرة السعودية", "يوسف الثنيان", listOf("محمد الشلهوب", "سلمان الفرج", "خالد مسعد"))
        add(ChallengeDifficulty.LEGEND, "السعودية 🇸🇦", listOf("القادسية", "الهلال", "العين"), 20, "رأس حربة", "الكاسر وأفضل لاعب في آسيا 2007 وقناص الأهداف الحاسمة بالرأس والقدمين", "ياسر القحطاني", listOf("ناصر الشمراني", "نايف هزازي", "محمد السهلاوي"))
        add(ChallengeDifficulty.LEGEND, "الكاميرون 🇨🇲", listOf("تونير ياوندي", "باستيا", "سانت إتيان", "مونبلييه"), 9, "مهاجم", "أكبر لاعب يسجل في تاريخ المونديال بعمر 42 عاماً ورقصته الشهيرة عند الراية", "روجيه ميلا", listOf("صامويل إيتو", "باتريك مبوما", "ريغوبرت سونغ"))
        add(ChallengeDifficulty.LEGEND, "الكاميرون 🇨🇲", listOf("ريال مدريد", "مايوركا", "برشلونة", "إنتر ميلان", "أنجي", "تشيلسي"), 9, "رأس حربة", "الأسد الكاميروني الفائز بالثلاثية مع فريقين مختلفين وهداف الكان التاريخي", "صامويل إيتو", listOf("صامويل أومتيتي", "ألكسندر سونغ", "فنسنت أبو بكر"))
        add(ChallengeDifficulty.LEGEND, "البرتغال 🇵🇹", listOf("سبورتينغ", "برشلونة", "ريال مدريد", "إنتر ميلان"), 7, "جناح أيمن", "صاحب صفقة الانتقال الأكثر جدلاً في تاريخ الكلاسيكو والفائز بالكرة الذهبية 2000", "لويس فيغو", listOf("روي كوستا", "باولو فوتري", "سيموني سابروزا"))
        add(ChallengeDifficulty.LEGEND, "أوكرانيا 🇺🇦", listOf("دينامو كييف", "ميلان", "تشيلسي"), 7, "مهاجم", "القناص الأوكراني الحائز على الكرة الذهبية 2004 وهداف ديربي الغضب التاريخي", "أندري شيفشينكو", listOf("سيرجي ريبروف", "أندري يارمولينكو", "يفغين كونوبليانكا"))
        add(ChallengeDifficulty.LEGEND, "جمهورية التشيك 🇨🇿", listOf("سبارتا براغ", "لاتسيو", "يوفنتوس"), 11, "لاعب وسط", "الزئبقي التشيكي صاحب الكرة الذهبية 2003 الذي قاد اليوفي لنهائي الأبطال", "بافل نيدفيد", listOf("توماس روزيسكي", "ميلان باروش", "كاريل بوبورسكي"))
        add(ChallengeDifficulty.LEGEND, "السويد 🇸🇪", listOf("مالمو", "أياكس", "يوفنتوس", "إنتر", "برشلونة", "ميلان", "باريس", "اليونايتد"), 11, "مهاجم", "السلطان صاحب الأهداف البهلوانية والتصريحات النارية وبطل 4 دوريات أوروبية", "زلاتان إبراهيموفيتش", listOf("هنريك لارسون", "فريدريك ليونغبرغ", "إلكسندر إيزاك"))
        add(ChallengeDifficulty.LEGEND, "غانا 🇬🇭", listOf("أودينيزي", "باستيا", "ليون", "تشيلسي", "ريال مدريد", "ميلان"), 5, "لاعب وسط ارتكاز", "الثور الغاني مايكل صاحب التسديدة الخارقة في مرمى برشلونة 2009", "مايكل إيسيان", listOf("سولي علي مونتاري", "أسامواه جيان", "كوادوو أسامواه"))
        add(ChallengeDifficulty.LEGEND, "ألمانيا 🇩🇪", listOf("هامبورغ", "بايرن ميونخ", "تشيلسي"), 13, "لاعب وسط", "قائد المانشافت وصاحب التسديدات بعيدة المدى وبطل الثنائية الألمانية", "مايكل بالاك", listOf("تورستن فرينغز", "بيرند شنايدر", "ينس يريميز"))
        add(ChallengeDifficulty.LEGEND, "ألمانيا 🇩🇪", listOf("كايزرسلاوترن", "فيردر بريمن", "بايرن ميونخ", "لاتسيو"), 11, "رأس حربة", "الهداف التاريخي لبطولات كأس العالم برصيد 16 هدفاً وقفزته البهلوانية الشهيرة", "ميروسلاف كلوزه", listOf("لوتار ماتيوس", "يورغن كلينسمان", "لوكاس بودولسكي"))
        add(ChallengeDifficulty.LEGEND, "فرنسا 🇫🇷", listOf("نانت", "مارسيليا", "ميلان", "تشيلسي"), 8, "مدافع / ارتكاز", "الصخرة الفرنسية وبطل مونديال 1998 واليورو 2000 وأحد أصلب المدافعين في التاريخ", "مارسيل ديسايي", listOf("لوران بلان", "ليليان تورام", "إريك أبيدال"))
        add(ChallengeDifficulty.LEGEND, "إيطاليا 🇮🇹", listOf("نابولي", "بارما", "إنتر ميلان", "يوفنتوس", "ريال مدريد"), 5, "قلب دفاع", "المدافع الوحيد في العصر الحديث المتوج بالكرة الذهبية وأفضل لاعب بالعالم 2006", "فابيو كانافارو", listOf("أليساندرو نستا", "جورجيو كيلليني", "ماركو ماتيراتزي"))

        return list
    }

    private fun generateClubQuestionsPool(): List<ClubGuessQuestion> {
        val list = mutableListOf<ClubGuessQuestion>()

        fun add(
            diff: ChallengeDifficulty,
            name: String,
            country: String,
            league: String,
            stadium: String,
            titles: String,
            legend: String,
            nickname: String,
            badge: String,
            pCol: Long,
            sCol: Long,
            opts: List<String>
        ) {
            val fullOpts = (opts + name).distinct().shuffled()
            list.add(
                ClubGuessQuestion(
                    id = list.size + 1,
                    difficulty = diff,
                    clubName = name,
                    country = country,
                    league = league,
                    stadium = stadium,
                    titlesCount = titles,
                    famousLegend = legend,
                    nickname = nickname,
                    badgeSymbol = badge,
                    primaryColorHex = pCol,
                    secondaryColorHex = sCol,
                    correctAnswer = name,
                    options = fullOpts
                )
            )
        }

        // 1 - 30 Popular clubs with rich visual badges
        add(ChallengeDifficulty.EASY, "ريال مدريد", "إسبانيا 🇪🇸", "الدوري الإسباني", "سانتياغو برنابيو", "15 دوري أبطال أوروبا و36 دوري إسباني", "كريستيانو رونالدو، دي ستيفانو، زيدان", "الميرينغي / النادي الملكي", "real_madrid", 0xFFFFFFFF, 0xFFFFD700, listOf("برشلونة", "أتلتيكو مدريد", "إشبيلية"))
        add(ChallengeDifficulty.EASY, "برشلونة", "إسبانيا 🇪🇸", "الدوري الإسباني", "سبوتيفاي كامب نو", "5 دوري أبطال أوروبا و27 دوري إسباني", "ليونيل ميسي، كرويف، إنييستا", "البلوغرانا / فخر كتالونيا", "barcelona", 0xFF004D98, 0xFFA50044, listOf("ريال مدريد", "فالنسيا", "أتلتيك بيلباو"))
        add(ChallengeDifficulty.EASY, "الأهلي المصري", "مصر 🇪🇬", "الدوري المصري", "ستاد القاهرة الدولي", "12 دوري أبطال إفريقيا و44 دوري مصري", "محمود الخطيب، أبو تريكة، حسام حسن", "نادي القرن الإفريقي / الشياطين الحمر", "ahly", 0xFFD32F2F, 0xFFFFD700, listOf("الزمالك", "بيراميدز", "الإسماعيلي"))
        add(ChallengeDifficulty.EASY, "الزمالك", "مصر 🇪🇬", "الدوري المصري", "ستاد القاهرة الدولي", "5 دوري أبطال إفريقيا و14 دوري مصري", "حسن شحاتة، حازم إمام، شيكابالا", "الفارس الأبيض / مدرسة الفن والهندسة", "zamalek", 0xFFFFFFFF, 0xFFE53935, listOf("الأهلي المصري", "الاتحاد السكندري", "المصري البورسعيدي"))
        add(ChallengeDifficulty.EASY, "ليفربول", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "الدوري الإنجليزي", "أنفيلد رود", "6 دوري أبطال أوروبا و19 دوري إنجليزي", "ستيفن جيرارد، محمد صلاح، كيني دالغليش", "الريدز / لن تسير وحدك أبداً", "liverpool", 0xFFC8102E, 0xFF00B2A9, listOf("مانشستر سيتي", "تشيلسي", "أرسنال"))
        add(ChallengeDifficulty.EASY, "مانشستر سيتي", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "الدوري الإنجليزي", "استاد الاتحاد", "1 دوري أبطال أوروبا و10 دوري إنجليزي", "سيرجيو أغويرو، كيفين دي بروين، كومباني", "السيتيزنز / أصحاب القمصان السماوية", "man_city", 0xFF6CABDD, 0xFF1C2C5B, listOf("مانشستر يونايتد", "توتنهام", "نيوكاسل"))
        add(ChallengeDifficulty.EASY, "بايرن ميونخ", "ألمانيا 🇩🇪", "الدوري الألماني", "أليانز أرينا", "6 دوري أبطال أوروبا و33 دوري ألماني", "فرانز بكنباور، غيرد مولر، توماس مولر", "البافاري / إف سي هوليوود", "bayern", 0xFFDC052D, 0xFF0066B2, listOf("بوروسيا دورتموند", "باير ليفركوزن", "لايبزيغ"))
        add(ChallengeDifficulty.EASY, "باريس سان جيرمان", "فرنسا 🇫🇷", "الدوري الفرنسي", "بارك دي برانس", "12 دوري فرنسي ونهائي الأبطال 2020", "كيليان مبابي، إبراهيموفيتش، راي", "البي إس جي / أمراء العاصمة الفرنسية", "psg", 0xFF004170, 0xFFDA291C, listOf("مارسيليا", "ليون", "موناكو"))
        add(ChallengeDifficulty.EASY, "يوفنتوس", "إيطاليا 🇮🇹", "الدوري الإيطالي", "أليانز ستاديوم تورينو", "2 دوري أبطال أوروبا و36 دوري إيطالي", "أليساندرو ديل بييرو، بوفون، بلاتيني", "السيدة العجوز / البيانكونيري", "juventus", 0xFF000000, 0xFFFFFFFF, listOf("إنتر ميلان", "إيه سي ميلان", "روما"))
        add(ChallengeDifficulty.EASY, "أرسنال", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "الدوري الإنجليزي", "استاد الإمارات", "13 دوري إنجليزي ودوري اللاهزيمة 2004", "تييري هنري، دينيس بيركامب، فييرا", "الغانرز / المدفعجية", "arsenal", 0xFFDB0007, 0xFF023474, listOf("تشيلسي", "توتنهام", "ليفربول"))
        add(ChallengeDifficulty.EASY, "الهلال السعودي", "السعودية 🇸🇦", "الدوري السعودي", "المملكة أرينا", "4 دوري أبطال آسيا و19 دوري سعودي", "يوسف الثنيان، سامي الجابر، محمد الدعيع", "الزعيم العالمي / الموج الأزرق", "hilal", 0xFF0038A8, 0xFFFFFFFF, listOf("النصر السعودي", "الاتحاد السعودي", "الأهلي السعودي"))
        add(ChallengeDifficulty.EASY, "النصر السعودي", "السعودية 🇸🇦", "الدوري السعودي", "الأول بارك", "9 دوري سعودي وبطولة الأندية العربية 2023", "ماجد عبد الله، كريستيانو رونالدو، محيسن الجمعان", "العالمي / فارس نجد", "nassr", 0xFFFFD100, 0xFF002D72, listOf("الهلال السعودي", "الشباب", "الاتفاق"))
        add(ChallengeDifficulty.MEDIUM, "إنتر ميلان", "إيطاليا 🇮🇹", "الدوري الإيطالي", "جوسيبي مياتزا (سان سيرو)", "3 دوري أبطال أوروبا وثلاثية 2010 التاريخية و20 دوري إيطالي", "خافيير زانيتي، رونالدو الظاهرة، مازولا", "النيراتزوري / الأفاعي", "inter", 0xFF001EA0, 0xFF000000, listOf("إيه سي ميلان", "يوفنتوس", "لاتسيو"))
        add(ChallengeDifficulty.MEDIUM, "إيه سي ميلان", "إيطاليا 🇮🇹", "الدوري الإيطالي", "سان سيرو", "7 دوري أبطال أوروبا و19 دوري إيطالي", "باولو مالديني، فرانكو باريزي، فان باستن", "الروسونيري / ديافولو الشياطين", "milan", 0xFFFB090B, 0xFF000000, listOf("إنتر ميلان", "روما", "فيورنتينا"))
        add(ChallengeDifficulty.MEDIUM, "تشيلسي", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "الدوري الإنجليزي", "ستامفورد بريدج", "2 دوري أبطال أوروبا و6 دوري إنجليزي", "ديدييه دروغبا، فرانك لامبارد، جون تيري", "البلوز / فخر لندن", "chelsea", 0xFF034694, 0xFFFFFFFF, listOf("أرسنال", "فولهام", "وست هام"))
        add(ChallengeDifficulty.MEDIUM, "بوروسيا دورتموند", "ألمانيا 🇩🇪", "الدوري الألماني", "سيغنال إيدونا بارك (فيستفالن)", "1 دوري أبطال أوروبا و8 دوري ألماني", "ماتياس زامر، رويس، ليفاندوفسكي", "أسود الفيستفاليا / الجدار الأصفر", "dortmund", 0xFFFDE100, 0xFF000000, listOf("بايرن ميونخ", "شالكه", "فرانكفورت"))
        add(ChallengeDifficulty.MEDIUM, "أتلتيكو مدريد", "إسبانيا 🇪🇸", "الدوري الإسباني", "سيفيتاس ميتروبوليتانو", "11 دوري إسباني و3 دوري أوروبي", "لويس أراغونيس، دييغو فورلان، غودين", "الروخي بلانكوس / صناع الوسائد", "atletico", 0xFFCB3524, 0xFF272E61, listOf("ريال مدريد", "خيتافي", "رايو فايكانو"))
        add(ChallengeDifficulty.MEDIUM, "الاتحاد السعودي", "السعودية 🇸🇦", "الدوري السعودي", "مدينة الملك عبد الله الرياضية (الجوهرة)", "2 دوري أبطال آسيا متتالية و9 دوري سعودي", "محمد نور، حمزة إدريس، أحمد جميل", "العميد / النمور الصفراء", "ittihad", 0xFFFFD100, 0xFF000000, listOf("الأهلي السعودي", "الوحدة", "الفتح"))
        add(ChallengeDifficulty.MEDIUM, "بيراميدز", "مصر 🇪🇬", "الدوري المصري", "استاد الدفاع الجوي", "بطل كأس مصر 2024 ووصيف الدوري المصري", "عبد الله السعيد، إبراهيم عادل، رمضان صبحي", "السماوي / الأهرامات", "pyramids", 0xFF0F1E36, 0xFF29B6F6, listOf("سيراميكا كليوباترا", "المودرن سبورت", "سموحة"))
        add(ChallengeDifficulty.MEDIUM, "مانشستر يونايتد", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "الدوري الإنجليزي", "أولد ترافورد (مسرح الأحلام)", "3 دوري أبطال أوروبا و20 دوري إنجليزي", "سير بوبي تشارلتون، جورج بست، واين روني", "الشياطين الحمر", "man_united", 0xFFC70101, 0xFFFFD100, listOf("ليفربول", "مانشستر سيتي", "ليدز يونايتد"))
        add(ChallengeDifficulty.MEDIUM, "توتنهام هوتسبير", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "الدوري الإنجليزي", "استاد توتنهام هوتسبير", "2 دوري إنجليزي ونهائي أبطال أوروبا 2019", "هاري كين، غلين هودل، جيمي غريفز", "السبيرز / الديوك", "tottenham", 0xFF132257, 0xFFFFFFFF, listOf("أرسنال", "تشيلسي", "كريستال بالاس"))
        add(ChallengeDifficulty.MEDIUM, "روما", "إيطاليا 🇮🇹", "الدوري الإيطالي", "الأولمبيكو", "3 دوري إيطالي ودوري المؤتمر الأوروبي 2022", "فرانشيسكو توتي، دانييلي دي روسي، فالكاو", "الذئاب / الجيلاروسي", "roma", 0xFF8E1F2F, 0xFFF0BC42, listOf("لاتسيو", "نابولي", "فيورنتينا"))
        add(ChallengeDifficulty.MEDIUM, "نابولي", "إيطاليا 🇮🇹", "الدوري الإيطالي", "دييغو أرماندو مارادونا", "3 دوري إيطالي وكأس الاتحاد الأوروبي 1989", "دييغو مارادونا، كاريكا، كفاراتسخيليا", "البارتينوبي / أبناء الجنوب الإيطالي", "napoli", 0xFF12A0D7, 0xFF003B6F, listOf("يوفنتوس", "ساليرنيتانا", "باليرمو"))
        add(ChallengeDifficulty.MEDIUM, "إف سي بورتو", "البرتغال 🇵🇹", "الدوري البرتغالي", "ملعب الدراجاو (التنين)", "2 دوري أبطال أوروبا و30 دوري برتغالي", "ديكو، باولو فوتري، فيتور بايا", "التنانين الزرقاء", "porto", 0xFF0038A8, 0xFFFFFFFF, listOf("بنفيكا", "سبورتينغ لشبونة", "براغا"))
        add(ChallengeDifficulty.MEDIUM, "بنفيكا", "البرتغال 🇵🇹", "الدوري البرتغالي", "ملعب دا لوز (النور)", "2 دوري أبطال أوروبا و38 دوري برتغالي", "أوزيبيو الفهد الأسمر، كولونا، روي كوستا", "النسور الحمر", "benfica", 0xFFE53935, 0xFFFFFFFF, listOf("بورتو", "سبورتينغ لشبونة", "فيتوريا غيماريش"))
        add(ChallengeDifficulty.MEDIUM, "سبورتينغ لشبونة", "البرتغال 🇵🇹", "الدوري البرتغالي", "خوسيه ألفالادي", "20 دوري برتغالي ومصنع كرات ذهبية", "كريستيانو رونالدو، لويس فيغو، ناني", "الأسود الخضراء", "sporting", 0xFF006633, 0xFFFFD700, listOf("بنفيكا", "بورتو", "بوافيستا"))
        add(ChallengeDifficulty.MEDIUM, "إنتر ميامي", "الولايات المتحدة 🇺🇸", "الدوري الأمريكي", "تشيس ستاديوم", "بطل درع الجماهير 2024 وكأس الدوريات 2023", "ليونيل ميسي، لويس سواريز، بوسكيتس", "طيور النحام الوردية / هيرونز", "inter_miami", 0xFFF7B5CD, 0xFF000000, listOf("لوس أنجلوس إف سي", "نيويورك سيتي", "أتلانتا يونايتد"))
        add(ChallengeDifficulty.HARD, "غلطة سراي", "تركيا 🇹🇷", "الدوري التركي", "رامس بارك (علي سامي ين)", "24 دوري تركي وكأس الاتحاد الأوروبي 2000", "هاكان شوكور، تافاريل، جورجي هاجي", "الأسود / سيمبوم", "galatasaray", 0xFFA32638, 0xFFFFA300, listOf("فنربخشة", "بشكتاش", "طرابزون سبور"))
        add(ChallengeDifficulty.HARD, "فنربخشة", "تركيا 🇹🇷", "الدوري التركي", "شكري سراج أوغلو", "19 دوري تركي", "أليكس دي سوزا، إيمري بيلوز أوغلو، تونجاي", "الكناري الأصفر", "fenerbahce", 0xFF002D72, 0xFFFFEB3B, listOf("غلطة سراي", "بشكتاش", "إسطنبول باشاك شهير"))
        add(ChallengeDifficulty.HARD, "بشكتاش", "تركيا 🇹🇷", "الدوري التركي", "توبراش ستاديوم (فودافون بارك)", "16 دوري تركي و11 كأس تركيا", "سليمان سيبا، إلهان مانسيز، ريكاردو كواريزما", "النسور السوداء", "besiktas", 0xFF000000, 0xFFFFFFFF, listOf("غلطة سراي", "فنربخشة", "قاسم باشا"))

        return list
    }

    private fun generateWorldCupQuestionsPool(): List<WorldCupQuestion> {
        val list = mutableListOf<WorldCupQuestion>()

        fun add(
            diff: ChallengeDifficulty,
            year: String,
            q: String,
            hint: String,
            correct: String,
            opts: List<String>,
            fact: String
        ) {
            val fullOpts = (opts + correct).distinct().shuffled()
            list.add(
                WorldCupQuestion(
                    id = list.size + 1,
                    difficulty = diff,
                    edition = year,
                    question = q,
                    hint = hint,
                    correctAnswer = correct,
                    options = fullOpts,
                    triviaFact = fact
                )
            )
        }

        // Qatar 2022
        add(ChallengeDifficulty.EASY, "قطر 2022", "من هو المنتخب المتوج بلقب كأس العالم 2022 في قطر بعد ملحمة استاد لوسيل؟", "قاده الأسطورة ليونيل ميسي للفوز بركلات الترجيح", "الأرجنتين 🇦🇷", listOf("فرنسا 🇫🇷", "كرواتيا 🇭🇷", "المغرب 🇲🇦"), "فازت الأرجنتين بركلات الترجيح 4-2 بعد التعادل المثير 3-3.")
        add(ChallengeDifficulty.EASY, "قطر 2022", "من هو أول منتخب عربي وإفريقي في التاريخ يصل إلى المربع الذهبي (نصف النهائي) في المونديال؟", "أسود الأطلس بقيادة المدرب وليد الركراكي", "المغرب 🇲🇦", listOf("السنغال 🇸🇳", "الجزائر 🇩🇿", "مصر 🇪🇬"), "حقق المغرب إنجازاً تاريخياً بإقصاء إسبانيا والبرتغال والحلول بالمركز الرابع.")
        add(ChallengeDifficulty.EASY, "قطر 2022", "من فاز بجائزة هداف كأس العالم (الحذاء الذهبي) 2022 بعد تسجيله 8 أهداف وهاتريك في النهائي؟", "الصاروخ الفرنسي ونجم باريس سان جيرمان وقتها", "كيليان مبابي", listOf("ليونيل ميسي", "أوليفييه جيرو", "جوليان ألفاريز"), "أول هاتريك في نهائي المونديال منذ جيوف هيرست في 1966.")
        add(ChallengeDifficulty.EASY, "قطر 2022", "أي منتخب عربي فجر واحدة من أكبر مفاجآت التاريخ بفوزه على الأرجنتين 2-1 في دور المجموعات؟", "الصقور الخضر بهدفي صالح الشهري وسالم الدوسري", "السعودية 🇸🇦", listOf("تونس 🇹🇳", "المغرب 🇲🇦", "قطر 🇶🇦"), "الخسارة الوحيدة للأرجنتين في مشوار التتويج بلقب كأس العالم 2022.")

        // Russia 2018
        add(ChallengeDifficulty.EASY, "روسيا 2018", "من فاز بلقب كأس العالم 2018 في روسيا بعد فوزه على كرواتيا 4-2 في النهائي؟", "منتخب الديوك بقيادة المدرب ديدييه ديشامب", "فرنسا 🇫🇷", listOf("كرواتيا 🇭🇷", "بلجيكا 🇧🇪", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿"), "اللقب الثاني لفرنسا بعد مونديال 1998 على أرضها.")
        add(ChallengeDifficulty.MEDIUM, "روسيا 2018", "من فاز بجائزة أفضل لاعب (الكرة الذهبية) في مونديال روسيا 2018؟", "مايسترو خط وسط ريال مدريد وقائد كرواتيا", "لوكا مودريتش", listOf("أنطوان غريزمان", "إيدين هازارد", "هاري كين"), "قاد كرواتيا لأول نهائي مونديالي في تاريخها ونال الكرة الذهبية العالمية بعدها.")

        // Brazil 2014
        add(ChallengeDifficulty.EASY, "البرازيل 2014", "ما هي النتيجة الصادمة والتاريخية في نصف نهائي مونديال 2014 بين ألمانيا والبرازيل؟", "أكبر هزيمة للسامبا في تاريخ المونديال على أرضهم", "7 - 1 لألمانيا", listOf("5 - 0 لألمانيا", "4 - 1 لألمانيا", "3 - 0 لألمانيا"), "سجلت ألمانيا 5 أهداف في أول 29 دقيقة فقط في ملعب مينيراو.")
        add(ChallengeDifficulty.MEDIUM, "البرازيل 2014", "من سجل هدف تتويج ألمانيا بلقب كأس العالم 2014 في مرمى الأرجنتين في الدقيقة 113؟", "البديل الذهبي ليوكيم لوف", "ماريو غوتزه", listOf("توماس مولر", "ميروسلاف كلوزه", "مسعود أوزيل"), "أول منتخب أوروبي يتوج بكأس العالم على أراضي قارة أمريكا الجنوبية.")

        // South Africa 2010
        add(ChallengeDifficulty.EASY, "جنوب إفريقيا 2010", "من سجل الهدف القاتل في الدقيقة 116 ليمنح إسبانيا لقبها المونديالي الأول في 2010 ضد هولندا؟", "الرسام ونجم خط وسط برشلونة", "أندريس إنييستا", listOf("دافيد فيا", "تشافي هيرنانديز", "فيرناندو توريس"), "أهدى إنييستا الهدف لصديقه الراحل داني خاركي.")
        add(ChallengeDifficulty.MEDIUM, "جنوب إفريقيا 2010", "من هو الأخطبوط الشهير الذي توقع نتائج مباريات مونديال 2010 بنجاح تام؟", "كان يعيش في حوض أحياء مائية بمدينة أوبرهاوزن الألمانية", "الأخطبوط بول", listOf("الأخطبوط جورج", "السلحفاة كاباو", "الببغاء ماني"), "توقع الفائز في جميع مباريات ألمانيا السبع والنهائي الإسباني بنسبة 100%.")

        // Germany 2006
        add(ChallengeDifficulty.MEDIUM, "ألمانيا 2006", "ما هي الواقعة الأكثر إثارة للجدل في نهائي مونديال 2006 بين فرنسا وإيطاليا؟", "نطحة زيدان الشهيرة لمدافع إيطاليا", "نطحة زيدان لماركو ماتيراتزي", listOf("إلغاء هدف تريزيغيه", "طرد بوفون بالبطاقة الحمراء", "اشتباك كانافارو وهنري"), "نال زيدان البطاقة الحمراء في آخر مباراة بمسيرته الكروية وفازت إيطاليا بالركلات الترجيحية.")
        add(ChallengeDifficulty.EASY, "ألمانيا 2006", "من هو المنتخب المتوج بمونديال 2006 ليحصد نجمته الرابعة في التاريخ؟", "الآتزوري الإيطالي بقيادة مارتشيلو ليبي والقائد كانافارو", "إيطاليا 🇮🇹", listOf("فرنسا 🇫🇷", "ألمانيا 🇩🇪", "البرتغال 🇵🇹"), "لم تستقبل شباك بوفون سوى هدفين طوال البطولة أحدهما هدف عكسي والآخر ركلة جزاء.")

        // Korea & Japan 2002
        add(ChallengeDifficulty.EASY, "كوريا واليابان 2002", "من سجل هدفي نهائي مونديال 2002 ليمنح البرازيل لقبها الخامس (البنتكامبيون) ضد ألمانيا؟", "الظاهرة ذو قصة الشعر الشهيرة بعد عودته من إصابة الرباط الصليبي", "رونالدو نازاريو", listOf("ريفالدو", "رونالدينيو", "روبرتو كارلوس"), "حصد رونالدو الحذاء الذهبي بـ 8 أهداف وثأر لخسارة نهائي 1998.")
        add(ChallengeDifficulty.HARD, "كوريا واليابان 2002", "أي منتخب آسيوي حقق إنجازاً تاريخياً بالوصول إلى نصف نهائي مونديال 2002 كأول منتخب في القارة؟", "شارك في استضافة البطولة مع اليابان تحت قيادة غوس هيدينك", "كوريا الجنوبية 🇰🇷", listOf("اليابان 🇯🇵", "السعودية 🇸🇦", "الصين 🇨🇳"), "أطاحت كوريا الجنوبية بكل من إيطاليا وإسبانيا في أدوار خروج المغلوب.")

        // France 1998
        add(ChallengeDifficulty.EASY, "فرنسا 1998", "كم كانت نتيجة نهائي كأس العالم 1998 بين فرنسا والبرازيل في ستاد دي فرانس؟", "شهدت ثنائية بالرأس من زين الدين زيدان وهدف إيمانويل بيتي", "3 - 0 لفرنسا", listOf("2 - 1 لفرنسا", "1 - 0 لفرنسا", "4 - 1 لفرنسا"), "أول لقب مونديالي لفرنسا في التاريخ وأكبر خسارة للبرازيل بنهائي المونديال.")

        // USA 1994
        add(ChallengeDifficulty.MEDIUM, "أمريكا 1994", "من أهدر ركلة الترجيح الشهيرة في نهائي 1994 فوق عارضة البرازيل ليطلق صافرة تتويج السامبا؟", "ذيل الحصان الإلهي ونجم يوفنتوس وأفضل لاعب بالعالم وقتها", "روبرتو باجيو", listOf("فرانكو باريزي", "دانييلي ماسارو", "باولو مالديني"), "أول نهائي في تاريخ المونديال ينتهي سلبياً ويحسم بركلات الترجيح.")
        add(ChallengeDifficulty.HARD, "أمريكا 1994", "من سجل 5 أهداف كاملة في مباراة واحدة في مونديال 1994 ضد الكاميرون في رقم قياسي صامد؟", "مهاجم المنتخب الروسي الذي نال الحذاء الذهبي مناصفة مع ستويتشكوف", "أوليغ سالينكو", listOf("خريستو ستويتشكوف", "يورغن كلينسمان", "روماريو"), "اللاعب الوحيد في تاريخ المونديال الذي يسجل 5 أهداف في لقاء واحد.")

        // Italy 1990
        add(ChallengeDifficulty.HARD, "إيطاليا 1990", "من فاز بلقب كأس العالم 1990 في إيطاليا بهدف أندرياس بريمه من ركلة جزاء؟", "المانشافت بقيادة القيصر فرانز بكنباور كمدرب", "ألمانيا الغربية 🇩🇪", listOf("الأرجنتين 🇦🇷", "إيطاليا 🇮🇹", "إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿"), "أصبح بكنباور ثاني شخص في التاريخ يفوز بالمونديال كلاعب وقائد ومدرب بعد ماريو زاغالو.")
        add(ChallengeDifficulty.MEDIUM, "إيطاليا 1990", "أي منتخب عربي سجل مجدي عبد الغني هدفه الشهير في مرماه في مونديال 1990؟", "أبطال أوروبا وقتها مع فان باستن ورود خوليت وريكارد", "هولندا 🇳🇱", listOf("إنجلترا 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "إيرلندا 🇮🇪", "بلجيكا 🇧🇪"), "انتهت المباراة بالتعادل التاريخي 1-1 من ركلة جزاء سددها مجدي عبد الغني.")

        // Mexico 1986
        add(ChallengeDifficulty.EASY, "المكسيك 1986", "من سجل الهدف الأسطوري بمراوغة 5 لاعبين وحارس إنجلترا والمسمى بـ (هدف القرن)؟", "أسطورة الأرجنتين رقم 10 في ربع نهائي مكسيكو 1986", "دييغو مارادونا", listOf("خورخي فالدانو", "خورخي بوروتشاغا", "غاري لينيكر"), "سجل مارادونا في نفس المباراة هدفه المثير للجدل (يد الرب).")

        // Historic Records Across All Cups
        add(ChallengeDifficulty.EASY, "تاريخ المونديال", "من هو الهداف التاريخي لبطولات كأس العالم على مر التاريخ برصيد 16 هدفاً؟", "القناص الألماني الشهير برأسه وقفزاته البهلوانية", "ميروسلاف كلوزه", listOf("رونالدو الظاهرة (15)", "غيرد مولر (14)", "بيليه (12)"), "كسر كلوزه الرقم في شباك البرازيل بنصف نهائي 2014.")
        add(ChallengeDifficulty.HARD, "تاريخ المونديال", "من هو اللاعب الوحيد في التاريخ الذي سجل 13 هدفاً في نسخة واحدة من المونديال (السويد 1958)؟", "المهاجم الفرنسي الأسطوري", "جوست فونتين", listOf("بيليه", "ساندور كوتشيس", "أوزيبيو"), "رقم قياسي تاريخي يستحيل تحطيمه بعد تسجيله 13 هدفاً في 6 مباريات فقط.")
        add(ChallengeDifficulty.EASY, "تاريخ المونديال", "من هو اللاعب الوحيد في تاريخ كرة القدم الذي توج بكأس العالم 3 مرات كلاعب؟", "الجوهرة السوداء لمنتخب السامبا (1958, 1962, 1970)", "بيليه 🇧🇷", listOf("كافو", "غارينشيا", "زاغالو"), "توج بيليه بعمر 17 عاماً في 1958 ثم 1962 و1970.")
        add(ChallengeDifficulty.MEDIUM, "أوروغواي 1930", "أين أقيمت أول بطولة كأس عالم في التاريخ عام 1930 ومن كان أول بطل متوج بها؟", "استضافتها على استاد سنتيناريو في مونتيفيديو", "أوروغواي 🇺🇾", listOf("الأرجنتين 🇦🇷", "البرازيل 🇧🇷", "إيطاليا 🇮🇹"), "تغلبت الأوروغواي في النهائي على جارتها الأرجنتين بنتيجة 4-2.")
        add(ChallengeDifficulty.HARD, "البرازيل 1950", "ما هو الاسم التاريخي للمباراة الفاصلة في مونديال 1950 التي صعقت فيها أوروغواي البرازيل 2-1 في الماراكانا؟", "أمام أكثر من 199 ألف مشجع صدموا بالصمت والدموع", "الماراكانازو (Maracanaço)", listOf("نهائي الصدمة", "ملحمة ريو", "صدمة البرازيل", "موقعة السامبا"), "كانت البرازيل تحتاج نقطة واحدة فقط للقب لكن أوروغواي فازت بهدف غيغيا الصاعق.")

        return list
    }

    private fun generateSquadPlayersPool(): List<SquadPlayer> {
        val list = mutableListOf<SquadPlayer>()

        fun add(id: String, name: String, pos: String, rat: Int, price: Double, nat: String, club: String, league: String) {
            list.add(SquadPlayer(id, name, pos, rat, price, nat, club, league))
        }

        // ========================================================
        // 1. الدوري الإنجليزي (Premier League)
        // ========================================================
        add("epl_gk_1", "دافيد رايا", "GK", 86, 16.0, "إسبانيا", "أرسنال", "الدوري الإنجليزي")
        add("epl_gk_2", "أليسون بيكر", "GK", 89, 22.0, "البرازيل", "ليفربول", "الدوري الإنجليزي")
        add("epl_def_1", "فيرجيل فان دايك", "DEF", 90, 26.0, "هولندا", "ليفربول", "الدوري الإنجليزي")
        add("epl_def_2", "ويليام ساليبا", "DEF", 88, 24.0, "فرنسا", "أرسنال", "الدوري الإنجليزي")
        add("epl_def_3", "ترينت ألكسندر أرنولد", "DEF", 87, 22.0, "إنجلترا", "ليفربول", "الدوري الإنجليزي")
        add("epl_def_4", "جفارديول", "DEF", 85, 18.0, "كرواتيا", "مانشستر سيتي", "الدوري الإنجليزي")
        add("epl_mid_1", "كيفين دي بروين", "MID", 90, 28.0, "بلجيكا", "مانشستر سيتي", "الدوري الإنجليزي")
        add("epl_mid_2", "رودري", "MID", 91, 30.0, "إسبانيا", "مانشستر سيتي", "الدوري الإنجليزي")
        add("epl_mid_3", "مارتن أوديغارد", "MID", 88, 24.0, "النرويج", "أرسنال", "الدوري الإنجليزي")
        add("epl_mid_4", "برونو فيرنانديز", "MID", 87, 20.0, "البرتغال", "مانشستر يونايتد", "الدوري الإنجليزي")
        add("epl_fwd_1", "محمد صلاح", "FWD", 90, 28.0, "مصر", "ليفربول", "الدوري الإنجليزي")
        add("epl_fwd_2", "إيرلينغ هالاند", "FWD", 91, 35.0, "النرويج", "مانشستر سيتي", "الدوري الإنجليزي")
        add("epl_fwd_3", "بوكايو ساكا", "FWD", 88, 25.0, "إنجلترا", "أرسنال", "الدوري الإنجليزي")
        add("epl_fwd_4", "سون هيونغ مين", "FWD", 87, 19.0, "كوريا الجنوبية", "توتنهام", "الدوري الإنجليزي")

        // ========================================================
        // 2. الدوري الإسباني (La Liga)
        // ========================================================
        add("laliga_gk_1", "تيبو كورتوا", "GK", 89, 21.0, "بلجيكا", "ريال مدريد", "الدوري الإسباني")
        add("laliga_gk_2", "يان أوبلاك", "GK", 87, 17.0, "سلوفينيا", "أتلتيكو مدريد", "الدوري الإسباني")
        add("laliga_def_1", "أنطونيو روديغر", "DEF", 88, 21.0, "ألمانيا", "ريال مدريد", "الدوري الإسباني")
        add("laliga_def_2", "جول كوندي", "DEF", 86, 18.0, "فرنسا", "برشلونة", "الدوري الإسباني")
        add("laliga_def_3", "داني كارفاخال", "DEF", 86, 17.0, "إسبانيا", "ريال مدريد", "الدوري الإسباني")
        add("laliga_def_4", "أليخاندرو بالدي", "DEF", 83, 13.0, "إسبانيا", "برشلونة", "الدوري الإسباني")
        add("laliga_mid_1", "جود بيلينغهام", "MID", 90, 32.0, "إنجلترا", "ريال مدريد", "الدوري الإسباني")
        add("laliga_mid_2", "بيدري", "MID", 87, 22.0, "إسبانيا", "برشلونة", "الدوري الإسباني")
        add("laliga_mid_3", "فيديريكو فالفيردي", "MID", 88, 25.0, "الأوروغواي", "ريال مدريد", "الدوري الإسباني")
        add("laliga_mid_4", "لوكا مودريتش", "MID", 85, 12.0, "كرواتيا", "ريال مدريد", "الدوري الإسباني")
        add("laliga_fwd_1", "فينيسيوس جونيور", "FWD", 90, 34.0, "البرازيل", "ريال مدريد", "الدوري الإسباني")
        add("laliga_fwd_2", "كيليان مبابي", "FWD", 91, 35.0, "فرنسا", "ريال مدريد", "الدوري الإسباني")
        add("laliga_fwd_3", "لامين يامال", "FWD", 87, 26.0, "إسبانيا", "برشلونة", "الدوري الإسباني")
        add("laliga_fwd_4", "روبرت ليفاندوفسكي", "FWD", 88, 20.0, "بولندا", "برشلونة", "الدوري الإسباني")

        // ========================================================
        // 3. الدوري الإيطالي (Serie A)
        // ========================================================
        add("seriea_gk_1", "يان سومر", "GK", 86, 12.0, "سويسرا", "إنتر ميلان", "الدوري الإيطالي")
        add("seriea_gk_2", "مايك ماينان", "GK", 87, 18.0, "فرنسا", "إيه سي ميلان", "الدوري الإيطالي")
        add("seriea_def_1", "أليساندرو باستوني", "DEF", 87, 20.0, "إيطاليا", "إنتر ميلان", "الدوري الإيطالي")
        add("seriea_def_2", "تيو هيرنانديز", "DEF", 87, 22.0, "فرنسا", "إيه سي ميلان", "الدوري الإيطالي")
        add("seriea_def_3", "فيديريكو ديماركو", "DEF", 85, 17.0, "إيطاليا", "إنتر ميلان", "الدوري الإيطالي")
        add("seriea_def_4", "بريمر", "DEF", 86, 18.0, "البرازيل", "يوفنتوس", "الدوري الإيطالي")
        add("seriea_mid_1", "نيكولو باريلا", "MID", 87, 23.0, "إيطاليا", "إنتر ميلان", "الدوري الإيطالي")
        add("seriea_mid_2", "هاكان كالهانوغلو", "MID", 86, 18.0, "تركيا", "إنتر ميلان", "الدوري الإيطالي")
        add("seriea_mid_3", "تون دي كوفمينيرس", "MID", 84, 15.0, "هولندا", "يوفنتوس", "الدوري الإيطالي")
        add("seriea_fwd_1", "لاوتارو مارتينيز", "FWD", 89, 27.0, "الأرجنتين", "إنتر ميلان", "الدوري الإيطالي")
        add("seriea_fwd_2", "رافائيل لياو", "FWD", 86, 22.0, "البرتغال", "إيه سي ميلان", "الدوري الإيطالي")
        add("seriea_fwd_3", "خفيتشا كفاراتسخيليا", "FWD", 86, 21.0, "جورجيا", "نابولي", "الدوري الإيطالي")

        // ========================================================
        // 4. الدوري الألماني (Bundesliga)
        // ========================================================
        add("bundes_gk_1", "مانويل نوير", "GK", 87, 15.0, "ألمانيا", "بايرن ميونخ", "الدوري الألماني")
        add("bundes_gk_2", "لوكاس هراديكي", "GK", 84, 10.0, "فنلندا", "باير ليفركوزن", "الدوري الألماني")
        add("bundes_def_1", "جوناثان تاه", "DEF", 86, 16.0, "ألمانيا", "باير ليفركوزن", "الدوري الألماني")
        add("bundes_def_2", "جيريمي فريمبونغ", "DEF", 86, 19.0, "هولندا", "باير ليفركوزن", "الدوري الألماني")
        add("bundes_def_3", "ألفونسو ديفيز", "DEF", 85, 18.0, "كندا", "بايرن ميونخ", "الدوري الألماني")
        add("bundes_mid_1", "فلوريان فيرتز", "MID", 89, 28.0, "ألمانيا", "باير ليفركوزن", "الدوري الألماني")
        add("bundes_mid_2", "جمال موسيالا", "MID", 88, 28.0, "ألمانيا", "بايرن ميونخ", "الدوري الألماني")
        add("bundes_mid_3", "غرانيت تشاكا", "MID", 86, 16.0, "سويسرا", "باير ليفركوزن", "الدوري الألماني")
        add("bundes_fwd_1", "هاري كين", "FWD", 90, 28.0, "إنجلترا", "بايرن ميونخ", "الدوري الألماني")
        add("bundes_fwd_2", "عمر مرموش", "FWD", 85, 20.0, "مصر", "آينتراخت فرانكفورت", "الدوري الألماني")
        add("bundes_fwd_3", "سيرهو غيراسي", "FWD", 84, 16.0, "غينيا", "بوروسيا دورتموند", "الدوري الألماني")

        // ========================================================
        // 5. الدوري الفرنسي (Ligue 1)
        // ========================================================
        add("ligue1_gk_1", "جانلويجي دوناروما", "GK", 88, 20.0, "إيطاليا", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_def_1", "أشرف حكيمي", "DEF", 87, 24.0, "المغرب", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_def_2", "ماركينيوس", "DEF", 86, 18.0, "البرازيل", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_def_3", "نونو مينديز", "DEF", 84, 16.0, "البرتغال", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_mid_1", "فيتينيا", "MID", 86, 18.0, "البرتغال", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_mid_2", "وارن زاير إيمري", "MID", 83, 14.0, "فرنسا", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_mid_3", "ألكسندر غولوفين", "MID", 82, 12.0, "روسيا", "موناكو", "الدوري الفرنسي")
        add("ligue1_fwd_1", "عثمان ديمبيلي", "FWD", 86, 21.0, "فرنسا", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_fwd_2", "برادلي باركولا", "FWD", 84, 17.0, "فرنسا", "باريس سان جيرمان", "الدوري الفرنسي")
        add("ligue1_fwd_3", "مصطفى محمد", "FWD", 80, 11.0, "مصر", "نانت", "الدوري الفرنسي")

        // ========================================================
        // 6. الدوري السعودي (Saudi Pro League)
        // ========================================================
        add("spl_gk_1", "ياسين بونو", "GK", 87, 19.0, "المغرب", "الهلال", "الدوري السعودي")
        add("spl_gk_2", "إدوارد ميندي", "GK", 83, 12.0, "السنغال", "الأهلي", "الدوري السعودي")
        add("spl_def_1", "كاليدو كوليبالي", "DEF", 84, 15.0, "السنغال", "الهلال", "الدوري السعودي")
        add("spl_def_2", "إيمريك لابورت", "DEF", 84, 14.0, "إسبانيا", "النصر", "الدوري السعودي")
        add("spl_def_3", "سعود عبد الحميد", "DEF", 81, 10.0, "السعودية", "الهلال", "الدوري السعودي")
        add("spl_mid_1", "سيرجي ميلينكوفيتش سافيتش", "MID", 86, 20.0, "صربيا", "الهلال", "الدوري السعودي")
        add("spl_mid_2", "نغولو كانتي", "MID", 85, 17.0, "فرنسا", "الاتحاد", "الدوري السعودي")
        add("spl_mid_3", "روبن نيفيز", "MID", 84, 16.0, "البرتغال", "الهلال", "الدوري السعودي")
        add("spl_mid_4", "رياض محرز", "MID", 85, 20.0, "الجزائر", "الأهلي", "الدوري السعودي")
        add("spl_fwd_1", "كريستيانو رونالدو", "FWD", 88, 25.0, "البرتغال", "النصر", "الدوري السعودي")
        add("spl_fwd_2", "كريم بنزيما", "FWD", 86, 21.0, "فرنسا", "الاتحاد", "الدوري السعودي")
        add("spl_fwd_3", "سالم الدوسري", "FWD", 83, 14.0, "السعودية", "الهلال", "الدوري السعودي")
        add("spl_fwd_4", "ألكسندر ميتروفيتش", "FWD", 84, 18.0, "صربيا", "الهلال", "الدوري السعودي")

        // ========================================================
        // 7. الدوري المصري (Egyptian Premier League)
        // ========================================================
        add("eg_gk_1", "محمد الشناوي", "GK", 83, 12.0, "مصر", "الأهلي", "الدوري المصري")
        add("eg_gk_2", "محمد عواد", "GK", 80, 8.0, "مصر", "الزمالك", "الدوري المصري")
        add("eg_def_1", "محمد عبد المنعم", "DEF", 82, 12.0, "مصر", "الأهلي", "الدوري المصري")
        add("eg_def_2", "علي معلول", "DEF", 81, 10.0, "تونس", "الأهلي", "الدوري المصري")
        add("eg_def_3", "أحمد فتوح", "DEF", 80, 9.0, "مصر", "الزمالك", "الدوري المصري")
        add("eg_def_4", "عمر كمال عبد الواحد", "DEF", 79, 8.0, "مصر", "الأهلي", "الدوري المصري")
        add("eg_mid_1", "إمام عاشور", "MID", 83, 14.0, "مصر", "الأهلي", "الدوري المصري")
        add("eg_mid_2", "مروان عطية", "MID", 81, 10.0, "مصر", "الأهلي", "الدوري المصري")
        add("eg_mid_3", "عبد الله السعيد", "MID", 80, 8.0, "مصر", "الزمالك", "الدوري المصري")
        add("eg_fwd_1", "أحمد مصطفى زيزو", "FWD", 83, 15.0, "مصر", "الزمالك", "الدوري المصري")
        add("eg_fwd_2", "حسين الشحات", "FWD", 81, 11.0, "مصر", "الأهلي", "الدوري المصري")
        add("eg_fwd_3", "إبراهيم عادل", "FWD", 81, 12.0, "مصر", "بيراميدز", "الدوري المصري")
        add("eg_fwd_4", "وسام أبو علي", "FWD", 81, 11.0, "فلسطين", "الأهلي", "الدوري المصري")

        // ========================================================
        // 8. الدوري الأمريكي (MLS)
        // ========================================================
        add("mls_gk_1", "دريك كالندر", "GK", 79, 8.0, "الولايات المتحدة", "إنتر ميامي", "الدوري الأمريكي")
        add("mls_def_1", "جوردي ألبا", "DEF", 83, 12.0, "إسبانيا", "إنتر ميامي", "الدوري الأمريكي")
        add("mls_def_2", "سيرجي بالنسيا", "DEF", 78, 7.0, "إسبانيا", "لوس أنجلوس إف سي", "الدوري الأمريكي")
        add("mls_mid_1", "سيرجيو بوسكيتس", "MID", 84, 14.0, "إسبانيا", "إنتر ميامي", "الدوري الأمريكي")
        add("mls_mid_2", "ريكي بويج", "MID", 82, 11.0, "إسبانيا", "لوس أنجلوس غالاكسي", "الدوري الأمريكي")
        add("mls_mid_3", "لوتشيانو أكوستا", "MID", 81, 10.0, "الأرجنتين", "إف سي سينسيناتي", "الدوري الأمريكي")
        add("mls_fwd_1", "ليونيل ميسي", "FWD", 90, 30.0, "الأرجنتين", "إنتر ميامي", "الدوري الأمريكي")
        add("mls_fwd_2", "لويس سواريز", "FWD", 84, 15.0, "الأوروغواي", "إنتر ميامي", "الدوري الأمريكي")
        add("mls_fwd_3", "دينيس بوانغا", "FWD", 82, 12.0, "الغابون", "لوس أنجلوس إف سي", "الدوري الأمريكي")

        // ========================================================
        // 9. الدوري التركي (Süper Lig)
        // ========================================================
        add("turk_gk_1", "فرناندو موسليرا", "GK", 83, 10.0, "الأوروغواي", "غلطة سراي", "الدوري التركي")
        add("turk_def_1", "دافينسون سانشيز", "DEF", 82, 12.0, "كولومبيا", "غلطة سراي", "الدوري التركي")
        add("turk_def_2", "ألكسندر دجيكو", "DEF", 80, 9.0, "غانا", "فنربخشة", "الدوري التركي")
        add("turk_mid_1", "سفيان أمرابط", "MID", 82, 12.0, "المغرب", "فنربخشة", "الدوري التركي")
        add("turk_mid_2", "فريد", "MID", 81, 11.0, "البرازيل", "فنربخشة", "الدوري التركي")
        add("turk_mid_3", "لوكاس توريرا", "MID", 82, 12.0, "الأوروغواي", "غلطة سراي", "الدوري التركي")
        add("turk_fwd_1", "فيكتور أوسيمين", "FWD", 87, 24.0, "نيجيريا", "غلطة سراي", "الدوري التركي")
        add("turk_fwd_2", "ماورو إيكاردي", "FWD", 83, 14.0, "الأرجنتين", "غلطة سراي", "الدوري التركي")
        add("turk_fwd_3", "إدين دجيكو", "FWD", 82, 11.0, "البوسنة", "فنربخشة", "الدوري التركي")
        add("turk_fwd_4", "حكيم زياش", "FWD", 82, 12.0, "المغرب", "غلطة سراي", "الدوري التركي")

        // ========================================================
        // 10. الدوري البرتغالي (Primeira Liga)
        // ========================================================
        add("port_gk_1", "ديوغو كوستا", "GK", 86, 18.0, "البرتغال", "بورتو", "الدوري البرتغالي")
        add("port_def_1", "غونسالو إيناسيو", "DEF", 83, 13.0, "البرتغال", "سبورتينغ لشبونة", "الدوري البرتغالي")
        add("port_def_2", "أوتاميندي", "DEF", 81, 10.0, "الأرجنتين", "بنفيكا", "الدوري البرتغالي")
        add("port_mid_1", "أوركون كوكتشو", "MID", 82, 12.0, "تركيا", "بنفيكا", "الدوري البرتغالي")
        add("port_mid_2", "مورتن هيولماند", "MID", 82, 12.0, "الدنمارك", "سبورتينغ لشبونة", "الدوري البرتغالي")
        add("port_mid_3", "أنخيل دي ماريا", "MID", 83, 14.0, "الأرجنتين", "بنفيكا", "الدوري البرتغالي")
        add("port_fwd_1", "فيكتور جيوكيريس", "FWD", 87, 25.0, "السويد", "سبورتينغ لشبونة", "الدوري البرتغالي")
        add("port_fwd_2", "فرانشيسكو ترينكاو", "FWD", 81, 11.0, "البرتغال", "سبورتينغ لشبونة", "الدوري البرتغالي")
        add("port_fwd_3", "غالينو", "FWD", 81, 11.0, "البرازيل", "بورتو", "الدوري البرتغالي")

        return list
    }
}
