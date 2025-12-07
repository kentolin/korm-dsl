// examples/example-android/src/main/kotlin/com/korm/examples/android/MainActivity.kt

package com.korm.examples.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.korm.examples.android.di.RepositoryModule
import com.korm.examples.android.ui.screens.AddUserScreen
import com.korm.examples.android.ui.screens.UserDetailScreen
import com.korm.examples.android.ui.screens.UserListScreen
import com.korm.examples.android.ui.theme.KormTheme
import com.korm.examples.android.ui.viewmodels.UserViewModel
import com.korm.examples.android.ui.viewmodels.UserViewModelFactory

/**
 * Main Activity demonstrating KORM-DSL usage in Android with Jetpack Compose.
 *
 * This example shows:
 * 1. SQLite database integration with KORM-DSL
 * 2. CRUD operations through repositories
 * 3. Modern Android UI with Jetpack Compose
 * 4. Navigation between screens
 * 5. LiveData observation for reactive updates
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize repository
        val repository = RepositoryModule.provideUserRepository(this)

        setContent {
            KormTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KormApp(repository)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        com.korm.examples.android.di.DatabaseModule.closeDatabase()
    }
}

@Composable
fun KormApp(repository: com.korm.examples.android.data.repository.UserRepository) {
    val navController = rememberNavController()

    // Create ViewModel with factory
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = "user_list"
    ) {
        // User List Screen
        composable("user_list") {
            UserListScreen(
                viewModel = viewModel,
                onUserClick = { user ->
                    navController.navigate("user_detail/${user.id}")
                },
                onAddUserClick = {
                    navController.navigate("add_user")
                }
            )
        }

        // User Detail Screen
        composable(
            route = "user_detail/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            UserDetailScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Add User Screen
        composable("add_user") {
            AddUserScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
