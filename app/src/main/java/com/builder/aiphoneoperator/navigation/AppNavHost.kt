package com.builder.aiphoneoperator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.domain.usecase.RequirementFixUseCase
import com.builder.aiphoneoperator.ui.common.RefreshOnResume
import com.builder.aiphoneoperator.ui.screen.capabilities.CapabilitiesScreen
import com.builder.aiphoneoperator.ui.screen.capabilities.CapabilitiesViewModel
import com.builder.aiphoneoperator.ui.screen.emergencycontrols.EmergencyControlsScreen
import com.builder.aiphoneoperator.ui.screen.emergencycontrols.EmergencyControlsViewModel
import com.builder.aiphoneoperator.ui.screen.history.HistoryScreen
import com.builder.aiphoneoperator.ui.screen.history.HistoryViewModel
import com.builder.aiphoneoperator.ui.screen.home.HomeScreen
import com.builder.aiphoneoperator.ui.screen.home.HomeViewModel
import com.builder.aiphoneoperator.ui.screen.memory.MemoryScreen
import com.builder.aiphoneoperator.ui.screen.memory.MemoryViewModel
import com.builder.aiphoneoperator.ui.screen.onboarding.OnboardingScreen
import com.builder.aiphoneoperator.ui.screen.onboarding.OnboardingViewModel
import com.builder.aiphoneoperator.ui.screen.repairmode.RepairModeScreen
import com.builder.aiphoneoperator.ui.screen.repairmode.RepairModeViewModel
import com.builder.aiphoneoperator.ui.screen.runningtask.RunningTaskScreen
import com.builder.aiphoneoperator.ui.screen.runningtask.RunningTaskViewModel
import com.builder.aiphoneoperator.ui.screen.servicestatus.ServiceStatusScreen
import com.builder.aiphoneoperator.ui.screen.servicestatus.ServiceStatusViewModel
import com.builder.aiphoneoperator.ui.screen.settings.SettingsScreen
import com.builder.aiphoneoperator.ui.screen.settings.SettingsViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestination.Onboarding.route,
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        OperatorRepository.initialize(context)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(AppDestination.Onboarding.route) {
            val viewModel: OnboardingViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()
            val repairStatus by viewModel.repairStatus.collectAsState()
            RefreshOnResume { viewModel.refresh(context) }
            OnboardingScreen(
                state = viewModel.state,
                appState = appState,
                requirements = repairStatus,
                onRefresh = { viewModel.refresh(context) },
                onFixRequirement = {
                    RequirementFixUseCase.execute(context, it.requirement)
                    viewModel.refresh(context)
                },
                onAcknowledgeAutostart = viewModel::acknowledgeAutostart,
                onContinue = { navController.navigate(AppDestination.Home.route) }
            )
        }
        composable(AppDestination.Home.route) {
            val viewModel: HomeViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()
            HomeScreen(
                state = viewModel.state,
                appState = appState,
                onOpenRunningTask = {
                    viewModel.onCommandSubmitted("Command submitted from Home")
                    navController.navigate(AppDestination.RunningTask.route)
                },
                onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                onOpenServiceStatus = { navController.navigate(AppDestination.ServiceStatus.route) },
                onOpenHistory = { navController.navigate(AppDestination.History.route) },
                onOpenMemory = { navController.navigate(AppDestination.Memory.route) },
                onOpenCapabilities = { navController.navigate(AppDestination.Capabilities.route) },
                onOpenEmergencyControls = { navController.navigate(AppDestination.EmergencyControls.route) },
                onOpenRepairMode = { navController.navigate(AppDestination.RepairMode.route) },
            )
        }
        composable(AppDestination.RunningTask.route) {
            val viewModel: RunningTaskViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()
            RunningTaskScreen(
                state = viewModel.state,
                appState = appState,
                onPause = viewModel::onPause,
                onStop = viewModel::onStop,
                onCancel = viewModel::onCancel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.RepairMode.route) {
            val viewModel: RepairModeViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()
            val repairStatus by viewModel.repairStatus.collectAsState()
            RefreshOnResume { viewModel.refresh(context) }
            RepairModeScreen(
                state = viewModel.state,
                appState = appState,
                requirements = repairStatus,
                onRefresh = { viewModel.refresh(context) },
                onFixRequirement = {
                    RequirementFixUseCase.execute(context, it.requirement)
                    viewModel.refresh(context)
                },
                onAcknowledgeAutostart = viewModel::acknowledgeAutostart,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Settings.route) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                state = viewModel.state,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.ServiceStatus.route) {
            val viewModel: ServiceStatusViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()
            RefreshOnResume { viewModel.refresh(context) }
            ServiceStatusScreen(
                state = viewModel.state,
                appState = appState,
                onRefresh = { viewModel.refresh(context) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.History.route) {
            val viewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                state = viewModel.state,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Memory.route) {
            val viewModel: MemoryViewModel = viewModel()
            MemoryScreen(
                state = viewModel.state,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Capabilities.route) {
            val viewModel: CapabilitiesViewModel = viewModel()
            CapabilitiesScreen(
                state = viewModel.state,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.EmergencyControls.route) {
            val viewModel: EmergencyControlsViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()
            EmergencyControlsScreen(
                state = viewModel.state,
                appState = appState,
                onStopAll = viewModel::stopAll,
                onPauseCurrent = viewModel::pauseCurrent,
                onCancelCurrent = viewModel::cancelCurrent,
                onSetSafetyLock = viewModel::setSafetyLock,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
