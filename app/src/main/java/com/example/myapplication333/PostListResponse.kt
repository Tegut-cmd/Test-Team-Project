package com.example.myapplication333

data class PostListResponse(
    val success: Boolean,
    val message: String?,
    val posts: List<Post>
)