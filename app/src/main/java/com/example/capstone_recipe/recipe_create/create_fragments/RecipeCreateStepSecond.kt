package com.example.capstone_recipe.recipe_create.create_fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone_recipe.UpdateValue
import com.example.capstone_recipe.recipe_create.create_adapter.ExplanationAdapter
import com.example.capstone_recipe.recipe_create.create_adapter.ItemTouchHelperCallback
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.FragmentRecipeCreateStepSecondBinding
import java.text.SimpleDateFormat

class RecipeCreateStepSecond(ExplanationList: List<String>, ImageList: List<Uri?>) : Fragment() {
    private lateinit var storagePermission: ActivityResultLauncher<String>  // 저장소 권한
    private lateinit var galleryLauncher: ActivityResultLauncher<String>    //갤러리
    private lateinit var binding: FragmentRecipeCreateStepSecondBinding
    private lateinit var context: Context
    private lateinit var adapter: ExplanationAdapter
    private val stepExplanationList: MutableList<String> = ExplanationList.toMutableList()
    private val stepImageList:MutableList<Uri?> = ImageList.toMutableList()
    private lateinit var itemTouchHelper : ItemTouchHelper  // 아이템 드래그 용
    private var recipeStepImageView: ImageView? = null      // 어댑터에서 galleryLauncher 호출하여 이미지를 바꾸기 위하여
    private var recipeStepImagePosition = 0                 // 이미지가 몇번째 단계의 이미지인지
    private var beforeImageUri: Uri? = null              // 이미지 변경 전 - 이미지 선택 취소 시 beforeImage 로 저장
    private lateinit var updateCollBack: UpdateValue

    override fun onAttach(context: Context) {
        super.onAttach(context)
        updateCollBack = context as UpdateValue
    }

    override fun onStop() {
        super.onStop()
        updateCollBack.updateStepExplanationList(stepExplanationList)
    }

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
                stepImageList[recipeStepImagePosition] = uri
            }
            updateCollBack.updateStepImageList(stepImageList)
            updateCollBack.updateStepExplanationList(stepExplanationList)
            adapter.notifyItemChanged(recipeStepImagePosition)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeCreateStepSecondBinding.inflate(inflater, container, false)
        context = binding.root.context

        adapter = ExplanationAdapter(this@RecipeCreateStepSecond, stepExplanationList, stepImageList)
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerviewCreateExplanation)

        binding.recyclerviewCreateExplanation.adapter = adapter
        binding.recyclerviewCreateExplanation.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        return binding.root
    }

    fun setImageFromGallery(targetView: AppCompatImageView, targetViewPos: Int){
        // 어댑터에서 해당 함수 호출 시 권한 확인 후 (권한 없으면 다시 신청)
        // 이미지를 바꿀 대상을 recipeStepImage 에 저장 -> 해당 이미지가 몇번째 단계인지 recipeStepImagePosition 에 저장
        // 해당 이미지의 uri 를 beforeImageUri 에 저장
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { ContextCompat.checkSelfPermission(context,Manifest.permission.READ_MEDIA_IMAGES) }
                        else { ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) }

        if(permission == PackageManager.PERMISSION_DENIED){ requestPermission() }
        else{
            recipeStepImageView = targetView
            recipeStepImagePosition = targetViewPos
            beforeImageUri = stepImageList[recipeStepImagePosition]
            galleryLauncher.launch("image/*")
        }
    }
    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { storagePermission.launch(Manifest.permission.READ_MEDIA_IMAGES) }
        else{ storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE) }
    }
}
