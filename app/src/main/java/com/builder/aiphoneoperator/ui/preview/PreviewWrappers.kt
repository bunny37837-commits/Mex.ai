package com.builder.aiphoneoperator.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.builder.aiphoneoperator.ui.theme.AiPhoneOperatorTheme

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    AiPhoneOperatorTheme(content = content)
}

@Preview(showBackground = true)
annotation class LightPreview
