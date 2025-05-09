package com.example.quiz_game.data.session

import com.example.quiz_game.App
import com.example.quiz_game.other.Utils.runWithTimeout

object SessionRepository {
    suspend fun insert(
        vararg session: Session,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                App.db.sessionDao().insert(
                    *session.map {
                        it.uid = it.generateUid()
                        it.finishedAt = System.currentTimeMillis()
                        it.nickname = App.userPrefs.getString("nickname", null)
                        it
                    }.toTypedArray()
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun get(onSuccess: (List<Session>) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.sessionDao().get()

                if (data.isEmpty()) {
                    onError(Exception("No data found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = { },
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
                    onSuccess = {
                        App.db.sessionDao().deleteByUid(it.uid)
                        onSuccess()
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

    suspend fun updateCategoryName(
        uid: String,
        categoryName: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateCategoryName(it.uid, categoryName)
                        onSuccess()
                    },
                    onError = onError
                )

            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateQuizzesUids(
        uid: String,
        quizzesUids: List<String>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateQuizzesUids(it.uid, quizzesUids)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateScore(
        uid: String,
        score: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateScore(it.uid, score)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateMaxScore(
        uid: String,
        maxScore: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateMaxScore(it.uid, maxScore)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateAchievements(
        uid: String,
        achievements: List<String>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateAchievements(it.uid, achievements)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateStartedAt(
        uid: String,
        startedAt: Long,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.sessionDao().updateStartedAt(it.uid, startedAt)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }
}