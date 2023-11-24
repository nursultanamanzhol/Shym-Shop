package com.shym.commercial.authorization.register

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

interface UserRepository {
    fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult>
    fun saveUserInfo(uid: String, email: String, iinMain: String, name: String): Task<Void>
}