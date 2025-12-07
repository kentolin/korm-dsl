// examples/example-android/src/main/kotlin/com/korm/examples/android/ui/screens/AddUserScreen.kt

package com.korm.examples.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.korm.examples.android.ui.viewmodels.UserViewModel

/**
 * Add User Screen using Jetpack Compose.
 *
 * Demonstrates form validation and user creation using KORM-DSL.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    val loading by viewModel.loading.observeAsState(false)
    val message by viewModel.message.observeAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    // Show success message and navigate back
    LaunchedEffect(message) {
        if (message?.contains("created successfully") == true) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add User") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create a new user",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Fill in the details below to create a new user in the database using KORM-DSL.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = when {
                        it.isBlank() -> "Name is required"
                        it.length < 2 -> "Name must be at least 2 characters"
                        it.length > 100 -> "Name must not exceed 100 characters"
                        else -> null
                    }
                },
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !loading
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = when {
                        it.isBlank() -> "Email is required"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() ->
                            "Invalid email format"
                        else -> null
                    }
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !loading
            )

            // Age Field
            OutlinedTextField(
                value = age,
                onValueChange = {
                    age = it
                    ageError = when {
                        it.isBlank() -> "Age is required"
                        it.toIntOrNull() == null -> "Age must be a valid number"
                        it.toInt() < 1 -> "Age must be at least 1"
                        it.toInt() > 150 -> "Age must not exceed 150"
                        else -> null
                    }
                },
                label = { Text("Age") },
                leadingIcon = { Icon(Icons.Default.Cake, null) },
                isError = ageError != null,
                supportingText = ageError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !loading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "KORM-DSL Database",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "This user will be stored in a local SQLite database using KORM-DSL ORM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    // Validate all fields
                    val isValid = nameError == null &&
                        emailError == null &&
                        ageError == null &&
                        name.isNotBlank() &&
                        email.isNotBlank() &&
                        age.isNotBlank()

                    if (isValid) {
                        viewModel.createUser(
                            name = name,
                            email = email,
                            age = age.toInt()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !loading &&
                    nameError == null &&
                    emailError == null &&
                    ageError == null &&
                    name.isNotBlank() &&
                    email.isNotBlank() &&
                    age.isNotBlank()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.PersonAdd, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create User")
                }
            }
        }
    }
}
