package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class AppDataRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createUserProfile_validPayload_createsDocument(): Unit = runBlocking {
    val email = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
    val uid = signInTestUser(email)
    val repository = AppDataRepository(firestore)

    val profile = UserProfile(
      id = uid,
      userId = uid,
      name = "Alice Star",
      email = email,
      avatarEmoji = "⚽",
      points = 100,
      winStreak = 2,
      virtualCoins = 200.0,
      favoriteChallenges = listOf("guess_player")
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createUserProfile(profile)
    }
    assertTrue("Create profile should succeed", result.isSuccess)

    val fetchResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getUserProfile(uid)
    }
    assertTrue("Get profile should succeed", fetchResult.isSuccess)
    val fetched = fetchResult.getOrThrow()
    assertEquals(uid, fetched.userId)
    assertEquals("Alice Star", fetched.name)
  }

  @Test
  fun getUserProfile_crossUserAccess_failsWithPermissionDenied(): Unit = runBlocking {
    val aliceEmail = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
    val bobEmail = "bob_${UUID.randomUUID().toString().take(8)}@test.com"
    val aliceUid = signInTestUser(aliceEmail)
    val aliceRepo = AppDataRepository(firestore)
    val profile = UserProfile(
      id = aliceUid,
      userId = aliceUid,
      name = "Alice Star",
      email = aliceEmail,
      avatarEmoji = "⚽",
      points = 100,
      winStreak = 2,
      virtualCoins = 200.0
    )
    withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createUserProfile(profile) }

    // Bob signs in and tries to read Alice's private profile
    signInTestUser(bobEmail)
    val bobRepo = AppDataRepository(firestore)

    val fetchResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.getUserProfile(aliceUid)
    }
    assertTrue("Cross-user read should fail", fetchResult.isFailure)
    val exception = fetchResult.exceptionOrNull()
    val firestoreEx = checkNotNull(exception?.unwrapFirestoreException()) {
      "Expected FirebaseFirestoreException but got $exception"
    }
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
  }

  @Test
  fun observeUserProfile_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    val email = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
    val aliceUid = signInTestUser(email)
    val aliceRepo = AppDataRepository(firestore)
    val profile = UserProfile(
      id = aliceUid,
      userId = aliceUid,
      name = "Alice Star",
      email = email,
      avatarEmoji = "⚽",
      points = 100,
      winStreak = 2,
      virtualCoins = 200.0
    )
    withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createUserProfile(profile) }

    auth.signOut()
    val unauthRepo = AppDataRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        unauthRepo.observeUserProfile(aliceUid).first()
      }
      fail("Expected snapshot listener to fail on unauthenticated access")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  @Test
  fun observeLeaderboard_authenticatedUser_emitsLeaderboard(): Unit = runBlocking {
    val email = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
    val aliceUid = signInTestUser(email)
    val repository = AppDataRepository(firestore)

    val profile = UserProfile(
      id = aliceUid,
      userId = aliceUid,
      name = "Alice Star",
      email = email,
      avatarEmoji = "⚽",
      points = 300,
      winStreak = 5,
      virtualCoins = 200.0
    )
    withTimeout(DEFAULT_TIMEOUT_MS) { repository.createUserProfile(profile) }

    val entries = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeLeaderboard().first { list ->
        list.any { it.userId == aliceUid }
      }
    }
    assertTrue("Leaderboard should contain Alice entry", entries.any { it.userId == aliceUid })
  }

  companion object {
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
