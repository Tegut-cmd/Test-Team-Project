package com.example.myapplication333

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // 기존 로그인/회원가입 API
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

    @Multipart
    @POST("create_post.php")
    fun createPost(
        @Part("user_id") userId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("is_public") isPublic: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<PostResponse>

    @GET("get_posts.php")
    fun getAllPosts(): Call<PostListResponse>


    @GET("get_user_posts.php")
    fun getUserPosts(@Query("user_id") userId: Int): Call<List<Post>>

    @FormUrlEncoded
    @POST("delete_post.php")
    fun deletePost(
        @Field("post_id") postId: Int
    ): Call<Map<String, Any>>
}
