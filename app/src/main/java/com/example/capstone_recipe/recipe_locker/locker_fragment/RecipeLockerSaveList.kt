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
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.User
import com.example.capstone_recipe.databinding.FragmentRecipeLockerSaveListBinding
import com.example.capstone_recipe.databinding.FragmentRecipeLockerUploadListBinding
import com.example.capstone_recipe.recipe_locker.locker_adpater.LockerRecipeViewerAdapter
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecipeLockerSaveList(private val userInfo:User) : Fragment() {

    private lateinit var binding: FragmentRecipeLockerSaveListBinding
    private lateinit var adapter: LockerRecipeViewerAdapter
    private lateinit var context: Context
    private val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.head_default_image}")!! }
    private var creatorNameList = listOf<String>()
    private var saveList = listOf<RecipeBasicInfo>()
    private var imageList = listOf<Uri>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeLockerSaveListBinding.inflate(inflater, container, false)
        adapter = LockerRecipeViewerAdapter()
        context = binding.root.context

        lifecycleScope.launch(Dispatchers.IO) {
            async {
                saveList = getRecipeInfoListByIds(userInfo.saveRecipe)
                imageList = getImageListByUploadList(saveList)
            }.await()
            creatorNameList = async { getCreatorListByIds(userInfo.saveRecipe) }.await()
            withContext(Dispatchers.Main){
                adapter.recipeList = saveList
                adapter.creatorIdNameList = creatorNameList
                adapter.imageList = imageList
                binding.recyclerviewSaveList.adapter = adapter
            }
        }


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