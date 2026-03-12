package com.builder.aiphoneoperator.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.builder.aiphoneoperator.model.RepairRequirement

object SystemSettingsNavigator {
    fun openFixPath(context: Context, requirement: RepairRequirement) {
        val intent = when (requirement) {
            RepairRequirement.ACCESSIBILITY_SERVICE -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            RepairRequirement.NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context))
            RepairRequirement.BATTERY_OPTIMIZATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri(context))
            } else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context))
            RepairRequirement.AUTOSTART -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context))
            RepairRequirement.AI_SERVICE_RUNNING -> Intent(context, com.builder.aiphoneoperator.MainActivity::class.java)
        }.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    private fun packageUri(context: Context): Uri = Uri.parse("package:${context.packageName}")
}
