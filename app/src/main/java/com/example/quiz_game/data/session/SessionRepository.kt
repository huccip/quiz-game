package com.example.quiz_game.data.session

import androidx.compose.ui.util.fastFirstOrNull
import com.example.quiz_game.App
import com.example.quiz_game.other.Utils.runWithTimeout

object SessionRepository {

    private const val TAG = "test1234 SessionRepository"

    suspend fun insert(
        vararg session: Session,
        onSuccess: (ArrayList<Session>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val sessions = arrayListOf<Session>()
        runWithTimeout(
            block = {
                App.db.sessionDao().insert(*session.map {
                    it.createdAt = System.currentTimeMillis()
                    it.score = 0
                    it.nickname = App.userPrefs.getString("nickname", null)
                    it.uid = it.generateUid()

                    sessions.add(it)
                    it
                }.toTypedArray())
            },
            onFinish = { onSuccess(sessions) },
            onTimeout = onError
        )
    }

    suspend fun get(onSuccess: (List<Session>) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.sessionDao().get()

                if (data.isEmpty()) {
                    onError(Exception("No sessions found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = { },
            onTimeout = onError
        )
    }

    suspend fun resume(onSuccess: (Session) -> Unit, onError: (Throwable) -> Unit) {
        var data: Session? = null

        runWithTimeout(
            block = {
                get(
                    onSuccess = {
                        data = it.fastFirstOrNull { session -> session.expiredAt == null }
                    },
                    onError = onError
                )
            },
            onFinish = {
                if (data == null) {
                    onError(Exception("No active sessions found"))
                    return@runWithTimeout
                }

                onSuccess(data!!)
            },
            onTimeout = onError
        )
    }

    suspend fun getByUid(uid: String, onSuccess: (Session) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.sessionDao().getByUid(uid)

                if (data == null) {
                    onError(Exception("Session with uid $uid was not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = { },
            onTimeout = onError
        )
    }

    suspend fun deleteByUid(uid: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = { session ->
                        if (session.expiredAt == null) {
                            onError(Exception("Session with uid ${session.uid} has not finished yet"))
                            return@getByUid
                        }

                        App.db.sessionDao().deleteByUid(session.uid)
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun truncate(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = { App.db.sessionDao().truncate() },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateScore(
        uid: String,
        score: Int,
        onSuccess: (Session) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateScore(it.uid, score)
                    },
                    onError = onError
                )
            },
            onFinish = {
                getByUid(
                    uid = uid,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onTimeout = onError
        )
    }

    suspend fun updateAchievements(
        uid: String,
        achievements: List<String>,
        onSuccess: (Session) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateAchievements(it.uid, achievements)
                    },
                    onError = onError
                )
            },
            onFinish = {
                getByUid(
                    uid = uid,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onTimeout = onError
        )
    }

    suspend fun updateCreatedAt(
        uid: String,
        createdAt: Long,
        onSuccess: (Session) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateCreatedAt(it.uid, createdAt)
                    },
                    onError = onError
                )
            },
            onFinish = {
                getByUid(
                    uid = uid,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onTimeout = onError
        )
    }

    suspend fun updateExpiredAt(
        uid: String,
        expiredAt: Long,
        onSuccess: (Session) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = { App.db.sessionDao().updateExpiredAt(it.uid, expiredAt) },
                    onError = onError
                )
            },
            onFinish = {
                getByUid(
                    uid = uid,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onTimeout = onError
        )
    }
}