package com.example.quiz_game.other

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes

/**
 * Lightweight singleton that plays short sound effects through [SoundPool].
 *
 * The manager is intentionally defensive: any missing or failed-to-load raw
 * resource is treated as a silent no-op so the app never crashes on a
 * dropped/renamed audio asset. This lets us reference all SFX up-front and
 * supply the actual `.mp3` files incrementally.
 *
 * Usage:
 *   - [init] from `App.onCreate()` once
 *   - [play] for one-shots (tap, correct, wrong, buy, achievement, ...)
 *   - [playLooped] / [stop] for looped clips like the countdown ticker
 *   - [release] from `App.onTerminate()`
 */
object SoundManager {

    private const val TAG = "SoundManager"
    private const val MAX_STREAMS = 6

    private var pool: SoundPool? = null
    private val ids = mutableMapOf<Sound, Int>()
    private val loaded = mutableSetOf<Int>()

    /** Master toggle. Flip to "false" to mute every SFX globally. */
    @Volatile
    var enabled: Boolean = true

    fun init(context: Context) {
        if (pool != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val sp = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(attrs)
            .build()

        sp.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded.add(sampleId)
            else Log.w(TAG, "test1234 SoundPool failed to load sampleId=$sampleId status=$status")
        }

        pool = sp

        // Resolve raw resource ids by name so a missing file does not stop
        // the whole load — it simply leaves that Sound unplayable.
        val pkg = context.packageName
        val resources = context.resources
        Sound.entries.forEach { sound ->
            val resId = resources.getIdentifier(sound.resourceName, "raw", pkg)
            if (resId == 0) {
                Log.w(TAG, "test1234 Missing raw resource: R.raw.${sound.resourceName}")
                return@forEach
            }
            try {
                val sampleId = sp.load(context, resId, 1)
                ids[sound] = sampleId
            } catch (t: Throwable) {
                Log.w(TAG, "test1234 Failed to queue load for ${sound.resourceName}", t)
            }
        }
    }

    /**
     * Play a one-shot. Returns the SoundPool stream id (or 0 if nothing was
     * played — e.g. the sample is still loading, missing, or audio is muted).
     *
     * If [volume] is `null` (the default) the sound's own [Sound.defaultVolume]
     * is used so each effect has a sensible mix level out of the box (e.g.
     * the countdown ticks are intentionally much quieter than tap/answer
     * cues). Pass an explicit value to override per call.
     */
    fun play(sound: Sound, volume: Float? = null, rate: Float = 1f): Int {
        if (!enabled) return 0
        val sp = pool ?: return 0
        val sampleId = ids[sound] ?: return 0
        if (sampleId !in loaded) return 0
        val v = volume ?: sound.defaultVolume
        return try {
            sp.play(sampleId, v, v, /*priority*/ 1, /*loop*/ 0, rate)
        } catch (t: Throwable) {
            Log.w(TAG, "play(${sound.resourceName}) failed", t)
            0
        }
    }

    /**
     * Start a looping clip (e.g. the post-game progress sweep). Returns the
     * stream id which the caller must keep and pass to [stop] when the loop
     * should end. As with [play], a `null` [volume] uses [Sound.defaultVolume].
     */
    fun playLooped(sound: Sound, volume: Float? = null, rate: Float = 1f): Int {
        if (!enabled) return 0
        val sp = pool ?: return 0
        val sampleId = ids[sound] ?: return 0
        if (sampleId !in loaded) return 0
        val v = volume ?: sound.defaultVolume
        return try {
            sp.play(sampleId, v, v, /*priority*/ 1, /*loop*/ -1, rate)
        } catch (t: Throwable) {
            Log.w(TAG, "playLooped(${sound.resourceName}) failed", t)
            0
        }
    }

    /** Stop a stream returned by [play] / [playLooped]. Safe with id 0. */
    fun stop(streamId: Int) {
        if (streamId == 0) return
        try { pool?.stop(streamId) } catch (t: Throwable) {
            Log.w(TAG, "stop($streamId) failed", t)
        }
    }

    fun release() {
        pool?.release()
        pool = null
        ids.clear()
        loaded.clear()
    }
}

/**
 * Inventory of every sound effect referenced by the app. Each entry maps to a
 * `R.raw.<resourceName>` file. Missing files are tolerated at runtime — the
 * corresponding [SoundManager.play] call simply no-ops.
 *
 * [defaultVolume] is the per-sound mix level applied when [SoundManager.play]
 * / [SoundManager.playLooped] are called without an explicit volume. Different
 * cues need different intensities (e.g. the countdown ticks must sit well
 * below tap/answer cues so they don't dominate the soundscape), so we set a
 * sensible default here rather than at every call site.
 */
enum class Sound(
    val resourceName: String,
    val defaultVolume: Float,
) {
    /** UI tap — fired on every clickable component. */
    TAP("sfx_tap", defaultVolume = 0.7f),

    /** Per-second countdown tick while the timer is in the normal range. */
    COUNTDOWN_TICK("sfx_countdown_tick", defaultVolume = 0.18f),

    /** Per-second countdown tick when the timer is red / about to run out. */
    COUNTDOWN_TICK_INTENSE("sfx_countdown_tick_intense", defaultVolume = 0.28f),

    /** Player picked the correct answer. */
    ANSWER_CORRECT("sfx_answer_correct", defaultVolume = 1.0f),

    /** Player picked a wrong answer. */
    ANSWER_WRONG("sfx_answer_wrong", defaultVolume = 1.0f),

    /** The timer ran out before the player picked any answer. */
    ANSWER_UNANSWERED("sfx_answer_unanswered", defaultVolume = 1.0f),

    /** Successful purchase in the shop. */
    SHOP_BUY("sfx_shop_buy", defaultVolume = 0.9f),

    /** Successful sell in the shop. */
    SHOP_SELL("sfx_shop_sell", defaultVolume = 0.9f),

    /** Looping tick that plays while the post-game score sweep is animating. */
    POSTGAME_PROGRESS("sfx_postgame_progress", defaultVolume = 0.5f),

    /** Steam-style achievement unlocked popup. */
    ACHIEVEMENT_POPUP("sfx_achievement_popup", defaultVolume = 1.0f);
}

/**
 * Wraps an `onClick` lambda so that every invocation also fires the global
 * tap SFX. Use everywhere a clickable component is set up so the tap sound
 * stays consistent without having to remember to call [SoundManager.play] at
 * every call site.
 *
 * Usage: `onClick = withTap(onClick)` or `onClick = withTap { ... }`.
 */
inline fun withTap(crossinline onClick: () -> Unit): () -> Unit = {
    SoundManager.play(Sound.TAP)
    onClick()
}
