package com.example.myapplication333.ui.home

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.myapplication333.RetrofitClient
import com.example.myapplication333.Post
import com.example.myapplication333.PostListResponse
import com.example.myapplication333.PostResponse
import com.example.myapplication333.R
import com.example.myapplication333.databinding.FragmentHomeBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.MarkerIcons
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var naverMap: NaverMap? = null
    private lateinit var homeViewModel: HomeViewModel
    private val markers = mutableListOf<Marker>()  // 마커 리스트 추가

    private var isMarkerMode = false
    private var currentMarker: Marker? = null
    private var selectedImageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                selectedImageUri = it
                showImagePreview(it)
            } catch (e: Exception) {
                Log.e("ImageSelection", "이미지 선택 중 오류", e)
                showToast("이미지 선택 중 오류가 발생했습니다")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.fabAddMarker.setOnClickListener {
            isMarkerMode = !isMarkerMode
            if (isMarkerMode) {
                showToast("지도를 클릭하여 마커를 추가하세요")
                binding.fabAddMarker.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                binding.fabAddMarker.setImageResource(android.R.drawable.ic_input_add)
                currentMarker?.map = null
            }
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        val cameraPosition = CameraPosition(LatLng(37.5666102, 126.9783881), 16.0)
        map.cameraPosition = cameraPosition

        map.setOnMapClickListener { _, coord ->
            if (isMarkerMode) {
                currentMarker?.map = null
                currentMarker = Marker().apply {
                    position = coord
                    this.map = map
                    icon = MarkerIcons.BLACK
                    iconTintColor = Color.RED
                }
                showMarkerConfirmDialog(coord)
            }
        }

        // 초기 데이터 로드
        loadPosts()
    }

    private fun loadPosts() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        Log.d("HomeFragment", "Loading posts for user ID: $userId")

        if (userId == -1) {
            Log.e("HomeFragment", "User ID not found in SharedPreferences")
            showToast("로그인 정보를 찾을 수 없습니다")
            return
        }

        RetrofitClient.api.getAllPosts().enqueue(object : Callback<PostListResponse> {
            override fun onResponse(
                call: Call<PostListResponse>,
                response: Response<PostListResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { postResponse ->
                        if (postResponse.success) {
                            postResponse.posts.forEach { post ->
                                if (post.is_public || post.user_id == userId) {
                                    addMarkerForPost(post)
                                }
                            }
                        } else {
                            showToast(postResponse.message ?: "게시글 로드 실패")
                        }
                    }
                } else {
                    showToast("게시글 로드 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PostListResponse>, t: Throwable) {
                Log.e("HomeFragment", "Network error", t)
                showToast("네트워크 오류: ${t.message}")
            }
        })
    }

    private fun showMarkerConfirmDialog(coord: LatLng) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_post_creation, null)
        val titleEdit = dialogView.findViewById<EditText>(R.id.et_title)
        val contentEdit = dialogView.findViewById<EditText>(R.id.et_content)
        val imageButton = dialogView.findViewById<Button>(R.id.btn_add_image)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.iv_preview)
        val isPublicSwitch = dialogView.findViewById<Switch>(R.id.switch_public)

        imageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("등록") { _, _ ->
                createPost(titleEdit.text.toString(), contentEdit.text.toString(),
                    coord, isPublicSwitch.isChecked)
            }
            .setNegativeButton("취소") { _, _ ->
                currentMarker?.map = null
                isMarkerMode = false
                binding.fabAddMarker.setImageResource(android.R.drawable.ic_input_add)
            }
            .create()

        dialog?.show()
    }

    private fun createPost(title: String, content: String, coord: LatLng, isPublic: Boolean) {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            showToast("로그인 정보를 찾을 수 없습니다")
            return
        }

        try {
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaType())
            val titleBody = title.toRequestBody("text/plain".toMediaType())
            val contentBody = content.toRequestBody("text/plain".toMediaType())
            val latBody = coord.latitude.toString().toRequestBody("text/plain".toMediaType())
            val lngBody = coord.longitude.toString().toRequestBody("text/plain".toMediaType())
            val isPublicBody = isPublic.toString().toRequestBody("text/plain".toMediaType())

            var imagePart: MultipartBody.Part? = null
            selectedImageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                bytes?.let {
                    val requestFile = bytes.toRequestBody("image/*".toMediaType())
                    imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
                }
            }

            RetrofitClient.api.createPost(
                userIdBody,
                titleBody,
                contentBody,
                latBody,
                lngBody,
                isPublicBody,
                imagePart
            ).enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { postResponse ->
                            if (postResponse.success && postResponse.post != null) {
                                addMarkerForPost(postResponse.post)
                                showToast("게시글이 등록되었습니다")
                                dialog?.dismiss()
                            } else {
                                showToast(postResponse.message ?: "게시글 등록 실패")
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("CreatePost", "Server Error: ${response.code()} - $errorBody")
                        showToast("게시글 등록 실패 (${response.code()})")
                    }
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    Log.e("CreatePost", "Network Error", t)
                    showToast("네트워크 오류: ${t.message}")
                }
            })

        } catch (e: Exception) {
            Log.e("CreatePost", "Error creating post", e)
            showToast("오류 발생: ${e.message}")
        }
    }

    private fun showPostDetailDialog(post: Post, marker: Marker) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_post_detail, null)
        dialogView.findViewById<TextView>(R.id.tv_title).text = post.title
        dialogView.findViewById<TextView>(R.id.tv_content).text = post.content

        val imageView = dialogView.findViewById<ImageView>(R.id.iv_post_image)
        if (!post.imageUrl.isNullOrEmpty()) {
            imageView.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(post.imageUrl)
                .into(imageView)
        } else {
            imageView.visibility = View.GONE
        }

        val btnDelete = dialogView.findViewById<Button>(R.id.btn_delete)
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("user_id", -1)

        if (currentUserId == post.user_id) {
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                deletePost(post.id, marker)
            }
        } else {
            btnDelete.visibility = View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun deletePost(postId: Int, marker: Marker) {
        AlertDialog.Builder(requireContext())
            .setTitle("게시글 삭제")
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                RetrofitClient.api.deletePost(postId).enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        if (response.isSuccessful) {
                            marker.map = null
                            markers.remove(marker)  // 마커 리스트에서도 제거
                            showToast("게시글이 삭제되었습니다")
                        } else {
                            showToast("삭제 실패")
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        showToast("네트워크 오류")
                    }
                })
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showImagePreview(uri: Uri) {
        try {
            val dialogView = dialog?.findViewById<View>(R.id.dialog_layout)
            val imagePreview = dialogView?.findViewById<ImageView>(R.id.iv_preview)
            imagePreview?.visibility = View.VISIBLE
            imagePreview?.let {
                Glide.with(requireContext())
                    .load(uri)
                    .into(it)
            }
        } catch (e: Exception) {
            Log.e("ImagePreview", "이미지 미리보기 중 오류", e)
            showToast("이미지 미리보기 중 오류가 발생했습니다")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearAllMarkers() {
        markers.forEach { marker ->
            marker.map = null
        }
        markers.clear()
    }

    private fun addMarkerForPost(post: Post) {
        val marker = Marker().apply {
            position = LatLng(post.latitude, post.longitude)
            map = naverMap
            icon = MarkerIcons.BLACK
            iconTintColor = Color.RED
            tag = post
        }

        marker.setOnClickListener {
            showPostDetailDialog(post, marker)
            true
        }

        markers.add(marker)  // 마커를 리스트에 추가
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        // 지도가 준비되었을 때만 데이터 로드
        if (naverMap != null) {
            clearAllMarkers()  // 기존 마커 제거
            loadPosts()        // 데이터 다시 로드
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}


