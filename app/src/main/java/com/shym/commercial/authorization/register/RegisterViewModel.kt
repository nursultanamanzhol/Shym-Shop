package com.shym.commercial.authorization.register

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
        return userRepository.createUserWithEmailAndPassword(email, password)
    }

    fun saveUserInfo(uid: String, email: String, iinMain: String, name: String): Task<Void> {
        return userRepository.saveUserInfo(uid, email, iinMain, name)
    }
}
