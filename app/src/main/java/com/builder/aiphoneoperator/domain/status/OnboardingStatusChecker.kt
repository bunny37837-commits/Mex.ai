package com.builder.aiphoneoperator.domain.status

import com.builder.aiphoneoperator.runtime.OnboardingStatus

interface OnboardingStatusChecker {
    suspend fun isOnboardingComplete(): Boolean
    suspend fun getStatus(): OnboardingStatus
}
