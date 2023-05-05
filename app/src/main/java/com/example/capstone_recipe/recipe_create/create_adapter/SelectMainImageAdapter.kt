package com.example.capstone_recipe.recipe_create.create_adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone_recipe.R
import com.example.capstone_recipe.recipe_create.create_fragments.RecipeCreateStepThird
import com.example.capstone_recipe.data_class.RecipeBasicInfo
import com.example.capstone_recipe.data_class.RecipeStep
import com.example.capstone_recipe.databinding.ItemSelectMainImageBinding
import java.text.SimpleDateFormat

class SelectMainImageAdapter(private val parent: RecipeCreateStepThird, private val stepImageList: List<Uri?>):RecyclerView.Adapter<SelectMainImageAdapter.Holder>() {

    private lateinit var binding: ItemSelectMainImageBinding
    private lateinit var context: Context
    private lateinit var checkedView: View
    private var checkedId = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding = ItemSelectMainImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = binding.root.context
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.settingClick(stepImageList[position])
    }

    override fun getItemCount() = stepImageList.size

    inner class Holder(val binding: ItemSelectMainImageBinding):RecyclerView.ViewHolder(binding.root){
        fun settingClick(imageUri: Uri?){
            val frameId = View.generateViewId()
            val packageName = "com.example.capstone_recipe"                                         //사용자가 선택한 이미지가
            val uri = Uri.parse("android.resource://$packageName/${R.drawable.ex_img}")     // 없는 경우 기본 이미지로 대체
            binding.frameChecked.id = frameId
            if(imageUri == null) { binding.ivMainImage.setImageURI(uri) }
            else { binding.ivMainImage.setImageURI(imageUri) }

            if(checkedId == -1){ // 처음 실행 시 첫번째 사진을 대표 이미지로 선정
                checkedView = itemView
                checkedId = frameId
                binding.frameChecked.visibility = View.VISIBLE
                if(imageUri == null) { parent.mainImageUri = uri }
                else { parent.mainImageUri = imageUri }
            }
            binding.ivMainImage.setOnClickListener {
                if(checkedId != frameId){ // 이미지 선택시 해당 이미지를 대표 이미지로 선정
                    val frame = checkedView.findViewById<FrameLayout>(checkedId)
                    frame.visibility = View.INVISIBLE
                    checkedView = itemView
                    checkedId = frameId
                    binding.frameChecked.visibility = View.VISIBLE
                }
                if(imageUri == null) { parent.mainImageUri = uri }
                else { parent.mainImageUri = imageUri }
            }
        }
        // 대표 이미지 : recipe_image/$recipeId/main/$userId_$recipeId_$yyyyMMdd_HHmm.jpeg
//        @SuppressLint("SimpleDateFormat")
//        fun makeMainImageFilePath(userId:String, uri: Uri): String { // 스텝 별 이미지
//            val mimeType = activity.contentResolver?.getType(uri) ?: "/none" //마임타입 ex) images/jpeg
//            val ext = mimeType.split("/")[1] //확장자 ex) jpeg
//            return "${userId}_main.$ext" // 파일 경로
//        }
    }
}