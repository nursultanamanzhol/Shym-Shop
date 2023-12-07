package com.shym.commercial.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel : ViewModel() {
    private val _userType = MutableLiveData<String>()
    val userType: LiveData<String>
        get() = _userType

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _userCategories = MutableLiveData<String>()
    val userCategories: LiveData<String>
        get() = _userCategories

    private val firebaseAuth = FirebaseAuth.getInstance()

    init {
        loadUserInfo()
        checkingUser()
    }

    private fun checkingUser() {
        val firebaseUser = firebaseAuth.currentUser
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        firebaseUser?.let {
            usersRef.child(it.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val userType = userSnapshot.child("userType").getValue(String::class.java)
                        _userType.postValue(userType)

                        val email = firebaseUser.email
                        _userEmail.postValue(email)

                        if (userType == "user") {
                            _userCategories.postValue("")
                        } else if (userType == "salesman") {
                            val userCategories = userSnapshot.child("categories").getValue(String::class.java)
                            _userCategories.postValue(userCategories)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Обработка ошибок, если необходимо
                    }
                })
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        firebaseAuth.currentUser?.let { user ->
            ref.child(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Здесь вы можете обработать данные о пользователе, если это необходимо
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Обработка ошибок, если необходимо
                    }
                })
        }
    }

}

