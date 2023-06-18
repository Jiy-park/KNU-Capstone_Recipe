package com.example.capstone_recipe.search.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.capstone_recipe.R
import com.example.capstone_recipe.data_class.UserInfo
import com.example.capstone_recipe.databinding.FragmentSearchTabViewerBinding
import com.example.capstone_recipe.search.adapter.SearchUserAdapter
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class SearchUserFragment : Fragment() {
    private var binding: FragmentSearchTabViewerBinding? = null
    private lateinit var context: Context
    private var searchUserAdapter =  SearchUserAdapter()

    private val db = Firebase.database
    private val storage = Firebase.storage

    private var isSearching = false
    private var noResult = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchTabViewerBinding.inflate(inflater,container, false)
        context = binding!!.root.context

        Glide.with(context)
            .load(R.drawable.progress)
            .into(binding!!.ivProgressImage)

        binding!!.recyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = searchUserAdapter
        }

        lifecycleScope.launch { updateSearchingView(isSearching, noResult)  }

        return binding!!.root
    }

    suspend fun startSearch(searchTarget: String, callback: (size: Int) -> Unit){
        updateSearchingView(_isSearching = true)
        val userIdList = searchUserByName(searchTarget)
        val userInfoList = getUsersInfo(userIdList)
        withContext(Dispatchers.Main) {
            searchUserAdapter.updateUserList(userInfoList)
            updateSearchingView(_isSearching = false, _noResult = userInfoList.isEmpty())
            callback(userInfoList.size)
        }
    }

    private suspend fun updateSearchingView(_isSearching: Boolean, _noResult: Boolean = false) = withContext(Dispatchers.Main){
        binding?.let {
            if(_isSearching) {
                it.recyclerview.visibility = View.GONE
                it.tvResultSearch.visibility = View.GONE
                it.progress.visibility = View.VISIBLE
            }
            else {
                it.progress.visibility = View.GONE
                it.recyclerview.visibility = View.VISIBLE
                if(_noResult) { it.tvResultSearch.visibility = View.VISIBLE }
                else { it.tvResultSearch.visibility = View.GONE }
            }
        }?: run{
            isSearching = _isSearching
            noResult = _noResult
        }
    }


    /** * 입력받은 name을 갖는 유저의 아이디를 리스트 형태로 반환*/
    private suspend fun searchUserByName(name: String): List<String>{
        if(name.isEmpty()){ return emptyList() }
        val foundList = mutableListOf<String>()
        val mutex = Mutex()
        withContext(Dispatchers.IO){
            db.getReference("users").get().await().children.map { userId ->
                async {
                    val userName = userId.child("name").value.toString()
                    if(userName.contains(name)) {
                        mutex.withLock {
                            foundList.add(userId.child("id").value.toString())
                        }
                    }
                }
            }.awaitAll()
        }
        return foundList
    }

    /** *유저 아이디 리스트를 받아 해당 아이디에 맞는 유저들 정보 (: 이름, 아이디, 프로필 이미지, 친구 확인) 를 찾아 리스트 형태로 반납 */
    private suspend fun getUsersInfo(userIdsList: List<String>):List<UserInfo>{
        val userInfoList = mutableListOf<UserInfo>()
        val userRef = db.getReference("users")
//        val userId = Preference(context).getUserId() // 디바이스 주인 == 검색한 주체
        val mutex = Mutex()

        withContext(Dispatchers.IO){
            userIdsList.map { id->
                async {
                    val name = userRef.child(id).child("name").get().await().value.toString()
                    val profilePath = userRef.child(id).child("profileImagePath").get().await().value.toString()
                    val profileUri = getFriendImageByPath(id, profilePath)
                    mutex.withLock{
                        userInfoList.add(UserInfo(id, name, profileUri))
                    }
                }
            }.awaitAll()
        }
        return userInfoList
    }

    /** *유저의 아이디와, 프로필 이미지 경로를 받아옴-> 해당 이미지를 반납 */
    private suspend fun getFriendImageByPath(userId: String, imagePath: String): Uri {
        val defaultImageUri by lazy { Uri.parse("android.resource://${context.packageName}/${R.drawable.default_user_profile_image}")!! }
        val userImageRef = storage.getReference("user_image")
        return if(imagePath.isNotEmpty()){ userImageRef.child(userId).child("profile").child(imagePath).downloadUrl.await() }
        else { defaultImageUri }
    }
}