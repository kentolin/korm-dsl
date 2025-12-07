// examples/example-android/src/main/kotlin/com/korm/examples/android/ui/screens/UserListScreen.kt

package com.korm.examples.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.korm.examples.android.data.local.entities.UserEntity
import com.korm.examples.android.ui.viewmodels.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * User List Screen using Jetpack Compose.
 *
 * Displays a list of users fetched using KORM-DSL through the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    onUserClick: (UserEntity) -> Unit,
    onAddUserClick: () -> Unit
) {
    val users by viewModel.users.observeAsState(emptyList())
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val message by viewModel.message.observeAsState()
    val statistics by viewModel.statistics.observeAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterOption by remember { mutableStateOf(FilterOption.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                actions = {
                    // Search
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }

                    // Refresh
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddUserClick) {
                Icon(Icons.Default.Add, "Add User")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Statistics Card
            statistics?.let { stats ->
                StatisticsCard(
                    stats = stats,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chips
            FilterChips(
                selectedFilter = filterOption,
                onFilterChange = { filter ->
                    filterOption = filter
                    when (filter) {
                        FilterOption.ALL -> viewModel.loadUsers()
                        FilterOption.ACTIVE -> viewModel.loadActiveUsers()
                        FilterOption.INACTIVE -> {
                            // Load inactive users
                            viewModel.loadUsers() // You can add loadInactiveUsers() to ViewModel
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // User List
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    ErrorMessage(
                        message = error ?: "Unknown error",
                        onRetry = { viewModel.loadUsers() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                users.isEmpty() -> {
                    EmptyState(
                        message = "No users found",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users, key = { it.id }) { user ->
                            UserItem(
                                user = user,
                                onClick = { onUserClick(user) },
                                onToggleStatus = { viewModel.toggleUserStatus(user.id) },
                                onDelete = { viewModel.deleteUser(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show messages
    message?.let { msg ->
        LaunchedEffect(msg) {
            // Show snackbar or toast
            viewModel.clearMessage()
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { minAge, maxAge ->
                viewModel.filterByAgeRange(minAge, maxAge)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun StatisticsCard(
    stats: com.korm.examples.android.data.repository.UserStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total",
                value = stats.total.toString(),
                color = MaterialTheme.colorScheme.primary
            )

            StatItem(
                label = "Active",
                value = stats.active.toString(),
                color = Color(0xFF4CAF50)
            )

            StatItem(
                label = "Inactive",
                value = stats.inactive.toString(),
                color = Color(0xFFF44336)
            )

            StatItem(
                label = "Avg Age",
                value = String.format("%.1f", stats.averageAge),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search by name...") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterOption.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserItem(
    user: UserEntity,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    color = if (user.isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Age: ${user.age}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (user.isActive) "Deactivate" else "Activate")
                        },
                        onClick = {
                            onToggleStatus()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (minAge: Int, maxAge: Int) -> Unit
) {
    var minAge by remember { mutableStateOf("0") }
    var maxAge by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Age") },
        text = {
            Column {
                OutlinedTextField(
                    value = minAge,
                    onValueChange = { minAge = it },
                    label = { Text("Min Age") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = maxAge,
                    onValueChange = { maxAge = it },
                    label = { Text("Max Age") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val min = minAge.toIntOrNull() ?: 0
                    val max = maxAge.toIntOrNull() ?: 100
                    onApplyFilter(min, max)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class FilterOption(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    INACTIVE("Inactive")
}
