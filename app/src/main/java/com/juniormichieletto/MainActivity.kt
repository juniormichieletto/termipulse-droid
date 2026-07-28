package com.juniormichieletto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.juniormichieletto.ui.terminal.MainTerminalScreen
import com.juniormichieletto.ui.terminal.TerminalViewModel
import com.juniormichieletto.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainTerminalScreen(viewModel = viewModel)
            }
        }
    }
}

