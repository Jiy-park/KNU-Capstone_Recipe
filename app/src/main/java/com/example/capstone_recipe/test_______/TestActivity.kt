package com.example.capstone_recipe.test_______

import android.Manifest
import android.app.ProgressDialog
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class TestActivity : AppCompatActivity() {
    val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    val storage = FirebaseStorage.getInstance()
    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
        uri?.let { uploadImage(it) }
    }
    val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->
        if(isGranted) { galleryLauncher.launch("image/*") }
        else { Toast.makeText(binding.root.context, "외부 저장소 읽기 권한을 승인해야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnUpload.setOnClickListener { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        binding.btnDownload.setOnClickListener { downloadImage("images/temp_1681035425810.jpeg") }
    }

    fun uploadImage(uri: Uri){
        val fullPath = makeFilePath("images", "temp", uri)
        val imageRef = storage.getReference(fullPath)
        val uploadTask = imageRef.putFile(uri)

        // Show progress dialog while uploading
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading image...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        uploadTask
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.d("LOG_CHECK", "MainActivity :: uploadImage() called :: 실패 : ${it.message}")
            }
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d("LOG_CHECK", "MainActivity :: uploadImage() called :: 성공 : $fullPath")
            }
    }

    fun makeFilePath(path: String, userId: String, uri: Uri): String {
        val mimeType = contentResolver.getType(uri) ?: "/none" //마임타입 ex) images/jpeg
        val ext = mimeType.split("/")[1] //확장자 ex) jpeg
        val timeSuffix = System.currentTimeMillis() // 시간값 ex)123123123
        return "${path}/${userId}_${timeSuffix}.$ext" // 파일 경로
    }

    fun downloadImage(path:String){
        storage.getReference(path).downloadUrl
            .addOnSuccessListener { uri->
                Glide.with(binding.root.context)
                    .load(uri)
                    .into(binding.iv)
            }
            .addOnFailureListener { Log.d("LOG_CHECK", "MainActivity :: downloadImage() called :: 실패 :${it.message}") }
    }
}