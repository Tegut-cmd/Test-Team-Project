package com.example.myapplication333


data class ResponseModel(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
)