package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.data.model.ChallengeItem
import com.example.data.repository.AppDataRepository
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppNavDestination
import com.example.ui.screens.AdminRechargeScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.RechargeRequest
import com.example.ui.screens.SubscriptionRechargeScreen
import com.example.ui.screens.ChallengesMainScreen
import com.example.ui.screens.ClubGuessScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.PlayerGuessScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SquadBuilderScreen
import com.example.ui.screens.WorldCupScreen
import com.example.ui.theme.DarkAppBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FootballChallengesApp()
                }
            }
        }
    }
}

@Composable
fun FootballChallengesApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val repository = remember { AppDataRepository(context) }

    // User session state from Firebase Auth & App authentication
    var firebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isAppAuthenticated by remember { mutableStateOf(Firebase.auth.currentUser != null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
            if (auth.currentUser != null) {
                isAppAuthenticated = true
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(firebaseUser?.uid) {
        val uid = firebaseUser?.uid
        if (uid != null) {
            repository.syncWithFirestore(uid, this)
        }
    }

    // Navigation state
    var currentDestination by remember { mutableStateOf(AppNavDestination.CHALLENGES) }
    var activeChallenge by remember { mutableStateOf<ChallengeItem?>(null) }
    var showAdminRecharge by remember { mutableStateOf(false) }
    var showSubscriptionScreen by remember { mutableStateOf(false) }
    var pendingRechargeRequests by remember {
        mutableStateOf(mutableListOf<RechargeRequest>())
    }

    // Eye Comfort Font Scale
    var fontScale by remember { mutableFloatStateOf(1.0f) }

    // Observed Data
    val currentUser by repository.currentUser.collectAsState()
    val challenges by repository.challenges.collectAsState()
    val leaderboard by repository.leaderboard.collectAsState()
    val soundSettings by repository.soundSettings.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val friendsList by repository.friends.collectAsState()
    val friendRequestsList by repository.friendRequests.collectAsState()
    val transactionsList by repository.transactions.collectAsState()

    val favoriteChallenges = remember(challenges) {
        challenges.filter { it.isFavorite }
    }

    if (!isAppAuthenticated && firebaseUser == null) {
        // Login & Registration Auth Gate
        AuthScreen(
            onLoginSuccess = { name, email, club, avatar, isNewUser ->
                repository.login(email = email, name = name, club = club, avatar = avatar, isNewUser = isNewUser)
                firebaseUser = Firebase.auth.currentUser
                isAppAuthenticated = true
            },
            fontScale = fontScale
        )
    } else if (showAdminRecharge) {
        AdminRechargeScreen(
            requests = pendingRechargeRequests,
            onApprove = { reqId, username ->
                val req = pendingRechargeRequests.find { it.id == reqId }
                if (req != null) {
                    repository.grantPackageSubscription(req.amount)
                }
                pendingRechargeRequests.removeIf { it.id == reqId }
            },
            onReject = { reqId, username ->
                pendingRechargeRequests.removeIf { it.id == reqId }
            },
            onBack = { showAdminRecharge = false },
            fontScale = fontScale
        )
    } else if (showSubscriptionScreen) {
        SubscriptionRechargeScreen(
            currentUsername = currentUser.name,
            currentUserTagId = currentUser.userTagId,
            onSubmitRequest = { packageTitle, method, phone, receiptUri ->
                val newReq = RechargeRequest(
                    id = "req_${System.currentTimeMillis()}",
                    username = currentUser.name,
                    userTagId = currentUser.userTagId,
                    amount = packageTitle,
                    method = method,
                    phone = phone,
                    receiptImageUri = receiptUri
                )
                pendingRechargeRequests.add(newReq)
                showSubscriptionScreen = false
            },
            onBack = { showSubscriptionScreen = false },
            fontScale = fontScale
        )
    } else if (activeChallenge != null) {
        // Active challenge playable screens (The 4 challenges)
        when (activeChallenge?.id) {
            "guess_player" -> {
                PlayerGuessScreen(
                    questions = repository.playerQuestions,
                    onBack = { activeChallenge = null },
                    onAddScore = { points -> repository.addPoints(points) },
                    fontScale = fontScale
                )
            }
            "guess_club" -> {
                ClubGuessScreen(
                    questions = repository.clubQuestions,
                    onBack = { activeChallenge = null },
                    onAddScore = { points -> repository.addPoints(points) },
                    fontScale = fontScale
                )
            }
            "world_cup" -> {
                WorldCupScreen(
                    questions = repository.worldCupQuestions,
                    onBack = { activeChallenge = null },
                    onAddScore = { points -> repository.addPoints(points) },
                    fontScale = fontScale
                )
            }
            "squad_builder" -> {
                SquadBuilderScreen(
                    playersPool = repository.squadPlayersPool,
                    tournamentChoices = repository.tournamentChoices,
                    onSimulate = { tournament, squad ->
                        repository.simulateTournament(tournament, squad)
                    },
                    onBack = { activeChallenge = null },
                    fontScale = fontScale
                )
            }
            else -> {
                activeChallenge = null
            }
        }
    } else {
        // Main scaffold with bottom navigation (No VIP/Subscriptions)
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkAppBackground),
            containerColor = DarkAppBackground,
            bottomBar = {
                AppBottomNavigation(
                    currentDestination = currentDestination,
                    onNavigate = { destination -> currentDestination = destination },
                    favoritesCount = favoriteChallenges.size,
                    fontScale = fontScale
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppNavDestination.CHALLENGES -> {
                        ChallengesMainScreen(
                            challenges = challenges,
                            notifications = notifications,
                            onMarkNotificationAsRead = { notifId -> repository.markNotificationAsRead(notifId) },
                            onMarkAllNotificationsAsRead = { repository.markAllNotificationsAsRead() },
                            onAddNotification = { title, desc, cat, ver ->
                                repository.addCustomNotification(title, desc, cat, ver)
                            },
                            onChallengeSelected = { challenge -> activeChallenge = challenge },
                            onToggleFavorite = { challengeId -> repository.toggleFavorite(challengeId) },
                            fontScale = fontScale,
                            onFontScaleChange = { newScale -> fontScale = newScale },
                            userPoints = currentUser.points
                        )
                    }
                    AppNavDestination.LEADERBOARD -> {
                        LeaderboardScreen(
                            leaderboard = leaderboard,
                            fontScale = fontScale
                        )
                    }
                    AppNavDestination.PROFILE -> {
                        ProfileScreen(
                            user = currentUser,
                            friendsList = friendsList,
                            friendRequestsList = friendRequestsList,
                            transactionsList = transactionsList,
                            onSendFriendRequest = { tagId -> repository.sendFriendRequest(tagId) },
                            onAcceptFriendRequest = { reqId -> repository.acceptFriendRequest(reqId) },
                            onRejectFriendRequest = { reqId -> repository.rejectFriendRequest(reqId) },
                            soundSettings = soundSettings,
                            onUpdateSoundSettings = { settings -> repository.updateSoundSettings(settings) },
                            onUpdateProfile = { name, avatar, club ->
                                repository.updateUserProfile(name, avatar, club)
                            },
                            fontScale = fontScale,
                            onFontScaleChange = { newScale -> fontScale = newScale },
                            onSignOut = {
                                signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {
                                        firebaseUser = null
                                        isAppAuthenticated = false
                                    },
                                    scope = coroutineScope
                                )
                            },
                            onNavigateToChallenges = { currentDestination = AppNavDestination.CHALLENGES },
                            onNavigateToAdminRecharge = { showAdminRecharge = true },
                            onNavigateToSubscription = { showSubscriptionScreen = true }
                        )
                    }
                }
            }
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
