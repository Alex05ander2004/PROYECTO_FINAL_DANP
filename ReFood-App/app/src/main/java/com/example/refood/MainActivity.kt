package com.example.refood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.refood.navigation.ReFoodNavGraph
import com.example.refood.ui.theme.ReFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ReFoodApplication).container
        setContent {
            ReFoodTheme {
                ReFoodNavGraph(container = container)
            }
        }
    }
}
