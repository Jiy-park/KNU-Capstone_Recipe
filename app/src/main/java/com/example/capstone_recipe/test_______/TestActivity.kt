package com.example.capstone_recipe.test_______

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class TestActivity : AppCompatActivity() {
    val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }

    lateinit var cameraPermission: ActivityResultLauncher<String> // 카메라 권한
    lateinit var storagePermission:ActivityResultLauncher<String> // 저장소 권한
    lateinit var cameraLauncher:ActivityResultLauncher<Uri> // 카메라 앱 호출
    lateinit var galleryLauncher:ActivityResultLauncher<String> //갤러리
    var photoUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.d("LOG_CHECK", "IN ON_CREATE1")
//        setViews()

        storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->
            Log.d("LOG_CHECK", "IN STORAGE_PERMISSION, isGranted : ${isGranted.toString()}")
            if(isGranted == true) { setViews() }
            else{
                Toast.makeText(baseContext, "외부 저장소 권한을 승인해야 앱을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        Log.d("LOG_CHECK", "IN ON_CREATE2")
        cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            Log.d("LOG_CHECK", "IN CAMERA_PERMISSION")
            if(isGranted == true) { openCamera() }
            else {
                Toast.makeText(baseContext, "카메라 권한을 승인해야 카메라를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }

        }
        Log.d("LOG_CHECK", "IN ON_CREATE3")
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){ isSuccess->
            Log.d("LOG_CHECK", "IN CAMERA_LAUNCHER")
            if(isSuccess == true) { binding.ivPrev.setImageURI(photoUri) }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
            binding.ivPrev.setImageURI(uri)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermission.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    fun setViews(){
        Log.d("LOG_CHECK", "IN SET_VIEWS")
        binding.btnCamera.setOnClickListener {
            cameraPermission.launch(android.Manifest.permission.CAMERA)
        }
        binding.btnGallery.setOnClickListener {
            openGallery()
        }
    }

    fun openCamera(){
        Log.d("LOG_CHECK", "IN OPEN_CAMERA")
        val photoFile = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        cameraLauncher.launch(photoUri)
    }

    fun openGallery(){
        galleryLauncher.launch("image/*")
    }
}