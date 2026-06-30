package com.nejracoric.securepassandroid.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.securepassandroid.security.BiometricHelper
import com.nejracoric.securepassandroid.ui.components.AuthFieldType
import com.nejracoric.securepassandroid.ui.components.AuthTextField
import com.nejracoric.securepassandroid.ui.components.AuthWaveDecoration
import com.nejracoric.securepassandroid.ui.components.GradientButton
import com.nejracoric.securepassandroid.ui.components.LockIconCircle
import com.nejracoric.securepassandroid.ui.components.TextLinkButton
import com.nejracoric.securepassandroid.ui.theme.ButtonGradient
import com.nejracoric.securepassandroid.ui.theme.DeepBackground
import com.nejracoric.securepassandroid.ui.theme.PurpleAccent
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val canUseBiometric = BiometricHelper.canAuthenticate(activity) && uiState.hasStoredSession

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            LockIconCircle()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Light)) {
                        append("Secure")
                    }
                    withStyle(SpanStyle(color = PurplePrimary, fontWeight = FontWeight.Bold)) {
                        append("Pass")
                    }
                },
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            )

            Text(
                text = "Vaše lozinke. Vaša sigurnost.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 36.dp),
            )

            AuthTextField(
                value = uiState.email,
                onValueChange = viewModel::setEmail,
                fieldType = AuthFieldType.Email,
                placeholder = "Email adresa",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                value = uiState.password,
                onValueChange = viewModel::setPassword,
                fieldType = AuthFieldType.Password,
                placeholder = "Lozinka",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.submit(onAuthenticated) },
                ),
            )

            if (!uiState.isRegisterMode) {
                TextLinkButton(
                    text = "Zaboravili ste lozinku?",
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 12.dp),
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = com.nejracoric.securepassandroid.ui.theme.NeonRed,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = PurplePrimary, modifier = Modifier.size(48.dp))
            } else {
                GradientButton(
                    text = if (uiState.isRegisterMode) "Registruj se" else "Prijavi se",
                    onClick = { viewModel.submit(onAuthenticated) },
                    brush = ButtonGradient,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isRegisterMode) {
                    "Već imate nalog? Prijavite se"
                } else {
                    "Nemate nalog? Registrujte se"
                },
                color = PurpleAccent,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { viewModel.toggleMode() },
            )

            AnimatedVisibility(
                visible = canUseBiometric,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFF2A2A36)),
                        )
                        Text(
                            text = "ILI",
                            color = TextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFF2A2A36)),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape, ambientColor = PurplePrimary.copy(0.4f))
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(PurplePrimary.copy(0.3f), Color(0xFF1A1A26)),
                                ),
                                shape = CircleShape,
                            )
                            .border(1.dp, PurplePrimary.copy(0.5f), CircleShape)
                            .clickable {
                                BiometricHelper.authenticate(
                                    activity = activity,
                                    title = "Brza prijava",
                                    subtitle = uiState.storedEmail?.let { "Dobrodošli, $it" } ?: "Potvrdi identitet",
                                    onSuccess = { viewModel.biometricLogin(onAuthenticated) },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometrijski login",
                            tint = PurplePrimary,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            AuthWaveDecoration()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
