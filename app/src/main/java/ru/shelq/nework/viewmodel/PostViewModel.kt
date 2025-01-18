package ru.shelq.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.dto.Post
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.error.AppError
import ru.shelq.nework.model.AttachmentModel
import ru.shelq.nework.model.FeedModelState
import ru.shelq.nework.repository.PostRepository
import ru.shelq.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

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
private val noAttachment: AttachmentModel? = null

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth
        .authState
        .flatMapLatest { auth ->
            repository.data
                .map { posts ->
                    posts.map { it.copy(ownedByMe = auth.id == it.authorId) }

                }
        }
        .catch { it.printStackTrace() }


    @OptIn(ExperimentalCoroutinesApi::class)
    val newerPostCount: Flow<Int> = data.flatMapLatest {
        repository.getNewerPost(repository.latestReadPostId())
            .catch { e -> throw AppError.from(e) }
            .flowOn(Dispatchers.Default)
    }
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    val edited = MutableLiveData(empty)
    val selectedPost = MutableLiveData<Post?>()
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _attachment = MutableLiveData(noAttachment)
    val attachment: LiveData<AttachmentModel?>
        get() = _attachment
    private val _changed = MutableLiveData<Boolean>()

    init {
        loadPost()
    }

    private fun loadPost() = viewModelScope.launch {
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

    fun changeAttachment(url: String?, uri: Uri?, file: File?, attachmentType: AttachmentType?) {
        if (uri == null) {
            if (url != null) { //редактирование поста с вложением
                _attachment.value = AttachmentModel(url, null, null, attachmentType)
            } else {
                _attachment.value = null //удалили вложение
            }
        } else {
            _attachment.value = AttachmentModel(null, uri, file, attachmentType)
        }
        _changed.value = true
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeByPost(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(error = false)
            repository.likeById(post)
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