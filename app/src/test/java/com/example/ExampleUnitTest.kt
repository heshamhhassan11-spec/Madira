package com.example

import com.example.data.model.ChallengeIconType
import com.example.data.repository.AppDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun repository_initialChallenges_containsFourStrictOrderedChallenges() {
        val repository = AppDataRepository()
        val challenges = repository.challenges.value

        assertEquals(4, challenges.size)

        // 1. Challenge 1: خمن اسم اللاعب (10, 25, 50, 100)
        val c1 = challenges[0]
        assertEquals("guess_player", c1.id)
        assertEquals(1, c1.orderIndex)
        assertEquals("خمن اسم اللاعب", c1.title)
        assertEquals(listOf(10, 25, 50, 100), c1.allowedQuestionCounts)

        // 2. Challenge 2: خمن شعار او اسم النادي (10, 25, 50, 100)
        val c2 = challenges[1]
        assertEquals("guess_club", c2.id)
        assertEquals(2, c2.orderIndex)
        assertEquals("خمن شعار او اسم النادي", c2.title)
        assertEquals(listOf(10, 25, 50, 100), c2.allowedQuestionCounts)

        // 3. Challenge 3: كأس العالم (10, 25, 50)
        val c3 = challenges[2]
        assertEquals("world_cup", c3.id)
        assertEquals(3, c3.orderIndex)
        assertEquals("كأس العالم", c3.title)
        assertEquals(listOf(10, 25, 50), c3.allowedQuestionCounts)

        // 4. Challenge 4: كوّن فريق
        val c4 = challenges[3]
        assertEquals("squad_builder", c4.id)
        assertEquals(4, c4.orderIndex)
        assertEquals("كوّن فريق", c4.title)
    }

    @Test
    fun repository_allTenLeagues_availableInSquadPlayersPool() {
        val repository = AppDataRepository()
        val pool = repository.squadPlayersPool

        val requiredLeagues = setOf(
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

        val leaguesInPool = pool.map { it.league }.toSet()
        assertTrue(leaguesInPool.containsAll(requiredLeagues))
    }

    @Test
    fun repository_twelveTournaments_availableForSimulation() {
        val repository = AppDataRepository()
        val tournaments = repository.tournamentChoices

        assertEquals(12, tournaments.size)

        // Verify simulation runs and generates matches & report
        val chosenTournament = tournaments.first { it.id == "ucl" }
        val dummySquad = repository.squadPlayersPool.take(11)

        val report = repository.simulateTournament(chosenTournament, dummySquad)
        assertNotNull(report)
        assertEquals("دوري أبطال أوروبا", report.tournamentName)
        assertEquals(3, report.matches.size)
        assertTrue(report.xpEarned > 0)
    }

    @Test
    fun repository_addScore_awardsTenPointsPerCorrectAnswer() {
        val repository = AppDataRepository()
        val initialPoints = repository.currentUser.value.points

        repository.addPoints(10)
        assertEquals(initialPoints + 10, repository.currentUser.value.points)
    }

    @Test
    fun levelingSystem_calculatesLevelAndTierProgressCorrectly() {
        val userWithZeroPoints = com.example.data.model.UserProfile(points = 0)
        val progressZero = userWithZeroPoints.levelProgress
        assertEquals(1, progressZero.level)
        assertEquals("tier_rookie", progressZero.currentTier.tierId)
        assertEquals(250, progressZero.pointsToNextLevel)
        assertEquals(0.0f, progressZero.levelProgressRatio, 0.001f)

        val userMid = com.example.data.model.UserProfile(points = 2850)
        val progressMid = userMid.levelProgress
        assertEquals(12, progressMid.level)
        assertEquals("tier_star", progressMid.currentTier.tierId)
    }

    @Test
    fun repository_v13_soundSettingsAndNotifications() {
        val repository = AppDataRepository()

        // 1. Sound settings test
        val initialSound = repository.soundSettings.value
        assertTrue(initialSound.soundEffectsEnabled)
        repository.toggleSoundEffects(false)
        assertEquals(false, repository.soundSettings.value.soundEffectsEnabled)
        repository.setSoundVolume(0.5f)
        assertEquals(0.5f, repository.soundSettings.value.volume, 0.01f)

        // 2. Notifications test
        val initialNotifs = repository.notifications.value
        assertTrue(initialNotifs.isNotEmpty())
        val firstNotif = initialNotifs.first()
        repository.markNotificationAsRead(firstNotif.id)
        assertTrue(repository.notifications.value.first { it.id == firstNotif.id }.isRead)

        // Custom notification creation
        repository.addCustomNotification(
            title = "تحديث دفاعي جديد",
            description = "إضافة أسئلة عن أساطير الدفاع في كؤوس العالم",
            category = "تحديث كروي",
            versionTag = "v1.3"
        )
        val updatedNotifs = repository.notifications.value
        assertEquals("تحديث دفاعي جديد", updatedNotifs.first().title)
        assertEquals("v1.3", updatedNotifs.first().versionTag)

        // 3. User profile update test
        repository.updateUserProfile(
            name = "الكابتن هشام",
            avatarEmoji = "👑",
            favoriteClub = "ريال مدريد"
        )
        assertEquals("الكابتن هشام", repository.currentUser.value.name)
        assertEquals("👑", repository.currentUser.value.avatarEmoji)
        assertEquals("ريال مدريد", repository.currentUser.value.favoriteClub)
    }
}
