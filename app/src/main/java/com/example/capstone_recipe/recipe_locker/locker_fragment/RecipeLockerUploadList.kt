package com.example.capstone_recipe.recipe_locker.locker_fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.User
import com.example.capstone_recipe.databinding.FragmentRecipeLockerUploadListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.LockerRecipeViewerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class RecipeLockerUploadList(private val userInfo: User) : Fragment() {
    private lateinit var binding: FragmentRecipeLockerUploadListBinding
    private lateinit var context: Context
    private lateinit var adapter: LockerRecipeViewerAdapter
    private val userRef = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("users")
    private val recipeRef = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("recipes")

    private val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.head_default_image}")!! }
    private var creatorNameList = listOf<String>()
    private var uploadList = listOf<RecipeBasicInfo>()
    private var imageList = listOf<Uri>()

    private suspend fun getUploadList(userId: String): MutableList<RecipeBasicInfo>{
        val user = userRef.child(userId).get().await()
        val recipeIdList = userRef.child(userId).child("uploadRecipe").get().await()
        val uploadList = MutableList(recipeIdList.childrenCount.toInt()) { RecipeBasicInfo() }
        val time = measureTimeMillis{
            withContext(Dispatchers.IO){
                recipeIdList.children.mapIndexed{ index, item ->
                    async {
                        val recipeId = item.value.toString()
                        recipeRef.child(recipeId)
                            .child("basicInfo")
                            .get()
                            .await()
                            .getValue(RecipeBasicInfo::class.java)?.let { recipeBasicInfo ->
                                uploadList[index] = recipeBasicInfo
                            }
                    }
                }.awaitAll()
            }
        }
        Log.d("LOG_CHECK", "RecipeLocker :: getUploadList() -> time : $time")
        return uploadList
    }

    private suspend fun getRecipeInfoListByIds(idList: List<String>): List<RecipeBasicInfo> {
        val recipeRef = Firebase
            .database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("recipes")
        val list = mutableListOf<RecipeBasicInfo>()

        withContext(Dispatchers.IO){
            async {
                for(id in idList){
                    list.add(
                        recipeRef
                            .child(id)
                            .child("basicInfo")
                            .get()
                            .await()
                            .getValue(RecipeBasicInfo::class.java)!!
                    )
                }
            }
        }.await()
        return list
    }

    private suspend fun getCreatorListByIds(idList: List<String>): List<String> {
        val userRef = Firebase
            .database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users")
        val list = mutableListOf<String>()

        withContext(Dispatchers.IO){
            async {
                for(id in idList){
                    val userId = if(id.contains("_")) { id.split("_")[1] }
                                    else{ "error" }
                    val name = userRef
                        .child(userId)
                        .child("name")
                        .get()
                        .await()
                        .value
                        .toString()
                    list.add("$name @$userId")
                }
            }
        }.await()
        return list
    }

    private suspend fun getImageListByUploadList(uploadList: List<RecipeBasicInfo>): List<Uri> {
        val recipeRef= Firebase.storage.getReference("recipe_image")

        val list = mutableListOf<Uri>()

        for(i in uploadList.indices){
            if(uploadList[i].mainImagePath == "") { list.add(defaultImageUri) }
            else{
                try{
                    val d = recipeRef
                        .child(uploadList[i].id)
                        .child("main_image")
                        .child(uploadList[i].mainImagePath!!)
                        .downloadUrl
                        .await()
                    list.add(d)
                }
                catch (e:Exception){
                    Log.d("LOG_CHECK", "RecipeLockerUploadList :: getImageListByUploadList() -> e : $e")
                }

            }
        }
        return list
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeLockerUploadListBinding.inflate(inflater, container, false)
        context = binding.root.context

        lifecycleScope.launch(Dispatchers.IO) {
            async {
                uploadList = getRecipeInfoListByIds(userInfo.uploadRecipe)
                imageList = getImageListByUploadList(uploadList)
            }.await()
            creatorNameList = async { getCreatorListByIds(userInfo.uploadRecipe) }.await()
            withContext(Dispatchers.Main){
                adapter.recipeList = uploadList
                adapter.creatorIdNameList = creatorNameList
                adapter.imageList = imageList
                binding.recyclerviewUploadList.adapter = adapter
            }
        }

        adapter = LockerRecipeViewerAdapter()
        binding.recyclerviewUploadList.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewUploadList.adapter = adapter
        return binding.root
    }
}