package com.shym.commercial.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shym.commercial.extensions.MyApplication

data class UserInfo(
    val name: String,
    val email: String,
    val userType: String,
    val memberDate: String,
    val profileImage: String
)

class ProfileViewModel : ViewModel() {
    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo>
        get() = _userInfo

    private val firebaseAuth = FirebaseAuth.getInstance()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        firebaseAuth.currentUser?.let { user ->
            ref.child(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val email = "${snapshot.child("email").value}"
                        val name = "${snapshot.child("name").value}"
                        val profileImage = "${snapshot.child("profileImage").value}"
                        val timestamp = "${snapshot.child("timestamp").value}"
                        val uid = "${snapshot.child("uid").value}"
                        val userType = "${snapshot.child("userType").value}"
                        val quickAccessCode = "${snapshot.child("quickAccessCode").value}"

                        val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                        val userInfo = UserInfo(
                            name = name,
                            email = email,
                            userType = userType,
                            memberDate = formattedDate,
                            profileImage = profileImage
                        )

                        _userInfo.postValue(userInfo)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Обработка ошибок, если необходимо
                    }
                })
        }
    }
}
