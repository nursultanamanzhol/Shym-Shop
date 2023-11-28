package com.shym.commercial.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shym.commercial.repositories.AuthRepository

// AuthViewModel.kt
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean>
        get() = _loginSuccess

    fun loginUser(email: String, password: String) {
        authRepository.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginSuccess.value = true
                } else {
                    _loginSuccess.value = false
                }
            }
    }
}
