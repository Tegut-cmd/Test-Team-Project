package com.example.myapplication333

data class Post(
    val id: Int,
    val user_id: Int,
    val username: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val is_public: Boolean,
    val created_at: String
)