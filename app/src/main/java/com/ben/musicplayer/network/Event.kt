package com.ben.musicplayer.network

open class Event<out T>(private val response: T) {
    var hasBeenHandled = false
        private set

    fun onContentNotHandled(): T? {
        return if(hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            response
        }
    }

    fun getContentEvenIfHandled() = response
}