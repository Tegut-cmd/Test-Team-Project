package com.example.myapplication333

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_splash)
            Log.d(TAG, "setContentView 완료")

            // 이미지뷰 찾기
            val splashImage = findViewById<ImageView>(R.id.splashImage)
            Log.d(TAG, "이미지뷰 찾기 완료")

            try {
                // 애니메이션 시작
                val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left)
                splashImage.startAnimation(slideAnimation)
                Log.d(TAG, "애니메이션 시작")
            } catch (e: Exception) {
                Log.e(TAG, "애니메이션 오류: ${e.message}")
                e.printStackTrace()
            }

            // 화면 전환
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    Log.d(TAG, "LoginActivity로 전환 시도")
                } catch (e: Exception) {
                    Log.e(TAG, "화면 전환 오류: ${e.message}")
                    e.printStackTrace()
                }
            }, 2000)

        } catch (e: Exception) {
            Log.e(TAG, "전체 오류: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy 오류: ${e.message}")
            e.printStackTrace()
        }
    }
}