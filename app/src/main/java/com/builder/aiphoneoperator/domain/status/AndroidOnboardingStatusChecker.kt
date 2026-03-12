package com.builder.aiphoneoperator.domain.status

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.builder.aiphoneoperator.runtime.OnboardingStatus

class AndroidOnboardingStatusChecker(
    private val context: Context,
) : OnboardingStatusChecker {
    override suspend fun isOnboardingComplete(): Boolean = getStatus().isComplete

    override suspend fun getStatus(): OnboardingStatus {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return OnboardingStatus(
            accessibilityEnabled = isAccessibilityEnabled(context),
            notificationPermissionGranted = isNotificationPermissionGranted(context),
            batteryOptimizationDisabled = powerManager.isIgnoringBatteryOptimizations(context.packageName),
            hyperOsAutostartEnabled = isHyperOsAutostartLikelyEnabled(context),
        )
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        return enabledServices.contains(context.packageName, ignoreCase = true)
    }

    private fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isHyperOsAutostartLikelyEnabled(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        if (!manufacturer.contains("xiaomi", ignoreCase = true)) return true
        val prefs = context.getSharedPreferences("operator_runtime", Context.MODE_PRIVATE)
        return prefs.getBoolean("hyperos_autostart_enabled", false)
    }
}
