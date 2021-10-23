package com.justbnutz.quicksettingsapplauncheroo.models

/**
 * Used for packaging responses out of and between coroutines (like an RxEvent)
 * https://developer.android.com/kotlin/coroutines#executing-in-a-background-thread
 */
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
