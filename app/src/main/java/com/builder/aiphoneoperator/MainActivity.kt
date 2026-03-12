package com.builder.aiphoneoperator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.navigation.AppNavHost
import com.builder.aiphoneoperator.service.OperatorForegroundService
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceIntent = Intent(this, OperatorForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent) else startService(serviceIntent)
        setContent {
            AiPhoneOperatorTheme {
                Surface {
                    AppNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            OperatorRepository.refreshStatuses(this@MainActivity)
        }
    }
}
