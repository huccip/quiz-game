package com.example.quiz_game.other

import com.example.quiz_game.data.shop.ShopCatalog
import com.example.quiz_game.data.shop.ShopItem
import java.util.Calendar

/**
 * Pure helpers for the three retention features (daily loot box, login
 * streak, first-quiz-of-the-day x2). Keeping the date math and reward tables
 * in one place makes them trivial to unit-test and to tweak from a single
 * location.
 */
object DailyRewards {

    // ── Calendar-day math ────────────────────────────────────────────────

    /**
     * Returns true when [a] and [b] (both epoch-millis) fall on the same
     * local-calendar day. Time-of-day is ignored, so 23:59 and 00:01 count
     * as different days even though they're only 2 minutes apart.
     */
    fun sameDay(a: Long, b: Long): Boolean {
        if (a == 0L || b == 0L) return false
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
                ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Whole-day gap between [earlier] and [later] using local-calendar days
     * (so 23:59 → 00:01 = 1, not 0). Returns `Int.MAX_VALUE` when [earlier]
     * is the sentinel `0L` (i.e. the user has never recorded that event).
     */
    fun calendarDaysBetween(earlier: Long, later: Long): Int {
        if (earlier == 0L) return Int.MAX_VALUE
        val msPerDay = 24L * 60L * 60L * 1000L
        // Anchor both timestamps to local midnight before subtracting so DST
        // and time-of-day shifts don't bleed into the day count.
        fun midnight(t: Long): Long = Calendar.getInstance().apply {
            timeInMillis = t
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val diffMs = midnight(later) - midnight(earlier)
        return (diffMs / msPerDay).toInt().coerceAtLeast(0)
    }

    // ── Login streak ─────────────────────────────────────────────────────

    /**
     * Coin reward for the n-th consecutive day of the current streak run
     * (1-indexed). Caps at day 7 then loops, so a 14-day streak is two full
     * "weeks" of escalating rewards.
     */
    fun streakReward(day: Int): Int {
        val schedule = intArrayOf(10, 20, 30, 45, 60, 80, 100)
        val idx = ((day - 1).coerceAtLeast(0)) % schedule.size
        return schedule[idx]
    }

    /** Result of a single login-streak evaluation. */
    data class StreakOutcome(
        /** New streak day count (1-based) AFTER applying today's login. */
        val streakDays: Int,
        /** Coins to grant for today's login. */
        val coinsGranted: Int,
        /** True if the streak was reset because the user missed 2+ days. */
        val wasReset: Boolean,
        /** True if today was already counted (no change, no reward). */
        val alreadyClaimedToday: Boolean,
    )

    /**
     * Pure decision function. Given the user's previous [lastLoginAt] /
     * [previousStreakDays] and the current [now], decide what the new streak
     * day count should be and how many coins to grant.
     */
    fun evaluateLogin(
        now: Long,
        lastLoginAt: Long,
        previousStreakDays: Int,
    ): StreakOutcome {
        if (sameDay(now, lastLoginAt)) {
            return StreakOutcome(
                streakDays = previousStreakDays.coerceAtLeast(1),
                coinsGranted = 0,
                wasReset = false,
                alreadyClaimedToday = true,
            )
        }
        val gap = calendarDaysBetween(lastLoginAt, now)
        return when {
            // First-ever login or 2+ day gap → fresh streak starting today.
            lastLoginAt == 0L || gap >= 2 -> StreakOutcome(
                streakDays = 1,
                coinsGranted = streakReward(1),
                wasReset = lastLoginAt != 0L,
                alreadyClaimedToday = false,
            )
            // Exactly the next calendar day → continue the streak.
            else -> {
                val next = previousStreakDays + 1
                StreakOutcome(
                    streakDays = next,
                    coinsGranted = streakReward(next),
                    wasReset = false,
                    alreadyClaimedToday = false,
                )
            }
        }
    }

    // ── Daily loot box ───────────────────────────────────────────────────

    /** Available iff a fresh calendar day has rolled over since [lastClaimAt]. */
    fun lootBoxAvailable(now: Long, lastClaimAt: Long): Boolean {
        if (lastClaimAt == 0L) return true
        return !sameDay(now, lastClaimAt)
    }

    /** Outcome of opening the daily loot box. */
    sealed interface LootBoxReward {
        /** Cosmetic "no luck today" outcome — still resets the 24h cooldown. */
        data object Nothing : LootBoxReward
        data class Coins(val amount: Int) : LootBoxReward
        data class PowerUp(val item: ShopItem) : LootBoxReward
    }

    /**
     * Roll the loot table. Weights chosen to feel rewarding but not trivial:
     * the median outcome is ~50–100 coins, with a long tail of misses and
     * occasional high payouts to keep the daily check-in interesting.
     */
    fun rollLootBox(): LootBoxReward {
        // Weighted bag (sums to 100). Tweak here to retune drop rates.
        val r = (1..100).random()
        return when {
            r <= 8 -> LootBoxReward.Nothing                                  //  8% — cold streak
            r <= 33 -> LootBoxReward.Coins(15)                               // 25% — small
            r <= 58 -> LootBoxReward.Coins(40)                               // 25% — solid
            r <= 75 -> LootBoxReward.Coins(100)                              // 17% — big
            r <= 80 -> LootBoxReward.Coins(200)                              //  5% — jackpot
            else -> {                                                        // 20% — power-up
                val item = ShopCatalog.items.random()
                LootBoxReward.PowerUp(item)
            }
        }
    }

    // ── First-quiz-of-the-day ────────────────────────────────────────────

    /** True iff the user has not yet completed any quiz on the current day. */
    fun isFirstQuizOfDay(now: Long, lastQuizCompletedAt: Long): Boolean =
        !sameDay(now, lastQuizCompletedAt)

    /** Multiplier applied silently on top of normal scoring for first-of-day. */
    const val FIRST_QUIZ_OF_DAY_MULTIPLIER: Int = 2
}
