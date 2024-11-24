package com.example.myapplication333

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @POST("dds.php")
    @FormUrlEncoded
    fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String,
        @Field("gender") gender: String
    ): Call<ResponseModel>

    @POST("login.php")
    @FormUrlEncoded
    fun loginUser(
        @Field("email") username: String,
        @Field("password") password: String
    ): Call<ResponseModel>
}
