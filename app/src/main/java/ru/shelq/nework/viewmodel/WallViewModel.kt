package ru.shelq.nework.viewmodel

import androidx.paging.PagingData
import androidx.paging.map
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.dto.Post
import ru.shelq.nework.repository.PostRepository
import ru.shelq.nework.repository.PostRepositoryUserWall

@HiltViewModel(assistedFactory = WallViewModel.Factory::class)
class WallViewModel @AssistedInject constructor(
    @PostRepositoryUserWall private val repository: PostRepository,
    auth: AppAuth,
    @Assisted private val userId: Long,
) : PostViewModel(repository, auth) {

    override var data: Flow<PagingData<Post>>

    @AssistedFactory
    interface Factory {
        fun create(userId: Long): WallViewModel
    }

    init {
        repository.setUser(userId)
        @OptIn(ExperimentalCoroutinesApi::class)
        data = auth.authState
            .flatMapLatest { (myId, _) ->
                repository.data.map { pagingData ->
                    pagingData.map { post ->
                        post.copy(ownedByMe = post.authorId == myId)
                    }
                }
            }
    }


}