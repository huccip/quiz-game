package com.example.quiz_game.other

infix fun <A, B, C> Pair<A, B>.too(c: C): Triple<A, B, C> = Triple(this.first, this.second, c)