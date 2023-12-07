package com.shym.commercial.data.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
class AuthRepositoryRg {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Users")

    fun registerUser(name: String, iinMain: String, email: String, password: String, cPassword: String, quickAccessCode: String, resultLiveData: MutableLiveData<Result<Unit>>) {
        // Логика валидации может быть перемещена сюда при необходимости

        // Аутентификация в Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Аккаунт создан, теперь добавим информацию о пользователе в базу данных
                saveUserInfo(name, iinMain, email, quickAccessCode, resultLiveData)
            }
            .addOnFailureListener { e ->
                // Ошибка при создании аккаунта
                resultLiveData.postValue(Result.failure(e))
            }
    }

    private fun saveUserInfo(name: String, iinMain: String, email: String, quickAccessCode: String, resultLiveData: MutableLiveData<Result<Unit>>) {
        // Сохранение информации о пользователе в Firebase Realtime Database

        // Таймстэмп
        val timestamp = System.currentTimeMillis()

        // Получаем текущий UID пользователя, так как пользователь зарегистрирован, мы можем его получить
        val uid = firebaseAuth.uid

        // Подготавливаем данные для добавления в базу данных
        val userMap: HashMap<String, Any?> = hashMapOf(
            "uid" to uid,
            "email" to email,
            "iiMain" to iinMain,
            "quickAccessCode" to quickAccessCode,
            "name" to name,
            "profileImage" to "", // Добавим пустое значение, заполним в редакторе профиля
            "language" to "",
            "mode" to "",
            "userType" to "user",
            "categories" to "company", // Возможные значения: user/admin, значение admin можно установить вручную в базе данных Firebase
            "timestamp" to timestamp
        )

        // Устанавливаем данные в базу данных
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
