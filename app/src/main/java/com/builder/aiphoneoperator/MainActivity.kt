package com.builder.aiphoneoperator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.builder.aiphoneoperator.data.repository.OperatorRepository
import com.builder.aiphoneoperator.navigation.AppNavHost
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
