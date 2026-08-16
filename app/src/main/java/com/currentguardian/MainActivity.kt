package com.currentguardian

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.currentguardian.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            updateStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usageButton.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            )
        }

        binding.startButton.setOnClickListener {
            startGuardian()
        }

        binding.stopButton.setOnClickListener {
            stopService(
                Intent(this, GuardianService::class.java)
            )
            updateStatus()
        }

        updateStatus()
    }

    private fun startGuardian() {

        if (!hasUsageAccess()) {
            binding.statusText.text =
                "請先開啟「使用狀態存取」權限。"
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        val intent = Intent(
            this,
            GuardianService::class.java
        )

        androidx.core.content.ContextCompat.startForegroundService(
            this,
            intent
        )

        binding.statusText.text = "管家已啟動"
    }

    private fun hasUsageAccess(): Boolean {

        val appOps =
            getSystemService(Context.APP_OPS_SERVICE)
                    as AppOpsManager

        val mode =
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun updateStatus() {

        binding.statusText.text =
            if (hasUsageAccess()) {
                "使用狀態權限：已授權"
            } else {
                "使用狀態權限：尚未授權"
            }
    }
}
