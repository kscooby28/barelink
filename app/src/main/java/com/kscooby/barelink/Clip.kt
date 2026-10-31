package com.kscooby.barelink

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.SystemClock

object Gate {
    @Volatile
    var suppressUntil: Long = 0L

    const val LABEL = "barelink"
}

fun readClipboard(context: Context): String? {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if (!cm.hasPrimaryClip()) return null
    val clip = cm.primaryClip ?: return null
    if (clip.description?.label == Gate.LABEL) return null
    if (isSensitive(clip)) return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(context)?.toString()
}

fun isSensitive(clip: ClipData): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    val extras = clip.description?.extras ?: return false
    return extras.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false)
}

fun writeClipboard(context: Context, text: String) {
    Gate.suppressUntil = SystemClock.elapsedRealtime() + 1500
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(Gate.LABEL, text))
}
