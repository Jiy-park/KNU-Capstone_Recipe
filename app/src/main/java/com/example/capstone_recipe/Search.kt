package com.example.capstone_recipe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.capstone_recipe.databinding.ActivitySearchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class Search : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private lateinit var context: Context
    private val db = Firebase.database("https://knu-capstone-f9f55-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .reference
    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = binding.root.context
        setViewEvent()


    }
    /**
     * *뷰 이벤트 정의
     * * 뒤로 가기 키
     * * 키보드 검색 키
     * */
    private fun setViewEvent() = binding.run {
        layerTopPanel.btnBack.setOnClickListener { finish() }
        editSearch.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                val searchTarget = binding.editSearch.text.toString()
                search(searchTarget)
                true
            }
            else { false}
        }
    }

    private fun search(search: String){
        Toast.makeText(context, search, Toast.LENGTH_SHORT).show()
        val d = binding.editSearch.text.toString()
        db.orderByChild("score").startAt(0.0).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    Log.d("LOG_CHECK", "Search :: onDataChange() -> ${snapshot.value}")

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
//        val query = db.orderByValue().startAt(search).endAt(search + "\uf8ff")
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (data in snapshot.children) {
//                    // 검색 결과 처리
//                    val result = data.value.toString()
//                    Log.d("LOG_CHECK", "검색 결과: $result")
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // 검색 취소 처리
//                Log.w("LOG_CHECK", "검색 취소됨", error.toException())
//            }
//        })


    }
}