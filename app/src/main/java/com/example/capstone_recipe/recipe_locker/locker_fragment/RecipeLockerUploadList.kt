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
import com.example.capstone_recipe.databinding.FragmentRecipeLockerUploadListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.LockerRecipeViewerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class RecipeLockerUploadList(private val user: DataSnapshot?) : Fragment() {
    private lateinit var binding: FragmentRecipeLockerUploadListBinding
    private lateinit var adapter: LockerRecipeViewerAdapter
    private lateinit var context: Context
    private val userRef = Firebase.database.getReference("users")
    private val recipeRef = Firebase.database.getReference("recipes")

    private var creatorNameList = listOf<String>()
    private var uploadList = listOf<RecipeBasicInfo>()
    private var imageList = listOf<Uri>()

    /** * userId에 해당하는 유저의 업로드 리스트(이미지 포함)를 받아와서 업데이트*/
    private suspend fun updateUploadList(){
        val recipeIdList = user!!.child("uploadRecipe")
        val size = recipeIdList.childrenCount.toInt()
        val newUploadList = MutableList(size) { RecipeBasicInfo() } // 업로드 리스트
        val newImageList = MutableList<Uri>(size) { Uri.EMPTY }         // 이미지 리스트

        withContext(Dispatchers.IO){
            recipeIdList.children.mapIndexed{ index, item ->
                async {
                    val recipeId = item.value.toString()
                    recipeRef.child(recipeId)
                        .child("basicInfo")
                        .get()
                        .await()
                        .getValue(RecipeBasicInfo::class.java)?.let { recipeBasicInfo ->
                            newUploadList[index] = recipeBasicInfo
                             newImageList[index] = getMainImage(recipeId, recipeBasicInfo.mainImagePath!!)
                        }
                }
            }.awaitAll()
            uploadList = newUploadList
            imageList = newImageList
        }
    }
    /** * recipeId에 해당하는 레시피의 메인 이미지 반납, 없을 경우 디폴트 이미지로 반납 */
    private suspend fun getMainImage(recipeId: String, mainImagePath: String): Uri{
        val defaultImageUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!!
        val uri =
            if(mainImagePath == ""){ defaultImageUri }
            else{
                Firebase.storage.getReference("recipe_image")
                    .child(recipeId)
                    .child("main_image")
                    .child(mainImagePath)
                    .downloadUrl
                    .await()
            }
        return uri
    }

    /** * 어댑터 하나만 쓰려고 만듦. 굳이의 영역이긴 한데 */
    private fun makeCreatorList(user: DataSnapshot): List<String>{
        val size = user.child("uploadRecipe").childrenCount
        val name = user.child("name").value.toString()
        val id = user.child("id").value.toString()
        val creator = "$name @$id"
        return List(size.toInt()) { creator }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeLockerUploadListBinding.inflate(inflater, container, false)
        adapter = LockerRecipeViewerAdapter()
        context = binding.root.context

        if(user != null && user.child("uploadRecipe").exists()){
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    updateUploadList() // 업로드 리스트, 이미지 리스트 업데이트
                    creatorNameList = makeCreatorList(user)
                }
                adapter.recipeList = uploadList
                adapter.creatorIdNameList = creatorNameList
                adapter.imageList = imageList
                binding.recyclerviewUploadList.adapter = adapter
            }
        }
        binding.recyclerviewUploadList.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewUploadList.adapter = adapter
        return binding.root
    }
}
