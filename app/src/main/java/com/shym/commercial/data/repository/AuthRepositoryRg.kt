package com.shym.commercial.data.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
class AuthRepositoryRg {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Users")

    fun registerUser(name: String, iinMain: String, email: String, password: String, cPassword: String, quickAccessCode: String, resultLiveData: MutableLiveData<Result<Unit>>) {

        // Аутентификация в Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                saveUserInfo(name, iinMain, email, quickAccessCode, resultLiveData)
            }
            .addOnFailureListener { e ->
                resultLiveData.postValue(Result.failure(e))
            }
    }

    private fun saveUserInfo(name: String, iinMain: String, email: String, quickAccessCode: String, resultLiveData: MutableLiveData<Result<Unit>>) {

        val timestamp = System.currentTimeMillis()

        val uid = firebaseAuth.uid

        val userMap: HashMap<String, Any?> = hashMapOf(
            "uid" to uid,
            "email" to email,
            "iiMain" to iinMain,
            "quickAccessCode" to quickAccessCode,
            "name" to name,
            "profileImage" to "",
            "language" to "",
            "mode" to "",
            "userType" to "user",
            "categories" to "company",
            "timestamp" to timestamp
        )

        databaseReference.child(uid!!).setValue(userMap)
            .addOnSuccessListener {
                // Информация о пользователе сохранена
                resultLiveData.postValue(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                // Ошибка при добавлении данных в базу данных
                resultLiveData.postValue(Result.failure(e))
            }
    }
}
