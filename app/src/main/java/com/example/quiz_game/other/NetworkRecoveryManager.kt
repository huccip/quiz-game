package com.example.quiz_game.other

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Generic singleton that manages pending network tasks.
 *
 * Any service can register a [PendingTask] when a network call fails. When internet returns, the UI
 * calls [retryAll] to re-execute all pending tasks.
 *
 * Usage:
 * ```
 * // In your catch block:
 * catch (e: IOException) {
 *     NetworkRecoveryManager.addPendingTask(
 *         PendingTask(id = "quotes", label = "Daily quote") { fetchQuote() }
 *     )
 * }
 * ```
 */
object NetworkRecoveryManager {

    private const val TAG = "NetworkRecoveryManager"

    /**
     * A unit of work that failed due to network issues and should be retried.
     *
     * @param id Unique key (e.g. "translator", "categories", "quotes").
     * ```
     *           Adding a task with the same id replaces the previous one.
     * @param label
     * ```
     * User-facing name shown in recovery UI (e.g. "Translation model").
     * @param retry The suspend function to re-execute when internet returns.
     */
    data class PendingTask(val id: String, val label: String, val retry: suspend () -> Unit)

    private val _pendingTasks = MutableStateFlow<List<PendingTask>>(emptyList())
    val pendingTasks: StateFlow<List<PendingTask>> = _pendingTasks.asStateFlow()

    private val mutex = Mutex()

    /**
     * Register a task to retry when internet returns. Idempotent: calling with the same
     * [PendingTask.id] replaces the old task.
     */
    suspend fun addPendingTask(task: PendingTask) {
        mutex.withLock {
            val current = _pendingTasks.value.toMutableList()
            current.removeAll { it.id == task.id }
            current.add(task)
            _pendingTasks.value = current
            Log.d(TAG, "Added pending task: ${task.id} (${task.label}). Total: ${current.size}")
        }
    }

    /** Remove a pending task (e.g. after successful manual retry or when no longer needed). */
    suspend fun removePendingTask(id: String) {
        mutex.withLock {
            val current = _pendingTasks.value.toMutableList()
            current.removeAll { it.id == id }
            _pendingTasks.value = current
            Log.d(TAG, "Removed pending task: $id. Remaining: ${current.size}")
        }
    }

    /** Check whether a task with [id] is already registered. */
    fun hasPending(id: String): Boolean = _pendingTasks.value.any { it.id == id }

    /**
     * Retry ALL pending tasks. Removes each task on success; keeps it on failure. Returns the
     * number of tasks that succeeded.
     */
    suspend fun retryAll(): Int {
        val tasks = _pendingTasks.value.toList()
        if (tasks.isEmpty()) return 0

        Log.d(TAG, "Retrying ${tasks.size} pending task(s)…")
        var succeeded = 0

        for (task in tasks) {
            try {
                Log.d(TAG, "Retrying: ${task.id}")
                task.retry()
                removePendingTask(task.id)
                succeeded++
                Log.d(TAG, "✅ ${task.id} succeeded")
            } catch (e: Exception) {
                Log.e(TAG, "❌ ${task.id} failed again", e)
                // Keep in pendingTasks for next retry
            }
        }

        Log.d(TAG, "Retry complete: $succeeded/${tasks.size} succeeded")
        return succeeded
    }

    /**
     * Retry a specific pending task by [id].
     * @return true if the task succeeded and was removed, false otherwise.
     */
    suspend fun retryTask(id: String): Boolean {
        val task = _pendingTasks.value.find { it.id == id } ?: return false
        return try {
            task.retry()
            removePendingTask(id)
            Log.d(TAG, "✅ $id retried successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ $id retry failed", e)
            false
        }
    }

    /** Clear all pending tasks. */
    suspend fun clearAll() {
        mutex.withLock {
            _pendingTasks.value = emptyList()
            Log.d(TAG, "Cleared all pending tasks")
        }
    }
}
