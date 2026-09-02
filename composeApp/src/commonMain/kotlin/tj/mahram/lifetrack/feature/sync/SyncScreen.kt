package tj.mahram.lifetrack.feature.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.getKoin
import tj.mahram.lifetrack.core.i18n.SyncStrings
import tj.mahram.lifetrack.core.i18n.syncStringsFor
import tj.mahram.lifetrack.domain.model.AuthState
import tj.mahram.lifetrack.ui.components.GradientButton
import tj.mahram.lifetrack.ui.components.brandVividGradient
import tj.mahram.lifetrack.ui.components.glassSurface

class SyncScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val koin = getKoin()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<SyncScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        val s = syncStringsFor(state.language)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(s.screenTitle, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                when (val auth = state.authState) {
                    is AuthState.SignedIn -> SignedInCard(
                        email = auth.email,
                        strings = s,
                        running = state.status.running,
                        lastSyncedAt = state.status.lastSyncedAt,
                        lastError = state.status.lastError,
                        onSyncNow = screenModel::syncNow,
                        onSignOut = screenModel::signOut
                    )
                    AuthState.SignedOut -> SignedOutCard(
                        strings = s,
                        busy = state.busy,
                        error = localizedError(state.error, s),
                        configured = state.configured,
                        onSignIn = screenModel::signIn,
                        onSignUp = screenModel::signUp,
                        onTyping = screenModel::clearError
                    )
                }
            }
        }
    }
}

private fun localizedError(error: String?, s: SyncStrings): String? = when (error) {
    null -> null
    SyncScreenModel.EMPTY_FIELDS -> s.emptyFields
    else -> error
}

@Composable
private fun SignedOutCard(
    strings: SyncStrings,
    busy: Boolean,
    error: String?,
    configured: Boolean,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onTyping: () -> Unit
) {
    var signUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(brandVividGradient()),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White) }
            Column {
                Text(strings.headerSignedOut, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            strings.explainer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; onTyping() },
            label = { Text(strings.emailLabel) },
            placeholder = { Text(strings.emailHint) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onTyping() },
            label = { Text(strings.passwordLabel) },
            placeholder = { Text(strings.passwordHint) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (!configured) {
            Text(strings.notConfigured, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GradientButton(
                text = if (signUpMode) strings.signUp else strings.signIn,
                onClick = {
                    if (signUpMode) onSignUp(email.trim(), password) else onSignIn(email.trim(), password)
                },
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp,
                enabled = !busy && configured
            )
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }

        TextButton(
            onClick = { signUpMode = !signUpMode; onTyping() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (signUpMode) strings.toggleToSignIn else strings.toggleToSignUp)
        }
    }
}

@Composable
private fun SignedInCard(
    email: String,
    strings: SyncStrings,
    running: Boolean,
    lastSyncedAt: Long?,
    lastError: String?,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(brandVividGradient()),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White) }
            Column(modifier = Modifier.weight(1f)) {
                Text(strings.signedInAs, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (running) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(strings.syncing, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    strings.lastSyncedText(lastSyncedAt?.let { formatClock(it) } ?: strings.never),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (lastError != null && !running) {
            Text(lastError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        GradientButton(
            text = strings.syncNow,
            onClick = onSyncNow,
            modifier = Modifier.fillMaxWidth(),
            height = 50.dp,
            enabled = !running
        )
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text(strings.signOut)
        }
    }
}

private fun formatClock(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    return "$h:$m"
}
