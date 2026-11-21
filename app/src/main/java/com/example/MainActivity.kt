package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.navigation.SmartBusinessNavGraph
import com.example.ui.components.FloatingToastBanner
import com.example.ui.state.MainViewModel
import com.example.ui.theme.SmartBusinessTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val toast by viewModel.toastMessage.collectAsStateWithLifecycle()

            SmartBusinessTheme(darkTheme = state.session.darkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SmartBusinessNavGraph(
                        state = state,
                        viewModel = viewModel,
                    )
                    FloatingToastBanner(
                        message = toast,
                        onDismiss = { viewModel.dismissToast() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding(),
                    )
                }
            }
        }
    }
}
