package com.nejracoric.securepassandroid.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejracoric.securepassandroid.data.api.PasswordEntryDto
import com.nejracoric.securepassandroid.security.BiometricHelper
import com.nejracoric.securepassandroid.ui.components.CustomSearchBar
import com.nejracoric.securepassandroid.ui.components.PlatformLogo
import com.nejracoric.securepassandroid.ui.components.VaultCard
import com.nejracoric.securepassandroid.ui.theme.DeepBackground
import com.nejracoric.securepassandroid.ui.theme.GlassOverlay
import com.nejracoric.securepassandroid.ui.theme.NeonRed
import com.nejracoric.securepassandroid.ui.theme.PurplePrimary
import com.nejracoric.securepassandroid.ui.theme.TextPrimary
import com.nejracoric.securepassandroid.ui.theme.TextSecondary

@Composable
fun VaultScreen(
    onLogout: () -> Unit = {},
    viewModel: VaultViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadPasswords() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Moj Trezor",
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Meni", tint = TextSecondary)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Osvježi") },
                        onClick = { showMenu = false; viewModel.loadPasswords() },
                    )
                    DropdownMenuItem(
                        text = { Text("Odjavi se", color = NeonRed) },
                        onClick = { showMenu = false; onLogout() },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomSearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::setSearchQuery,
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurplePrimary)
            }
            uiState.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage ?: "", color = TextSecondary)
                    Text(
                        "Pokušaj ponovo",
                        color = PurplePrimary,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable { viewModel.loadPasswords() },
                    )
                }
            }
            uiState.filteredPasswords.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (uiState.searchQuery.isNotBlank()) "Nema rezultata" else "Trezor je prazan.\nGeneriši i sačuvaj lozinku.",
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.filteredPasswords, key = { it.id }) { entry ->
                    VaultCard(entry = entry, onClick = { viewModel.selectEntry(entry) })
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    uiState.selectedEntry?.let { entry ->
        PasswordDetailSheet(
            entry = entry,
            isPasswordRevealed = uiState.isPasswordRevealed,
            onDismiss = viewModel::dismissDetail,
            onRevealRequest = viewModel::revealPassword,
            onHidePassword = viewModel::hidePassword,
            onDelete = { viewModel.deletePassword(entry.id) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordDetailSheet(
    entry: PasswordEntryDto,
    isPasswordRevealed: Boolean,
    onDismiss: () -> Unit,
    onRevealRequest: () -> Unit,
    onHidePassword: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GlassOverlay,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Zatvori", tint = TextSecondary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PlatformLogo(title = entry.title, size = 56)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = entry.title,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = entry.username ?: entry.url ?: "",
                        color = TextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailField(
                label = "Korisničko ime / Email",
                value = entry.username ?: "—",
                onCopy = { entry.username?.let { copyToClipboard(context, it) } },
                showCopy = !entry.username.isNullOrBlank(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailField(
                label = "Lozinka",
                value = if (isPasswordRevealed) entry.password else "•••••••••",
                isMonospace = true,
                trailing = {
                    Row {
                        IconButton(onClick = {
                            if (isPasswordRevealed) onHidePassword() else {
                                BiometricHelper.authenticate(
                                    activity, "Otključaj lozinku", "Potvrdi otiskom prsta",
                                    onSuccess = onRevealRequest,
                                )
                            }
                        }) {
                            Icon(
                                if (isPasswordRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(onClick = {
                            BiometricHelper.authenticate(
                                activity, "Otključaj lozinku", "Potvrdi otiskom prsta",
                                onSuccess = onRevealRequest,
                            )
                        }) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                },
            )

            Text(
                text = "Otisak prsta potreban za dekripciju",
                color = TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionRow(
                icon = Icons.Default.ContentCopy,
                label = "Kopiraj lozinku",
                onClick = {
                    BiometricHelper.authenticate(
                        activity, "Kopiraj lozinku", "Potvrdi identitet",
                        onSuccess = {
                            onRevealRequest()
                            copyToClipboard(context, entry.password)
                        },
                    )
                },
            )
            ActionRow(
                icon = Icons.Default.Edit,
                label = "Uredi zapis",
                onClick = { Toast.makeText(context, "Uskoro dostupno", Toast.LENGTH_SHORT).show() },
            )
            ActionRow(
                icon = Icons.Default.Delete,
                label = "Obriši zapis",
                tint = NeonRed,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    showCopy: Boolean = false,
    onCopy: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DeepBackground)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
            ) { text ->
                Text(
                    text = text,
                    color = TextPrimary,
                    style = if (isMonospace) {
                        androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    } else {
                        androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    },
                )
            }
            trailing?.invoke()
            if (showCopy) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, color = tint, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("SecurePass", text))
    Toast.makeText(context, "Kopirano", Toast.LENGTH_SHORT).show()
}
