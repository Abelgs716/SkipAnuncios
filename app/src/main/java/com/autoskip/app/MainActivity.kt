package com.autoskip.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var statusCard: MaterialCardView
    private lateinit var statusEmoji: TextView
    private lateinit var statusTitle: TextView
    private lateinit var statusDetail: TextView
    private lateinit var autoSkipSwitch: MaterialSwitch
    private lateinit var grantButton: Button
    private lateinit var counterText: TextView

    // The service runs in this same process, so its counter updates reach us live.
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Prefs.KEY_SKIPPED_COUNT) updateCounter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Prefs.isOnboardingDone(this)) {
            startActivity(Intent(this, PermissionActivity::class.java))
        }
        setContentView(R.layout.activity_main)

        statusCard = findViewById(R.id.status_card)
        statusEmoji = findViewById(R.id.status_emoji)
        statusTitle = findViewById(R.id.status_title)
        statusDetail = findViewById(R.id.status_detail)
        autoSkipSwitch = findViewById(R.id.autoskip_switch)
        grantButton = findViewById(R.id.button_grant)
        counterText = findViewById(R.id.counter_text)

        autoSkipSwitch.isChecked = Prefs.isAutoSkipEnabled(this)
        autoSkipSwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setAutoSkipEnabled(this, checked)
            updateStatus()
        }
        grantButton.setOnClickListener { openPermissionScreen() }
        findViewById<Button>(R.id.button_permissions_info).setOnClickListener { openPermissionScreen() }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        updateCounter()
        Prefs.get(this).registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onPause() {
        Prefs.get(this).unregisterOnSharedPreferenceChangeListener(prefsListener)
        super.onPause()
    }

    private fun openPermissionScreen() {
        startActivity(Intent(this, PermissionActivity::class.java))
    }

    private fun updateStatus() {
        val serviceEnabled = Prefs.isAccessibilityServiceEnabled(this)
        val toggleOn = Prefs.isAutoSkipEnabled(this)

        val (emoji, title, detail, color) = when {
            !serviceEnabled -> Status("⚠️", R.string.status_permission_needed,
                R.string.status_permission_needed_detail, R.color.status_warning)
            toggleOn -> Status("🟢", R.string.status_active,
                R.string.status_active_detail, R.color.status_active)
            else -> Status("🔴", R.string.status_disabled,
                R.string.status_disabled_detail, R.color.status_disabled)
        }
        statusEmoji.text = emoji
        statusTitle.setText(title)
        statusDetail.setText(detail)
        statusCard.strokeColor = ContextCompat.getColor(this, color)
        grantButton.isVisible = !serviceEnabled
    }

    private fun updateCounter() {
        counterText.text = getString(R.string.skipped_count, Prefs.skippedCount(this))
    }

    private data class Status(val emoji: String, val title: Int, val detail: Int, val color: Int)
}
