package ru.shelq.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.shelq.nework.dto.Post
import ru.shelq.nework.model.FeedModel
import ru.shelq.nework.repository.PostRepository
import ru.shelq.nework.repository.PostRepositoryImpl
import ru.shelq.nework.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "PostAutor",
    authorJob = "",
    authorAvatar = "",
    content = "",
    published = "post data pub",
    link = "",
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    users = emptyMap()
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    fun likeById(id: Long) = repository.likeById(id)
    fun removeById(id: Long) = repository.removeById(id)

    init {
        loadPost()
    }

     fun loadPost() {
        thread {
            _data.postValue(FeedModel(loading = true))
            repository.getAllAsync(object : PostRepository.GetPostsCallback {
                override fun onSuccess(posts: List<Post>) {
                    _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
                }

                override fun onError(throwable: Throwable) {
                    _data.value = FeedModel(error = true)
                }
            })
        }
    }

    fun changeContentAndSave(content: String) {
        thread {
            edited.value?.let { post ->
                val text = content.trim()
                if (post.content != text) {
                    repository.save(post)
                    edited.value = post.copy(content = text)
                    _postCreated.postValue(Unit)
                }

            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }


}