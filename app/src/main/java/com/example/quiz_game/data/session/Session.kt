package com.example.quiz_game.data.session

import android.util.Base64
import androidx.compose.ui.util.fastJoinToString
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = false)
    var uid: String = "",
    var nickname: String? = null,
    var categoryName: String? = null,
    var quizzesUids: List<String>? = null,
    var score: Int? = null,
    var maxScore: Int? = null,
    var achievements: List<Int>? = null,
    var startedAt: Long? = null,
    var finishedAt: Long? = null,
) {
    fun generateUid(): String = Base64.encodeToString(
        ((nickname + categoryName + startedAt) +
                        (UUID.randomUUID().mostSignificantBits))
            .split("")
            .shuffled()
            .fastJoinToString("")
            .toByteArray(),
        Base64.CRLF
    )

    fun timelapse(): String? {
        if (startedAt == null || finishedAt == null) return "Undefined"

        val seconds = (finishedAt!! - startedAt!!) / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return "${hours.toString().padStart(2, '0')}:${
            (minutes % 60).toString().padStart(2, '0')
        }:${(seconds % 60).toString().padStart(2, '0')}"
    }

    fun scorePercentage(): String? {
        if (maxScore == null || score == null) return null

        return "${(score!!.toDouble() / maxScore!!.toDouble() * 100).toInt()}%"
    }
}
