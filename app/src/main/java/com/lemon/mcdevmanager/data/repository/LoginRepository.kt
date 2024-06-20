package com.lemon.mcdevmanager.data.repository

class LoginRepository {

    companion object {
        @Volatile
        private var instance: LoginRepository? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: LoginRepository().also { instance = it }
        }
    }


}