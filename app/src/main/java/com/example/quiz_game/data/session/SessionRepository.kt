package com.example.quiz_game.data.session

import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SessionRepository {

    suspend fun insert(vararg session: Session): List<Session> =
            withContext(Dispatchers.IO) {
                val prepared =
                        session.map {
                            it.copy(
                                    createdAt = System.currentTimeMillis(),
                                    score = 0,
                                    nickname = Repository.getUser()?.username,
                                    uid = it.generateUid()
                            )
                        }
                App.db.sessionDao().insert(*prepared.toTypedArray())
                prepared
            }

    suspend fun get(): List<Session> = withContext(Dispatchers.IO) { App.db.sessionDao().get() }

    suspend fun getByUid(uid: String): Session =
            withContext(Dispatchers.IO) {
                App.db.sessionDao().getByUid(uid)
                        ?: throw Exception("Session with uid $uid was not found")
            }

    suspend fun deleteByUid(uid: String) =
            withContext(Dispatchers.IO) {
                val session =
                        App.db.sessionDao().getByUid(uid)
                                ?: throw Exception("Session with uid $uid was not found")
                if (session.expiredAt == null) {
                    throw Exception("Session with uid ${session.uid} has not finished yet")
                }
                App.db.sessionDao().deleteByUid(session.uid)
            }

    suspend fun truncate() = withContext(Dispatchers.IO) { App.db.sessionDao().truncate() }

    suspend fun updateScore(uid: String, score: Int): Session =
            withContext(Dispatchers.IO) {
                val session =
                        App.db.sessionDao().getByUid(uid)
                                ?: throw Exception("Session with uid $uid was not found")
                App.db.sessionDao().updateScore(session.uid, score)
                App.db.sessionDao().getByUid(uid)
                        ?: throw Exception("Session with uid $uid was not found after update")
            }

    suspend fun updateAchievements(uid: String, achievements: List<Int>): Session =
            withContext(Dispatchers.IO) {
                val session =
                        App.db.sessionDao().getByUid(uid)
                                ?: throw Exception("Session with uid $uid was not found")
                App.db.sessionDao().updateAchievements(session.uid, achievements)
                App.db.sessionDao().getByUid(uid)
                        ?: throw Exception("Session with uid $uid was not found after update")
            }

    suspend fun updateCreatedAt(uid: String, createdAt: Long): Session =
            withContext(Dispatchers.IO) {
                val session =
                        App.db.sessionDao().getByUid(uid)
                                ?: throw Exception("Session with uid $uid was not found")
                App.db.sessionDao().updateCreatedAt(session.uid, createdAt)
                App.db.sessionDao().getByUid(uid)
                        ?: throw Exception("Session with uid $uid was not found after update")
            }

    suspend fun updateExpiredAt(uid: String, expiredAt: Long): Session =
            withContext(Dispatchers.IO) {
                val session =
                        App.db.sessionDao().getByUid(uid)
                                ?: throw Exception("Session with uid $uid was not found")
                App.db.sessionDao().updateExpiredAt(session.uid, expiredAt)
                App.db.sessionDao().getByUid(uid)
                        ?: throw Exception("Session with uid $uid was not found after update")
            }
}
