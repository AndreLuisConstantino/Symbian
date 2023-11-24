package com.example.symbian

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserService {
    @POST("/usuario/cadastrarUsuario")
    suspend fun postUser(@Body body: JsonObject): Response<JsonObject>
}