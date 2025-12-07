// examples/example-android/src/main/kotlin/com/korm/examples/android/ui/viewmodels/UserViewModel.kt

package com.korm.examples.android.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korm.examples.android.data.local.entities.UserEntity
import com.korm.examples.android.data.repository.UserRepository
import com.korm.examples.android.data.repository.UserStatistics
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for user-related UI operations.
 *
 * Demonstrates KORM-DSL usage through the repository layer.
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users

    private val _selectedUser = MutableLiveData<UserEntity?>()
    val selectedUser: LiveData<UserEntity?> = _selectedUser

    private val _statistics = MutableLiveData<UserStatistics>()
    val statistics: LiveData<UserStatistics> = _statistics

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        loadUsers()
        loadStatistics()
    }

    /**
     * Load all users.
     */
    fun loadUsers() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.getAllUsers()
                .catch { e ->
                    _error.value = e.message
                    _loading.value = false
                }
                .collect { userList ->
                    _users.value = userList
                    _loading.value = false
                }
        }
    }

    /**
     * Load active users only.
     */
    fun loadActiveUsers() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val activeUsers = repository.getActiveUsers()
                _users.value = activeUsers
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Search users by name.
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            loadUsers()
            return
        }

        viewModelScope.launch {
            _loading.value = true
            repository.searchUsers(query)
                .catch { e ->
                    _error.value = e.message
                    _loading.value = false
                }
                .collect { results ->
                    _users.value = results
                    _loading.value = false
                }
        }
    }

    /**
     * Filter users by age range.
     */
    fun filterByAgeRange(minAge: Int, maxAge: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val filtered = repository.getUsersByAgeRange(minAge, maxAge)
                _users.value = filtered
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Create a new user.
     */
    fun createUser(name: String, email: String, age: Int) {
        viewModelScope.launch {
            _loading.value = true

            repository.createUser(name, email, age)
                .onSuccess { user ->
                    _message.value = "User created successfully"
                    loadUsers()
                    loadStatistics()
                }
                .onFailure { e ->
                    _error.value = e.message
                }

            _loading.value = false
        }
    }

    /**
     * Select a user for viewing details.
     */
    fun selectUser(userId: Long) {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                _selectedUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Update user.
     */
    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            _loading.value = true

            repository.updateUser(user)
                .onSuccess {
                    _message.value = "User updated successfully"
                    loadUsers()
                    _selectedUser.value = it
                }
                .onFailure { e ->
                    _error.value = e.message
                }

            _loading.value = false
        }
    }

    /**
     * Toggle user's active status.
     */
    fun toggleUserStatus(userId: Long) {
        viewModelScope.launch {
            repository.toggleUserStatus(userId)
                .onSuccess { updatedUser ->
                    _message.value = if (updatedUser.isActive) {
                        "User activated"
                    } else {
                        "User deactivated"
                    }
                    loadUsers()
                    loadStatistics()
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    /**
     * Delete user.
     */
    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            repository.deleteUser(userId)
                .onSuccess {
                    _message.value = "User deleted successfully"
                    loadUsers()
                    loadStatistics()
                    _selectedUser.value = null
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    /**
     * Delete all inactive users.
     */
    fun deleteInactiveUsers() {
        viewModelScope.launch {
            repository.deleteInactiveUsers()
                .onSuccess { count ->
                    _message.value = "$count inactive user(s) deleted"
                    loadUsers()
                    loadStatistics()
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    /**
     * Load user statistics.
     */
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = repository.getUserStatistics()
                _statistics.value = stats
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear success message.
     */
    fun clearMessage() {
        _message.value = null
    }
}
