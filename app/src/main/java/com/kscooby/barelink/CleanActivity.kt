package com.kscooby.barelink

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Invisible activity. Three jobs:
 * 1. Share sheet target — strip the shared text, copy it, optionally re-share.
 * 2. Text-selection action ("Strip link") next to the keyboard.
 * 3. Clipboard read — only works once this window is focused, which is how
 *    Android 10+ allows a non-keyboard app to see the clipboard.
 */
class CleanActivity : AppCompatActivity() {
    private var done = false
    private val handler = Handler(Looper.getMainLooper())
    private val giveUp = Runnable {
        if (done) return@Runnable
        done = true
        if (!quiet) toast("Could not read the clipboard.")
        finishQuiet()
    }

    private val quiet: Boolean
        get() = intent?.getBooleanExtra(EXTRA_QUIET, false) == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                done = true
                handleShare(incomingText())
                return
            }
            Intent.ACTION_PROCESS_TEXT -> {
                done = true
                handleSelection()
                return
            }
        }
        shrinkWindow()
        handler.postDelayed(giveUp, 1600)
    }

    private fun shrinkWindow() {
        window.setLayout(1, 1)
        window.setGravity(Gravity.TOP or Gravity.START)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) tryCleanClipboard()
    }

    private fun tryCleanClipboard() {
        if (done) return
        val text = readClipboard(this)
        if (text == null) return
        done = true
        handler.removeCallbacks(giveUp)
        val cleaned = LinkStrip.strip(text, Prefs.hard(this))
        if (cleaned != text) {
            writeClipboard(this, cleaned)
            toast("Link shortened")
        } else if (!quiet) {
            toast("No link junk to cut")
        }
        finishQuiet()
    }

    private fun handleShare(text: String) {
        if (text.isBlank()) {
            toast("Nothing to strip")
            finishQuiet()
            return
        }
        val cleaned = LinkStrip.strip(text, Prefs.hard(this))
        writeClipboard(this, cleaned)
        if (cleaned != text) toast("Link shortened") else toast("Already clean")
        if (Prefs.reshare(this)) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, cleaned)
            }
            val chooser = Intent.createChooser(send, "Share clean link").apply {
                putExtra(
                    Intent.EXTRA_EXCLUDE_COMPONENTS,
                    arrayOf(ComponentName(packageName, CleanActivity::class.java.name)),
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(chooser)
            // Finish after the chooser is up. Finishing first drops it on some phones.
            handler.postDelayed({
                if (!isFinishing) finishQuiet()
            }, 400)
            return
        }
        finishQuiet()
    }

    private fun handleSelection() {
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        val cleaned = LinkStrip.strip(text, Prefs.hard(this))
        val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
        if (!readonly) {
            setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, cleaned))
        }
        if (cleaned.isNotEmpty()) writeClipboard(this, cleaned)
        if (cleaned != text) toast("Link shortened")
        finishQuiet()
    }

    private fun incomingText(): String {
        val extra = intent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.toString()
        if (!extra.isNullOrBlank()) return extra
        val clip = intent.clipData
        if (clip != null && clip.itemCount > 0) {
            return clip.getItemAt(0).coerceToText(this)?.toString().orEmpty()
        }
        return ""
    }

    private fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun finishQuiet() {
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        handler.removeCallbacks(giveUp)
        if (intent?.action == ACTION_CLEAN) Gate.cleaning = false
        super.onDestroy()
    }

    companion object {
        const val ACTION_CLEAN = "com.kscooby.barelink.CLEAN"
        const val EXTRA_QUIET = "quiet"
    }
}
