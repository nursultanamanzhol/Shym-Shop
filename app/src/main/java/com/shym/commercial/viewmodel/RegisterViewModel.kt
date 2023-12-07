package com.shym.commercial.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shym.commercial.data.repository.AuthRepositoryRg

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepositoryRg()

    // LiveData для отслеживания результата регистрации
    private val _registrationResult = MutableLiveData<Result<Unit>>()
    val registrationResult: LiveData<Result<Unit>> get() = _registrationResult

    fun registerUser(name: String, iinMain: String, email: String, password: String, cPassword: String, quickAccessCode: String) {
        // Логика валидации может быть перемещена сюда при необходимости
        authRepository.registerUser(name, iinMain, email, password, cPassword, quickAccessCode, _registrationResult)
    }
}
