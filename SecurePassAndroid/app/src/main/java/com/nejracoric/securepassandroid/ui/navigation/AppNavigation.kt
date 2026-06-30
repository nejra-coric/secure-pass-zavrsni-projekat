package com.nejracoric.securepassandroid.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.securepassandroid.data.local.TokenManager
import com.nejracoric.securepassandroid.data.repository.AuthRepository
import com.nejracoric.securepassandroid.ui.auth.AuthScreen
import com.nejracoric.securepassandroid.ui.components.FloatingBottomNav
import com.nejracoric.securepassandroid.ui.components.MainTab
import com.nejracoric.securepassandroid.ui.generator.GeneratorScreen
import com.nejracoric.securepassandroid.ui.theme.DeepBackground
import com.nejracoric.securepassandroid.ui.vault.VaultScreen
import com.nejracoric.securepassandroid.ui.vault.VaultViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }

    if (!isAuthenticated) {
        AuthScreen(onAuthenticated = { isAuthenticated = true })
    } else {
        MainScreen(onLogout = { isAuthenticated = false })
    }
}

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Vault) }
    val vaultViewModel: VaultViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember {
        AuthRepository(TokenManager(context.applicationContext))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 4 } + fadeOut())
                }
            },
            label = "tabTransition",
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            when (tab) {
                MainTab.Vault -> VaultScreen(
                    viewModel = vaultViewModel,
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                            onLogout()
                        }
                    },
                )
                MainTab.Generator -> GeneratorScreen(
                    onPasswordSaved = {
                        vaultViewModel.loadPasswords()
                        selectedTab = MainTab.Vault
                    },
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        ) {
            FloatingBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}
