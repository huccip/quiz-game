package com.example.quiz_game.data.user

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

// TODO: setup proto datastore correctly

object UserSerializer : Serializer<User> {
    override val defaultValue: User
        get() = User()

    override suspend fun readFrom(input: InputStream): User {
        return User()
    }

    override suspend fun writeTo(
        t: User,
        output: OutputStream
    ) {
        TODO("Not yet implemented")
    }
}