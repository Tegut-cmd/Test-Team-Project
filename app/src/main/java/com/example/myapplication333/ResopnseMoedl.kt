package com.example.myapplication333


data class ResponseModel(
    val success: Boolean,
    val message: String,
    val data: Any? = null  // 필요한 경우 추가 데이터를 받을 수 있음
)