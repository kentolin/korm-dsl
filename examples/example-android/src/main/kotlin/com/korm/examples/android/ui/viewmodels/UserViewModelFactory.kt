// examples/example-android/src/main/kotlin/com/korm/examples/android/ui/viewmodels/UserViewModelFactory.kt

package com.korm.examples.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.korm.examples.android.data.repository.UserRepository

/**
 * Factory for creating UserViewModel with dependencies.
 */
class UserViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
