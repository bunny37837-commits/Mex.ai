package com.builder.aiphoneoperator.domain.agent

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object OpenAppResolver {
    fun resolve(context: Context, query: String): ExecutionAction.OpenApp? {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val normalized = query.trim().lowercase()

        val match = apps.firstOrNull { info ->
            val label = info.loadLabel(packageManager).toString().lowercase()
            val packageName = info.activityInfo.packageName.lowercase()
            label.contains(normalized) || packageName.contains(normalized)
        } ?: return null

        return ExecutionAction.OpenApp(
            query = query,
            resolvedPackageName = match.activityInfo.packageName,
            resolvedLabel = match.loadLabel(packageManager).toString(),
        )
    }
}
