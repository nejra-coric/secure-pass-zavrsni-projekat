package com.nejracoric.securepassandroid.ui.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.securepassandroid.ui.components.AuthFieldType
import com.nejracoric.securepassandroid.ui.components.AuthTextField
import com.nejracoric.securepassandroid.ui.components.CircularStrengthGauge
import com.nejracoric.securepassandroid.ui.components.ColoredPasswordText
import com.nejracoric.securepassandroid.ui.components.CrackTimeInfoBox
import com.nejracoric.securepassandroid.ui.components.GradientButton
import com.nejracoric.securepassandroid.ui.components.PurpleCheckbox
import com.nejracoric.securepassandroid.ui.components.TextLinkButton
import com.nejracoric.securepassandroid.ui.theme.ButtonGradient
import com.nejracoric.securepassandroid.ui.theme.DeepBackground
import com.nejracoric.securepassandroid.ui.theme.GlassOverlay
import com.nejracoric.securepassandroid.ui.theme.NeonGreen
import com.nejracoric.securepassandroid.ui.theme.NeonRed
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.SurfaceCard
import com.nejracoric.securepassandroid.ui.theme.TextPrimary
import com.nejracoric.securepassandroid.ui.theme.TextSecondary

@Composable
fun GeneratorScreen(
    onPasswordSaved: () -> Unit,
    viewModel: GeneratorViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Generator",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Kreiraj jaku i sigurnu lozinku",
            color = TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        PasswordDisplayCard(
            password = uiState.password,
            onCopy = { copyPassword(context, uiState.password) },
            onRefresh = viewModel::generatePassword,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DUŽINA LOZINKE: ${uiState.length}",
            color = TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = uiState.length.toFloat(),
            onValueChange = { viewModel.setLength(it.toInt()) },
            valueRange = 8f..32f,
            steps = 23,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = PurplePrimary,
                inactiveTrackColor = SurfaceCard,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "UKLJUČI KARAKTERE",
            color = TextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        PurpleCheckbox("Velika slova", uiState.includeUppercase, viewModel::setIncludeUppercase)
        PurpleCheckbox("Mala slova", uiState.includeLowercase, viewModel::setIncludeLowercase)
        PurpleCheckbox("Brojevi", uiState.includeNumbers, viewModel::setIncludeNumbers)
        PurpleCheckbox("Specijalni", uiState.includeSpecialChars, viewModel::setIncludeSpecialChars)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (uiState.password.isNotEmpty()) {
                    PwnedStatus(
                        isChecking = uiState.isCheckingPwned,
                        pwnedCount = uiState.pwnedCount,
                        failed = uiState.pwnedCheckFailed,
                        failedMessage = uiState.pwnedCheckFailedMessage,
                        onRetry = viewModel::retryPwnedCheck,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CrackTimeInfoBox(uiState.entropyBits, true)
                }
            }
            CircularStrengthGauge(
                entropyBits = uiState.entropyBits,
                hasPassword = uiState.password.isNotEmpty(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Generiši lozinku",
            onClick = viewModel::generatePassword,
            brush = Brush.horizontalGradient(listOf(PurplePrimary, com.nejracoric.securepassandroid.ui.theme.BlueNeon)),
        )

        Spacer(modifier = Modifier.height(12.dp))

        GradientButton(
            text = "Spasi u Trezor",
            onClick = viewModel::showSaveSheet,
            enabled = uiState.password.isNotEmpty(),
            brush = ButtonGradient,
        )

        if (uiState.saveSuccess) {
            Text(
                text = "✓ Lozinka uspješno sačuvana",
                color = NeonGreen,
                modifier = Modifier.padding(top = 12.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        }

        uiState.errorMessage?.let {
            Text(it, color = NeonRed, modifier = Modifier.padding(top = 8.dp), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (uiState.showSaveSheet) {
        SaveToVaultDialog(
            title = uiState.saveTitle,
            username = uiState.saveUsername,
            isSaving = uiState.isSaving,
            onTitleChange = viewModel::setSaveTitle,
            onUsernameChange = viewModel::setSaveUsername,
            onDismiss = viewModel::dismissSaveSheet,
            onSave = { viewModel.saveToVault(onPasswordSaved) },
        )
    }
}

@Composable
private fun PasswordDisplayCard(
    password: String,
    onCopy: () -> Unit,
    onRefresh: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, Color(0xFF2E2E3A), shape)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (password.isEmpty()) {
                Text("Pritisni generiši...", color = TextSecondary, modifier = Modifier.weight(1f))
            } else {
                ColoredPasswordText(password = password, modifier = Modifier.weight(1f))
            }
            IconButton(onClick = onCopy, enabled = password.isNotEmpty()) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun PwnedStatus(
    isChecking: Boolean,
    pwnedCount: Int,
    failed: Boolean,
    failedMessage: String?,
    onRetry: () -> Unit,
) {
    when {
        isChecking -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = PurplePrimary)
            Text("  HIBP provjera...", color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        failed -> Column {
            Text(failedMessage ?: "", color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Text("Pokušaj ponovo", color = PurplePrimary, modifier = Modifier.padding(top = 4.dp).clickable(onClick = onRetry))
        }
        pwnedCount > 0 -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = NeonRed, modifier = Modifier.size(16.dp))
            Text(" Provaljena u HIBP!", color = NeonRed, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        else -> Text("✓ Nije u HIBP bazi", color = NeonGreen, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SaveToVaultDialog(
    title: String,
    username: String,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassOverlay)
                .border(1.dp, PurplePrimary.copy(0.3f), RoundedCornerShape(24.dp))
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔐",
                    fontSize = 40.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = "Spasi u Trezor",
                    color = TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = "Unesite naziv platforme",
                    color = TextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                )

                AuthTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    fieldType = AuthFieldType.Platform,
                    placeholder = "npr. Binance, Twitter, Shopify...",
                )

                Spacer(modifier = Modifier.height(12.dp))

                AuthTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    fieldType = AuthFieldType.Email,
                    placeholder = "Korisničko ime / email (opciono)",
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isSaving) {
                    CircularProgressIndicator(color = PurplePrimary)
                } else {
                    GradientButton(text = "Spasi", onClick = onSave, brush = ButtonGradient)
                }

                TextLinkButton(
                    text = "Otkaži",
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 16.dp),
                    color = TextSecondary,
                )
            }
        }
    }
}

private fun copyPassword(context: Context, password: String) {
    if (password.isEmpty()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("SecurePass", password))
    Toast.makeText(context, "Kopirano", Toast.LENGTH_SHORT).show()
}
