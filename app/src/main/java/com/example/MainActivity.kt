package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MyraMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MyraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MyraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyraMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopSpeaking()
        viewModel.stopListening()
    }
}
