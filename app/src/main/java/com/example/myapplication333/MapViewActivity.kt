package com.example.myapplication333

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.myapplication333.R
import com.example.myapplication333.databinding.FragmentHomeBinding  // 변경된 부분
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: FragmentHomeBinding  // 변경된 부분
    private var naverMap: NaverMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_home)

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_view, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 지도 초기 설정
        naverMap?.let { naverMap ->
            val cameraPosition = CameraPosition(
                LatLng(37.5666102, 126.9783881),  // 서울시청
                16.0  // 줌 레벨
            )
            naverMap.cameraPosition = cameraPosition
        }
    }
}