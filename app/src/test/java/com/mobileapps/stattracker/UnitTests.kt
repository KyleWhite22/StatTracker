package com.mobileapps.stattracker

import org.junit.Test

import com.mobileapps.stattracker.classes.Group

import com.mobileapps.stattracker.classes.PlayerTotals

import com.mobileapps.stattracker.classes.Game

import com.mobileapps.stattracker.classes.ScoringType

import com.mobileapps.stattracker.classes.WinCondition

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTests {
    @Test
    fun groupCreate_isCorrect() {
        val group = Group(
            id = "123",
            ownerID = "owner1",
            name = "Hoops",
            location = "OSU",
            members = listOf("Max", "Kyle")
        )

        assertEquals("123", group.id)
        assertEquals("owner1", group.ownerID)
        assertEquals("Hoops", group.name)
        assertEquals("OSU", group.location)
        assertEquals(2, group.members.size)
    }

    @Test
    fun playerTotals_calculatesWinPctCorrectly() {
        val totals = PlayerTotals(
            wins = 8,
            losses = 2,
            points = 50,
            rebounds = 20,
            blocks = 5,
            steals = 4
        )

        assertEquals(0.8f, totals.winPct)
    }

    @Test
    fun game_hasCorrectDefaultValues() {
        val game = Game()

        assertEquals("", game.id)
        assertEquals("", game.groupId)
        assertEquals(emptyList<String>(), game.team1)
        assertEquals(emptyList<String>(), game.team2)
        assertEquals(0, game.score1)
        assertEquals(0, game.score2)
        assertEquals(emptyMap<String, Any>(), game.playerStats)
        assertEquals("PENDING", game.status)
        assertEquals(false, game.paused)
        assertEquals(0, game.durationSeconds)
        assertEquals(null, game.startTime)

        assertEquals(WinCondition.FIRST_TO_21, game.settings.winCondition)
        assertEquals(ScoringType.ONES_AND_TWOS, game.settings.scoringType)
        assertEquals(3, game.settings.teamSize)
        assertEquals(10, game.settings.timerDurationMinutes)
    }
}