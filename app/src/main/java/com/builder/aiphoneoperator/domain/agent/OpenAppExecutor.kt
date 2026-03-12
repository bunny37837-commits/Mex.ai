package com.builder.aiphoneoperator.domain.agent

import android.content.Context
import android.content.Intent

object OpenAppExecutor {
    fun execute(context: Context, action: ExecutionAction.OpenApp): ExecutionResult {
        val packageName = action.resolvedPackageName
            ?: return ExecutionResult.Failure("App package was not resolved.")
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return ExecutionResult.Failure("App was found but could not be launched.")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(launchIntent)
            ExecutionResult.Success("Opened ${action.resolvedLabel ?: packageName}.")
        } catch (t: Throwable) {
            ExecutionResult.Failure("Failed to launch ${action.resolvedLabel ?: packageName}: ${t.message ?: "unknown error"}")
        }
    }
}
