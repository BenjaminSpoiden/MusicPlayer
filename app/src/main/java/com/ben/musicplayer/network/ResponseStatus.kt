package com.ben.musicplayer.network

sealed class ResponseStatus<out T> {
    data class Successful<out T>(val response: T): ResponseStatus<T>()
    data class Failed(val message: String?): ResponseStatus<Nothing>()
    object Loading: ResponseStatus<Nothing>()
}