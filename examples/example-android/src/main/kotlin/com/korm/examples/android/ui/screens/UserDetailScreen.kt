// examples/example-android/src/main/kotlin/com/korm/examples/android/ui/screens/UserDetailScreen.kt

package com.korm.examples.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.korm.examples.android.data.local.entities.UserEntity
import com.korm.examples.android.ui.viewmodels.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * User Detail Screen using Jetpack Compose.
 *
 * Displays detailed information about a user and allows editing.
 * All data is fetched and updated using KORM-DSL through the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Long,
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.selectedUser.observeAsState()
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.selectUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit User" else "User Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (user != null) {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                if (isEditing) "Cancel" else "Edit"
                            )
                        }

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    ErrorMessage(
                        message = error ?: "Unknown error",
                        onRetry = { viewModel.selectUser(userId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                user == null -> {
                    EmptyState(
                        message = "User not found",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    if (isEditing) {
                        UserEditForm(
                            user = user!!,
                            onSave = { updatedUser ->
                                viewModel.updateUser(updatedUser)
                                isEditing = false
                            },
                            onCancel = { isEditing = false }
                        )
                    } else {
                        UserDetailContent(
                            user = user!!,
                            onToggleStatus = { viewModel.toggleUserStatus(user!!.id) }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete this user? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(userId)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserDetailContent(
    user: UserEntity,
    onToggleStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (user.isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.first().uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = if (user.isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Badge
        Badge(
            containerColor = if (user.isActive) {
                Color(0xFF4CAF50)
            } else {
                Color(0xFFF44336)
            }
        ) {
            Text(
                text = if (user.isActive) "Active" else "Inactive",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user.email
                )

                DetailRow(
                    icon = Icons.Default.Cake,
                    label = "Age",
                    value = "${user.age} years"
                )

                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Created",
                    value = formatDate(user.createdAt)
                )

                DetailRow(
                    icon = Icons.Default.Update,
                    label = "Updated",
                    value = formatDate(user.updatedAt)
                )

                DetailRow(
                    icon = Icons.Default.Fingerprint,
                    label = "ID",
                    value = user.id.toString()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Status Button
        Button(
            onClick = onToggleStatus,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (user.isActive) {
                    Color(0xFFF44336)
                } else {
                    Color(0xFF4CAF50)
                }
            )
        ) {
            Icon(
                if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (user.isActive) "Deactivate User" else "Activate User")
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditForm(
    user: UserEntity,
    onSave: (UserEntity) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var age by remember { mutableStateOf(user.age.toString()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = if (it.isBlank()) "Name is required" else null
            },
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = when {
                    it.isBlank() -> "Email is required"
                    !it.contains("@") -> "Invalid email format"
                    else -> null
                }
            },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Age Field
        OutlinedTextField(
            value = age,
            onValueChange = {
                age = it
                ageError = when {
                    it.isBlank() -> "Age is required"
                    it.toIntOrNull() == null -> "Age must be a number"
                    it.toInt() < 0 || it.toInt() > 150 -> "Age must be between 0 and 150"
                    else -> null
                }
            },
            label = { Text("Age") },
            leadingIcon = { Icon(Icons.Default.Cake, null) },
            isError = ageError != null,
            supportingText = ageError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (nameError == null && emailError == null && ageError == null) {
                        val updatedUser = user.copy(
                            name = name,
                            email = email,
                            age = age.toInt(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updatedUser)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = nameError == null && emailError == null && ageError == null
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
