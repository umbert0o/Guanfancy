package com.guanfancy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.guanfancy.app.ui.navigation.GuanfancyNavHost
import com.guanfancy.app.ui.theme.GuanfancyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuanfancyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GuanfancyNavHost()
                }
            }
        }
    }
}
