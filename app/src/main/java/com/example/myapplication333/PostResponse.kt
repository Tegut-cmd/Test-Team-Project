package com.example.myapplication333

data class PostResponse(
    val success: Boolean,
    val message: String,
    val post: Post?
)