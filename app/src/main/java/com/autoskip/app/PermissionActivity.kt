package com.autoskip.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

/** First-run screen explaining which permission AutoSkip needs and why. */
class PermissionActivity : AppCompatActivity() {

    private lateinit var grantedText: TextView
    private lateinit var openSettingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        grantedText = findViewById(R.id.permission_granted_text)
        openSettingsButton = findViewById(R.id.button_open_accessibility)

        openSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.button_app_info).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
        findViewById<Button>(R.id.button_continue).setOnClickListener {
            Prefs.setOnboardingDone(this)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val granted = Prefs.isAccessibilityServiceEnabled(this)
        grantedText.isVisible = granted
        openSettingsButton.isVisible = !granted
    }
}
