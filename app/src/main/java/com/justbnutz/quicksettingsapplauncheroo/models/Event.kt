package com.justbnutz.quicksettingsapplauncheroo.models

/**
 * Event wrapper for data that is exposed via a LiveData that represents an event.
 * Handy for handling LiveData that should only be consumed once (LiveData values may be
 * re-emitted on config changes, like rotation, leaving to unexpected behaviour):
 * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 *
 * Using this method instead of the newer Kotlin Channels method as seems that has some quirks for
 * ViewModels that are observed by multiple Views that I haven't figured out yet:
 * (https://proandroiddev.com/android-singleliveevent-redux-with-kotlin-flow-b755c70bb055)
 */
open class Event<out T>(private val data: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the data and prevents its use again.
     */
    fun getDataIfNotHandled(): T? =
        if (hasBeenHandled) null
        else {
            hasBeenHandled = true
            data
        }

    /**
     * Returns the data, even if it's already been handled.
     */
    fun peekData(): T = data
}