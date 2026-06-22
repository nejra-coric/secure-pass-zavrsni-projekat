package com.nejracoric.securepassandroid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.securepassandroid.ui.PasswordUiState
import com.nejracoric.securepassandroid.ui.PasswordViewModel
import com.nejracoric.securepassandroid.ui.theme.SecurePassAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecurePassAndroidTheme {
                SecurePassScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurePassScreen(
    viewModel: PasswordViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("SecurePass Engine") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        SecurePassScreenContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding),
            onPasswordChange = viewModel::setPassword,
            onLengthChange = viewModel::setLength,
            onIncludeSpecialCharsChange = viewModel::setIncludeSpecialChars,
            onGenerateClick = viewModel::generatePassword,
            onSaveClick = viewModel::savePasswordToList,
            onRetryPwnedCheck = viewModel::retryPwnedCheck,
            onCopyClick = { password ->
                copyPasswordToClipboard(context, password)
                Toast.makeText(context, "Lozinka kopirana", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
fun SecurePassScreenContent(
    uiState: PasswordUiState,
    modifier: Modifier = Modifier,
    onPasswordChange: (String) -> Unit = {},
    onLengthChange: (Int) -> Unit = {},
    onIncludeSpecialCharsChange: (Boolean) -> Unit = {},
    onGenerateClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onRetryPwnedCheck: () -> Unit = {},
    onCopyClick: (String) -> Unit = {},
) {
    val entropyColor = entropyColor(uiState.entropyBits)
    val entropyProgress = (uiState.entropyBits / 100.0).coerceIn(0.0, 1.0).toFloat()
    val bruteForce = bruteForceEstimate(uiState.entropyBits, uiState.password.isNotEmpty())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Lozinka") },
            placeholder = { Text("Unesi ili generiši lozinku") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            trailingIcon = {
                IconButton(
                    onClick = { onCopyClick(uiState.password) },
                    enabled = uiState.password.isNotEmpty(),
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopiraj lozinku",
                    )
                }
            },
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Dužina lozinke",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${uiState.length}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = uiState.length.toFloat(),
                onValueChange = { onLengthChange(it.toInt()) },
                valueRange = 8f..32f,
                steps = 23,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("8", style = MaterialTheme.typography.labelSmall)
                Text("32", style = MaterialTheme.typography.labelSmall)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Specijalni karakteri",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "!@#\$%^&* i sl.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(
                checked = uiState.includeSpecialChars,
                onCheckedChange = onIncludeSpecialCharsChange,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Jačina lozinke (entropija)",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = if (uiState.password.isEmpty()) {
                        "—"
                    } else {
                        String.format("%.1f bita", uiState.entropyBits)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = entropyColor,
                )
            }
            LinearProgressIndicator(
                progress = { entropyProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = entropyColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = entropyLabel(uiState.entropyBits, uiState.password.isNotEmpty()),
                style = MaterialTheme.typography.bodySmall,
                color = entropyColor,
            )

            if (uiState.password.isNotEmpty()) {
                Text(
                    text = "Procijenjeno vrijeme za provaljivanje",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = bruteForce.first,
                    style = MaterialTheme.typography.bodyMedium,
                    color = bruteForce.second,
                )

                PwnedStatusSection(
                    isCheckingPwned = uiState.isCheckingPwned,
                    pwnedCount = uiState.pwnedCount,
                    pwnedCheckFailed = uiState.pwnedCheckFailed,
                    pwnedCheckFailedMessage = uiState.pwnedCheckFailedMessage,
                    onRetryClick = onRetryPwnedCheck,
                )
            }
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onGenerateClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generiši lozinku")
        }

        OutlinedButton(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.password.isNotEmpty(),
        ) {
            Text("Sačuvaj trenutnu lozinku")
        }

        SessionHistorySection(
            savedPasswords = uiState.savedPasswords,
            averageSessionEntropy = uiState.averageSessionEntropy,
            onCopyClick = onCopyClick,
        )
    }
}

@Composable
private fun PwnedStatusSection(
    isCheckingPwned: Boolean,
    pwnedCount: Int,
    pwnedCheckFailed: Boolean,
    pwnedCheckFailedMessage: String?,
    onRetryClick: () -> Unit,
) {
    Spacer(modifier = Modifier.height(4.dp))

    when {
        isCheckingPwned -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Provjera u Have I Been Pwned bazi...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        pwnedCheckFailed -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = pwnedCheckFailedMessage
                        ?: "Provjera protiv HIBP baze trenutno nije dostupna.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRetryClick) {
                    Text("Pokušaj ponovo")
                }
            }
        }

        pwnedCount > 0 -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                    )
                    Text(
                        text = "⚠️ Ova lozinka je PROVALJENA i pronađena $pwnedCount puta u bazama procurjelih podataka! Nemojte je koristiti.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828),
                    )
                }
            }
        }

        else -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                )
                Text(
                    text = "✅ Ova lozinka nije pronađena u poznatim bazama procurjelih podataka.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32),
                )
            }
        }
    }
}

@Composable
private fun SessionHistorySection(
    savedPasswords: List<String>,
    averageSessionEntropy: Double,
    onCopyClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sačuvane lozinke u sesiji",
                style = MaterialTheme.typography.titleMedium,
            )
            if (savedPasswords.isNotEmpty()) {
                Text(
                    text = String.format("Prosječna entropija: %.1f bita", averageSessionEntropy),
                    style = MaterialTheme.typography.labelMedium,
                    color = entropyColor(averageSessionEntropy),
                )
            }
        }

        if (savedPasswords.isEmpty()) {
            Text(
                text = "Još nema sačuvanih lozinki u ovoj sesiji.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            savedPasswords.forEachIndexed { index, password ->
                SavedPasswordCard(
                    index = index + 1,
                    password = password,
                    onCopyClick = { onCopyClick(password) },
                )
            }
        }
    }
}

@Composable
private fun SavedPasswordCard(
    index: Int,
    password: String,
    onCopyClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lozinka #$index",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = password,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onCopyClick) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Kopiraj sačuvanu lozinku",
                )
            }
        }
    }
}

private fun entropyColor(entropyBits: Double): Color = when {
    entropyBits < 40 -> Color(0xFFE53935)
    entropyBits <= 70 -> Color(0xFFFFB300)
    else -> Color(0xFF43A047)
}

private fun entropyLabel(entropyBits: Double, hasPassword: Boolean): String {
    if (!hasPassword) return "Unesi ili generiši lozinku da vidiš procjenu jačine"
    return when {
        entropyBits < 40 -> "Slaba — povećaj dužinu ili uključi više tipova karaktera"
        entropyBits <= 70 -> "Srednja — prihvatljiva za većinu naloga"
        else -> "Jaka — visoka entropija"
    }
}

private fun bruteForceEstimate(entropyBits: Double, hasPassword: Boolean): Pair<String, Color> {
    if (!hasPassword) {
        return "" to Color.Unspecified
    }
    return when {
        entropyBits < 40 -> "Slaba (Provaljivo u nekoliko sekundi)" to Color(0xFFE53935)
        entropyBits <= 70 -> "Srednja (Potrebno nekoliko mjeseci/godina)" to Color(0xFFFF9800)
        else -> "Kriptografski sigurna (Potrebne su milijarde godina)" to Color(0xFF43A047)
    }
}

private fun copyPasswordToClipboard(context: Context, password: String) {
    if (password.isEmpty()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("SecurePass", password))
}

@Preview(showBackground = true)
@Composable
fun SecurePassScreenPreview() {
    SecurePassAndroidTheme {
        SecurePassScreenContent(
            uiState = PasswordUiState(
                password = "K9#mPx2vLq@nRw5!",
                length = 16,
                includeSpecialChars = true,
                entropyBits = 95.4,
                savedPasswords = listOf("Abc123!", "K9#mPx2vLq@nRw5!"),
                averageSessionEntropy = 72.5,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecurePassScreenWeakPreview() {
    SecurePassAndroidTheme {
        SecurePassScreenContent(
            uiState = PasswordUiState(
                password = "abc123",
                length = 8,
                includeSpecialChars = false,
                entropyBits = 28.5,
            ),
        )
    }
}
