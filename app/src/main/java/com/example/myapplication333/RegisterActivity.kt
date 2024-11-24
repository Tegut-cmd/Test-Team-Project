package com.example.myapplication333

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        try {
            initializeViews()
            setupClickListeners()
        } catch (e: Exception) {
            Log.e("RegisterActivity", "onCreate 오류: ${e.message}")
            Toast.makeText(this, "초기화 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            etUsername = findViewById(R.id.etUsername)
            etPassword = findViewById(R.id.etPassword)
            etConfirmPassword = findViewById(R.id.etConfirmPassword)
            etEmail = findViewById(R.id.etEmail)
            rgGender = findViewById(R.id.rgGender)
            btnRegister = findViewById(R.id.btnRegister)
        } catch (e: Exception) {
            Log.e("RegisterActivity", "initializeViews 오류: ${e.message}")
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            btnRegister.setOnClickListener {
                try {
                    val username = etUsername.text.toString().trim()
                    val password = etPassword.text.toString().trim()
                    val confirmPassword = etConfirmPassword.text.toString().trim()
                    val email = etEmail.text.toString().trim()

                    // gender 처리 수정
                    val gender = when (rgGender.checkedRadioButtonId) {
                        R.id.rbMale -> "male"
                        R.id.rbFemale -> "female"
                        else -> {
                            Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }

                    // 입력값 검증
                    if (validateInputs(username, email, password, confirmPassword, gender)) {
                        // 회원가입 API 호출
                        registerUser(username, password, email, gender)
                    }
                } catch (e: Exception) {
                    Log.e("RegisterActivity", "버튼 클릭 처리 오류: ${e.message}")
                    Toast.makeText(this, "처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("RegisterActivity", "setupClickListeners 오류: ${e.message}")
            throw e
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        gender: String
    ): Boolean {
        when {
            username.isEmpty() -> {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                etUsername.requestFocus()
                return false
            }
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
            password.length < 6 -> {
                Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
                return false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                etConfirmPassword.requestFocus()
                return false
            }
            gender.isEmpty() -> {
                Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun registerUser(username: String, password: String, email: String, gender: String) {
        // API 호출 전 로그
        Log.d("RegisterActivity", "회원가입 시도 - username: $username, email: $email, gender: $gender")

        // gender가 비어있지 않은지 한번 더 확인
        if (gender.isEmpty()) {
            Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.api.registerUser(username, password, email, gender)
            .enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        Log.d("RegisterActivity", "서버 응답 성공: ${result?.message}")
                        if (result?.success == true) {
                            Toast.makeText(this@RegisterActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                result?.message ?: "회원가입 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // 서버 오류 응답의 자세한 내용 로깅
                        val errorBody = response.errorBody()?.string()
                        Log.e("RegisterActivity", "서버 오류 응답: $errorBody")
                        Log.e("RegisterActivity", "HTTP 상태 코드: ${response.code()}")
                        Toast.makeText(this@RegisterActivity, "서버 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Log.e("RegisterActivity", "API 호출 실패", t)
                    Log.e("RegisterActivity", "에러 메시지: ${t.message}")
                    Toast.makeText(
                        this@RegisterActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}