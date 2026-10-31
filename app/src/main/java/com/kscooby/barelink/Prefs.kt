package com.kscooby.barelink

import android.content.Context

object Prefs {
    private const val FILE = "barelink"
    private const val AUTO = "auto"
    private const val HARD = "hard"
    private const val RESHARE = "reshare"

    private fun sp(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun auto(context: Context) = sp(context).getBoolean(AUTO, false)

    fun setAuto(context: Context, on: Boolean) {
        sp(context).edit().putBoolean(AUTO, on).apply()
    }

    /** True = delete everything after "?". False = drop tracking keys only. */
    fun hard(context: Context) = sp(context).getBoolean(HARD, true)

    fun setHard(context: Context, on: Boolean) {
        sp(context).edit().putBoolean(HARD, on).apply()
    }

    fun reshare(context: Context) = sp(context).getBoolean(RESHARE, true)

    fun setReshare(context: Context, on: Boolean) {
        sp(context).edit().putBoolean(RESHARE, on).apply()
    }
}
