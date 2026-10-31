package com.kscooby.barelink

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {
    private var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auto = findViewById<MaterialSwitch>(R.id.switch_auto)
        val hard = findViewById<MaterialSwitch>(R.id.switch_hard)
        val reshare = findViewById<MaterialSwitch>(R.id.switch_reshare)

        auto.setOnCheckedChangeListener { _, checked ->
            if (!ready) return@setOnCheckedChangeListener
            if (checked && !Settings.canDrawOverlays(this)) {
                auto.isChecked = false
                Toast.makeText(
                    this,
                    "Allow display over other apps first. Android only lets the app on screen read the clipboard.",
                    Toast.LENGTH_LONG,
                ).show()
                return@setOnCheckedChangeListener
            }
            Prefs.setAuto(this, checked)
            if (checked) WatchService.start(this) else stopService(Intent(this, WatchService::class.java))
        }

        hard.setOnCheckedChangeListener { _, checked ->
            if (!ready) return@setOnCheckedChangeListener
            Prefs.setHard(this, checked)
            findViewById<TextView>(R.id.hard_help).setText(
                if (checked) R.string.hard_on_help else R.string.hard_off_help,
            )
        }

        reshare.setOnCheckedChangeListener { _, checked ->
            if (!ready) return@setOnCheckedChangeListener
            Prefs.setReshare(this, checked)
        }

        findViewById<MaterialButton>(R.id.btn_overlay).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btn_notifications).setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            } else {
                Toast.makeText(this, "Notifications are already allowed on this Android version.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_battery).setOnClickListener {
            val pm = getSystemService(PowerManager::class.java)
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                Toast.makeText(this, "Battery optimization is already off for Barelink.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName"),
                ),
            )
        }

        findViewById<MaterialButton>(R.id.btn_clean).setOnClickListener {
            startActivity(
                Intent(this, CleanActivity::class.java).apply {
                    action = CleanActivity.ACTION_CLEAN
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        ready = false
        findViewById<MaterialSwitch>(R.id.switch_auto).isChecked = Prefs.auto(this)
        val hardOn = Prefs.hard(this)
        findViewById<MaterialSwitch>(R.id.switch_hard).isChecked = hardOn
        findViewById<MaterialSwitch>(R.id.switch_reshare).isChecked = Prefs.reshare(this)
        findViewById<TextView>(R.id.hard_help).setText(
            if (hardOn) R.string.hard_on_help else R.string.hard_off_help,
        )
        val overlay = findViewById<MaterialButton>(R.id.btn_overlay)
        overlay.text = if (Settings.canDrawOverlays(this)) {
            getString(R.string.overlay_allowed)
        } else {
            getString(R.string.overlay_needed)
        }
        ready = true
        if (Prefs.auto(this) && Settings.canDrawOverlays(this)) {
            WatchService.start(this)
        }
    }
}
