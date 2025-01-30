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
import ru.shelq.nework.dto.Coordinates
import kotlinx.coroutines.Job
import ru.shelq.nework.dto.Jobs
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.Post
import ru.shelq.nework.dto.User
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.error.AppError
import ru.shelq.nework.model.AttachmentModel
import ru.shelq.nework.model.FeedModelState
import ru.shelq.nework.repository.PostRepository
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.SingleLiveEvent
import java.io.File
import java.util.Calendar
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
private var getPostJob: Job? = null

@HiltViewModel
open class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    open var data: Flow<PagingData<Post>> = appAuth.authState
        .flatMapLatest { (myId, _) ->
            repository.data.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == myId)
                }
            }
        }


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

    private val _attachment = MutableLiveData(noAttachment)
    val attachment: LiveData<AttachmentModel?>
        get() = _attachment

    private val _changed = MutableLiveData<Boolean>()
    val changed: LiveData<Boolean>
        get() = _changed

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

//    Для карт
    private val _coords = MutableLiveData<Coordinates?>()
    val coords: LiveData<Coordinates?>
        get() = _coords

    private val _mentioned = MutableLiveData<List<User>>(emptyList())
    val mentioned: LiveData<List<User>>
        get() = _mentioned

    private val _mentionedNewPost = MutableLiveData<List<Long>>(emptyList())
    val mentionedNewPost: LiveData<List<Long>>
        get() = _mentionedNewPost



    private val _mentionedLoaded = SingleLiveEvent<Unit>()
    val mentionedLoaded: LiveData<Unit>
        get() = _mentionedLoaded

    private val _likers = MutableLiveData<List<User>>(emptyList())
    val likers: LiveData<List<User>>
        get() = _likers

    private val _likersLoaded = SingleLiveEvent<Unit>()
    val likersLoaded: LiveData<Unit>
        get() = _likersLoaded


    init {
        loadPost()
    }

    fun chooseUser(user: User) {
        _mentionedNewPost.value = _mentionedNewPost.value?.plus(user.id)
        _changed.value = true
    }

    fun removeUser(user: User) {
        _mentionedNewPost.value = _mentionedNewPost.value?.filter { it != user.id }
        _changed.value = true
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
        getPostJob?.cancel()
        getPostJob = viewModelScope.launch {
            try {
                repository.getPostById(postId).collect {
                    selectedPost.value = it
                }
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
        _changed.value = true
    }

    fun save() {
        edited.value?.let {
            val newPost = it.copy(
                published = AndroidUtils.calendarToUTCDate(Calendar.getInstance()),
                coords = _coords.value,
                mentionIds = _mentionedNewPost.value!!
            )
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_attachment.value) {
                        null -> {
                            repository.save(newPost.copy(attachment = null))
                        }

                        else -> {

                            if (_attachment.value?.url != null) {
                                repository.save(newPost)
                            } else {
                                _attachment.value!!.file?.let {
                                    repository.saveWithAttachment(
                                        newPost,
                                        MediaUpload(_attachment.value!!.file!!),
                                        _attachment.value!!.attachmentType!!
                                    )
                                }
                            }
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        clearEdit()
    }

    fun changeLink(link: String) {
        val text = link.trim()
        if (edited.value?.link == text) {
            return
        }
        if (text == "") {
            edited.value = edited.value?.copy(link = null)
        } else {
            edited.value = edited.value?.copy(link = text)
        }
        _changed.value = true
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

    fun edit(post: Post?) {
        if (post != null) {
            edited.value = post
        } else {
            clearEdit()
        }
    }

    private fun clearEdit() {
        edited.value = empty
        _attachment.value = null
        _coords.value = null
        _mentionedNewPost.value = emptyList()
        _changed.value = false

    }

    fun changeMentionedNewPost(list: List<Long>) {
        _mentionedNewPost.value = list
        _changed.value = true
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

    fun resetError() {
        _dataState.value = FeedModelState()
    }


    fun getMentioned(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            _mentioned.value = repository.getMentioned(post)
            _mentionedLoaded.value = Unit
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }



    fun getLikers(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            _likers.value = repository.getLikers(post)
            _likersLoaded.value = Unit
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            println(e.stackTrace)
            _dataState.value = FeedModelState(error = true)
        }
    }


}