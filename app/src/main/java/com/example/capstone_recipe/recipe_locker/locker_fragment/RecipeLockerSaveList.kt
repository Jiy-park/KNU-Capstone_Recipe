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
import com.example.capstone_recipe.databinding.FragmentRecipeLockerSaveListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.LockerRecipeViewerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class RecipeLockerSaveList(private val user: DataSnapshot?) : Fragment() {

    private lateinit var binding: FragmentRecipeLockerSaveListBinding
    private lateinit var adapter: LockerRecipeViewerAdapter
    private lateinit var context: Context
    private val recipeRef = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("recipes")

    private var creatorNameList = listOf<String>()
    private var saveList = listOf<RecipeBasicInfo>()
    private var imageList = listOf<Uri>()

    /** * userId에 해당하는 유저의 보관 리스트(이미지 포함)를 받아와서 업데이트*/
    private suspend fun updateUploadList(){
        val recipeIdList = user!!.child("saveRecipe")
        val size = recipeIdList.childrenCount.toInt()
        val newSaveList = MutableList(size) { RecipeBasicInfo() } // 업로드 리스트
        val newImageList = MutableList<Uri>(size) { Uri.EMPTY }         // 이미지 리스트

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
                                newSaveList[index] = recipeBasicInfo
                                newImageList[index] = getMainImage(recipeId, recipeBasicInfo.mainImagePath!!)
                            }
                    }
                }.awaitAll()
                saveList = newSaveList
                imageList = newImageList
            }
        }
        Log.d("LOG_CHECK", "RecipeLockerSaveList :: updateUploadList() -> time : $time")
    }

    /** * recipeId에 해당하는 레시피의 메인 이미지 반납, 없을 경우 디폴트 이미지로 반납 */
    private suspend fun getMainImage(recipeId: String, mainImagePath: String): Uri{
        Log.d("LOG_CHECK", "RecipeLockerSaveList :: getMainImage() -> 22222222222222222222222222222")
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
        Log.d("LOG_CHECK", "RecipeLockerSaveList :: getMainImage() -> 1111111111111111111")
        return uri
    }

    /** * 어댑터 하나만 쓰려고 만듦. 굳이의 영역이긴 한데 */
    private fun makeCreatorList(user: DataSnapshot): List<String>{
        val size = user.child("uploadRecipe").childrenCount.toInt()
        val name = user.child("name").value.toString()
        val id = user.child("id").value.toString()
        val creator = "$name @$id"
        return List(size) { creator }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeLockerSaveListBinding.inflate(inflater, container, false)
        adapter = LockerRecipeViewerAdapter()
        context = binding.root.context


        Log.d("LOG_CHECK", "RecipeLockerSaveList :: onCreateView() -> --")
        if(user != null && user.child("saveRecipe").exists()){
            Log.d("LOG_CHECK", "RecipeLockerSaveList :: onCreateView() -> called")
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    updateUploadList() // 업로드 리스트, 이미지 리스트 업데이트
                    creatorNameList = makeCreatorList(user)
                }
                adapter.recipeList = saveList
                adapter.creatorIdNameList = creatorNameList
                adapter.imageList = imageList
                binding.recyclerviewSaveList.adapter = adapter
            }
        }
        Log.d("LOG_CHECK", "RecipeLockerSaveList :: onCreateView() -> --")
//        lifecycleScope.launch(Dispatchers.IO) {
//            async {
//                saveList = getRecipeInfoListByIds(userInfo.saveRecipe)
//                imageList = getImageListByUploadList(saveList)
//            }.await()
//            creatorNameList = async { getCreatorListByIds(userInfo.saveRecipe) }.await()
//            withContext(Dispatchers.Main){
//                adapter.recipeList = saveList
//                adapter.creatorIdNameList = creatorNameList
//                adapter.imageList = imageList
//                binding.recyclerviewSaveList.adapter = adapter
//            }
//        }


        binding.recyclerviewSaveList.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewSaveList.adapter = adapter
        return binding.root
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
                            .child("basic_info")
                            .get()
                            .await()
                            .getValue(RecipeBasicInfo::class.java)!!
                    )
                }
            }
        }.await()
        return list
    }

    private suspend fun getImageListByUploadList(uploadList: List<RecipeBasicInfo>): List<Uri> {
        val recipeRef= Firebase.storage.getReference("recipe_image")
        val list = mutableListOf<Uri>()
        val defaultImageUri = Uri.EMPTY
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
}