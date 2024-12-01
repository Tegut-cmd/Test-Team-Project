package com.example.myapplication333

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var checkBoxAutoLogin: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        try {
            initializeViews()
            checkAutoLogin()  // 추가
            setupClickListeners()
        } catch (e: Exception) {
            Log.e("LoginActivity", "onCreate 오류: ${e.message}")
            Toast.makeText(this, "초기화 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            tvRegister = findViewById(R.id.tvRegister)
            checkBoxAutoLogin = findViewById(R.id.checkBoxAutoLogin)  // 추가
        } catch (e: Exception) {
            Log.e("LoginActivity", "initializeViews 오류: ${e.message}")
            throw e
        }
    }

    // 추가된 함수
    private fun checkAutoLogin() {
        val sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE)
        val isAutoLogin = sharedPref.getBoolean("auto_login", false)

        if (isAutoLogin) {
            val savedEmail = sharedPref.getString("email", "") ?: ""
            val savedPassword = sharedPref.getString("password", "") ?: ""

            if (savedEmail.isNotEmpty() && savedPassword.isNotEmpty()) {
                loginUser(savedEmail, savedPassword)
            }
        }
    }

    private fun setupClickListeners() {
        try {
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (validateInputs(email, password)) {
                    loginUser(email, password)
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

    private fun validateInputs(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다", Toast.LENGTH_SHORT).show()
                etEmail.requestFocus()
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

    private fun loginUser(email: String, password: String) {
        RetrofitClient.api.loginUser(email, password)
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result?.success == true) {
                            // 사용자 ID 저장
                            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("user_id", result.userId ?: -1)
                                apply()
                            }

                            // 자동 로그인 정보 저장
                            if (checkBoxAutoLogin.isChecked) {
                                saveLoginInfo(email, password)
                            }

                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
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

    private fun saveLoginInfo(email: String, password: String) {
        val sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("auto_login", true)
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    companion object {
        fun clearAutoLogin(context: Context) {
            val sharedPref = context.getSharedPreferences("login_pref", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }
        }
    }
}