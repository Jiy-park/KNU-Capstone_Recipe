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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.databinding.ActivityTestBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class TestActivity : AppCompatActivity() {
    val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    val s = Firebase.storage.getReference("images")
    val userRef = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users")
    val recipeRef = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("recipes")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launch(Dispatchers.IO) {
            val user = userRef.child("q").get().await()
            Log.d("LOG_CHECK", "user : $user")
            Log.d("LOG_CHECK", "name : ${user.child("name").value.toString()}")
        }

    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//        val uploadList1 = mutableListOf<RecipeBasicInfo>()
//        val uploadList2 = mutableListOf<RecipeBasicInfo>()
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            val time1 = measureTimeMillis {
//                val idList = du.child("q").child("uploadRecipe").get().await().children
//                for(id in idList){
//                    uploadList1.add(
//                        dr.child(id.value.toString())
//                            .child("basic_info")
//                            .get()
//                            .await()
//                            .getValue(RecipeBasicInfo::class.java)!!
//                    )
//                }
//            }
//            val time2 = measureTimeMillis {
//                val recipeIdList = du.child("q").child("uploadRecipe").get().await()
//                val uploadList = MutableList(recipeIdList.childrenCount.toInt()) { RecipeBasicInfo() }
//                recipeIdList.children.mapIndexed{ index, item ->
//                    async {
//                        val recipeId = item.value.toString()
//                        dr.child(recipeId)
//                            .child("basicInfo")
//                            .get()
//                            .await()
//                            .getValue(RecipeBasicInfo::class.java)?.let { recipeBasicInfo ->
//                                        uploadList[index] = recipeBasicInfo
//                            }
//                    }
//                }.awaitAll()
//
//                val d = du.child("q").child("uploadRecipe").get().await().children.toList()
//                val dwr = du.child("q").child("uploadRecipe").get().await().childrenCount
//
//                Log.d("LOG_CHECK", "dasd : $dwr")
//                val list = MutableList(d.size) { RecipeBasicInfo() }
//                val job = d.mapIndexed { index, item -> //0초
//                    Log.d("LOG_CHECK", "index = $index")
//                    val recipeId = item.value.toString()
//                    Log.d("LOG_CHECK", "item : $recipeId")
//                    async {
//                        dr.child(recipeId).child("basic_info").get().await().getValue(RecipeBasicInfo::class.java)?.let { recipeInfo ->
////                            mutex.withLock {
////                                uploadList2.add(recipeInfo)
////                            }
//                            list[index] = recipeInfo
//                        }
////                        dr.child(recipeId).child("basic_info").get().await().getValue(RecipeBasicInfo::class.java)?.let { recipeInfo ->
////                            synchronized(uploadList2) {
////                                uploadList2.add(recipeInfo)
////                            }
////                        }
//                    }
//                }.awaitAll()
//                Log.d("LOG_CHECK", "list : $list")
//            }
//            Log.d("LOG_CHECK", "uploadList1 : $uploadList1")
//            Log.d("LOG_CHECK", "uploadList2 : $uploadList2")
//            Log.d("LOG_CHECK", "time1 : $time1 \ntime2 : $time2")
//        }
//    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//        val uriList = mutableListOf<Uri>()
//        lifecycleScope.launch(Dispatchers.IO) {
//            val time = measureTimeMillis {
//                val l = s.listAll().await()
////                for(item in l.items){ // 13개 -> 3초
////                    uriList.add(item.downloadUrl.await())
////                    Log.d("LOG_CHECK", "${Thread.currentThread().name}")
////                }
//
//                val jobs = l.items.map {
//                    async(Dispatchers.IO) { // 13개 -> 1초
//                        uriList.add(it.downloadUrl.await())
//                        Log.d("LOG_CHECK", "${Thread.currentThread().name}")
//                    }
//                }
//                jobs.awaitAll()
//            }
//            Log.d("LOG_CHECK", "time : $time")
//            Log.d("LOG_CHECK", "${uriList.size}")
//        }
//    }


}
