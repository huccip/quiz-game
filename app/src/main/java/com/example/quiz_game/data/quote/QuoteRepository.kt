package com.example.quiz_game.data.quote

import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Utils.runWithTimeout

object QuoteRepository {
    private const val TAG = "QuoteRepository"

    enum class ReportType {
        Nudity, Violence, Racism, SomethingElse
    }

    suspend fun get(
        onSuccess: (Quote) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: Quote? = null
        runWithTimeout(
            block = {
                val response = Service.quoteService.get()
                if (response.isSuccessful) {
                    data = response.body()
                } else {
                    onError(Exception("Error while getting quote ${response.errorBody()}"))
                }
            },
            onFinish = {
                if (data == null) {
                    onError(Exception("Quote was not found"))
                    return@runWithTimeout
                }

                onSuccess(data!!)
            },
            onTimeout = onError
        )
    }

    suspend fun report(
        type: ReportType,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                // TODO: update report function
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }
}