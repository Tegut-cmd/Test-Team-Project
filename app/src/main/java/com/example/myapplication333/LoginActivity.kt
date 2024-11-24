package com.example.myapplication333

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        try {
            initializeViews()
            setupClickListeners()
        } catch (e: Exception) {
            Log.e("LoginActivity", "onCreate 오류: ${e.message}")
            Toast.makeText(this, "초기화 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            etUsername = findViewById(R.id.etUsername)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.loginButton)
            tvRegister = findViewById(R.id.tvSignup)
        } catch (e: Exception) {
            Log.e("LoginActivity", "initializeViews 오류: ${e.message}")
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            btnLogin.setOnClickListener {
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (validateInputs(username, password)) {
                    loginUser(username, password)
                }
            }

            tvRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "setupClickListeners 오류: ${e.message}")
            throw e
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        when {
            username.isEmpty() -> {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                etUsername.requestFocus()
                return false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
                return false
            }
        }
        return true
    }

    private fun loginUser(username: String, password: String) {
        // 로그인 시도 로그
        Log.d("LoginActivity", "로그인 시도 - username: $username")

        RetrofitClient.api.loginUser(username, password)
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        Log.d("LoginActivity", "서버 응답: ${result?.message}")
                        if (result?.success == true) {
                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            // 메인 화면으로 이동
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                result?.message ?: "로그인 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // 서버 오류 응답의 자세한 내용 로깅
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginActivity", "서버 오류 응답: $errorBody")
                        Log.e("LoginActivity", "HTTP 상태 코드: ${response.code()}")
                        Toast.makeText(this@LoginActivity, "서버 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Log.e("LoginActivity", "API 호출 실패", t)
                    Log.e("LoginActivity", "에러 메시지: ${t.message}")
                    Toast.makeText(
                        this@LoginActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}