package ru.shelq.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Post
import ru.shelq.nework.model.FeedModel
import ru.shelq.nework.model.FeedModelState
import ru.shelq.nework.repository.PostRepository
import ru.shelq.nework.repository.PostRepositoryImpl
import ru.shelq.nework.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = "",
    authorAvatar = "",
    content = "",
    published = "",
    link = "",
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    users = emptyMap()
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao)

    val data: LiveData<FeedModel<Post>> = repository.data
        .map(::FeedModel)
        .asLiveData(Dispatchers.Default)


    val newerPostCount = data.switchMap {
        repository.getNewerPost(it.data.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default)
    }
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    val edited = MutableLiveData(empty)
    val selectedPost = MutableLiveData<Post?>()
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    init {
        loadPost()
    }

    fun loadPost() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getPostById(postId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            selectedPost.value = repository.getPostById(postId)
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changeContentAndSave(content: String) {
        val text = content.trim()
        edited.value?.let {
            if (it.content == text) {
                return
            }
            edited.value = it.copy(content = text)
        }
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState()
                _postCreated.value = Unit
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeByPost(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(error = false)
            repository.likeByPost(post)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun readNewPosts() = viewModelScope.launch {
        repository.readNewPosts()
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


}