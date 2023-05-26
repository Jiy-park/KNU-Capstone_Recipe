package com.example.capstone_recipe.create_test.create_fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.R
import com.example.capstone_recipe.create_test.ItemTouchHelperCallback
import com.example.capstone_recipe.create_test.RecipeStepAdapter
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepSecondBinding

class RecipeCreateStepSecond(_recipeStepList: List<RecipeStep>, _recipeStepImageUriList: List<Uri>) : Fragment() {
    private lateinit var binding: FragmentRecipeCreateStepSecondBinding
    private lateinit var context: Context
    private lateinit var recipeStepAdapter: RecipeStepAdapter
    private lateinit var itemTouchHelper : ItemTouchHelper

    private var recipeStepList = _recipeStepList.toMutableList()
    private var recipeStepImageUriList = _recipeStepImageUriList.toMutableList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepSecondBinding.inflate(inflater, container, false)
        context = binding.root.context
        recipeStepAdapter = RecipeStepAdapter(this)

        binding.recyclerviewCreateExplanation.adapter = recipeStepAdapter
        binding.recyclerviewCreateExplanation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recipeStepAdapter.updateRecipeStepInfo(recipeStepList, recipeStepImageUriList)

        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(recipeStepAdapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerviewCreateExplanation)


        return binding.root
    }

    fun buildRecipeStepList() = recipeStepAdapter.getRecipeStepList()
    fun buildRecipeImageUriList() = recipeStepImageUriList


    private lateinit var storagePermission: ActivityResultLauncher<String>  // 저장소 권한
    private lateinit var galleryLauncher: ActivityResultLauncher<String>    //갤러리
    private var recipeStepImageView: ImageView? = null
    private var recipeStepImagePosition = -1
    private var beforeImageUri: Uri = Uri.EMPTY

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted-> // 갤러리 권한 요청
            if(isGranted == false) { Toast.makeText(context, "권한을 승인해야 레시피 제작 시 이미지를 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show() }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri-> // 갤러리 열기
            if(uri == null){        // 사용자가 이미지 선택 취소 -> 기존 이미지로 변경
                recipeStepImageView?.setImageURI(beforeImageUri)
            }
            else{                   // 사용자가 새로운 이미지 선택 -> 해당 이미지로 변경
                recipeStepImageView?.setImageURI(uri)
                recipeStepImageUriList[recipeStepImagePosition] = uri
            }
            recipeStepAdapter.notifyItemChanged(recipeStepImagePosition)
        }
    }

    /** *
     * 어댑터에서 해당 함수 호출 시 권한 확인 후 (권한 없으면 다시 신청)
     * 이미지를 바꿀 대상을 recipeStepImage 에 저장 -> 해당 이미지가 몇번째 단계인지 recipeStepImagePosition 에 저장
     * 해당 이미지의 uri 를 beforeImageUri 에 저장*/
    fun setImageFromGallery(targetView: AppCompatImageView, targetViewPos: Int){
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { ContextCompat.checkSelfPermission(context,
            Manifest.permission.READ_MEDIA_IMAGES) }
        else { ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) }

        if(permission == PackageManager.PERMISSION_DENIED){ requestPermission() }
        else{
            recipeStepImageView = targetView
            recipeStepImagePosition = targetViewPos
            beforeImageUri = recipeStepImageUriList[recipeStepImagePosition]
            galleryLauncher.launch("image/*")
        }
    }
    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { storagePermission.launch(Manifest.permission.READ_MEDIA_IMAGES) }
        else{ storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE) }
    }

}