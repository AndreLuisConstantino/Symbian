package com.example.symbian

import android.net.Uri
import com.google.gson.JsonObject
import retrofit2.Response

class LoginRepository {

    private val apiService = RetrofitHelper.getInstance().create(UserService::class.java)

    suspend fun loginUser(email: String, senha: String, foto: String): Response<JsonObject> {
        val requestBody = JsonObject().apply {
            addProperty("login", email)
            addProperty("senha", senha)
            addProperty("imagem", foto)
        }

        return apiService.postUser(requestBody)
    }
}