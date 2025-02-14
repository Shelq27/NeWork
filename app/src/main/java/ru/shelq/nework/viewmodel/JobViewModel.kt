package ru.shelq.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.dto.Jobs
import ru.shelq.nework.model.FeedModel
import ru.shelq.nework.model.FeedModelState
import ru.shelq.nework.repository.JobRepository
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.SingleLiveEvent
import java.util.Calendar

private val empty = Jobs(
    id = 0,
    name = "",
    position = "",
    start = AndroidUtils.calendarToUTCDate(Calendar.getInstance()),
    finish = null,
    link = null,
    userId = 0
)

@HiltViewModel(assistedFactory = JobViewModel.Factory::class)
class JobViewModel @AssistedInject constructor(
    private val repository: JobRepository,
    auth: AppAuth,
    @Assisted userId: Long,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(userId: Long): JobViewModel
    }


    var data: LiveData<FeedModel<Jobs>>

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val newJob = MutableLiveData(empty)


    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    init {
        repository.setUser(userId)
        data = auth
            .authState
            .flatMapLatest { auth ->
                repository.data.map { jobs ->
                    FeedModel(
                        jobs.map { it.copy(ownedByMe = auth.id == it.userId) },
                        jobs.none { userId == it.userId }
                    )
                }
            }.asLiveData(Dispatchers.Default)
    }


    fun loadJobs() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun setName(name: String) {
        newJob.value = newJob.value?.copy(name = name)
    }

    fun setPosition(position: String) {
        newJob.value = newJob.value?.copy(position = position)
    }

    fun setStart(start: String) {
        newJob.value = newJob.value?.copy(start = start)
    }

    fun setFinish(finish: String?) {
        newJob.value = newJob.value?.copy(finish = finish)
    }

    fun setLink(link: String?) {
        newJob.value = newJob.value?.copy(link = link)
    }

    fun save() {
        newJob.value?.let {
            val job = it.copy()
            _jobCreated.value = Unit
            newJob.value = empty
            viewModelScope.launch {
                try {
                    repository.save(job)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun removeById(jobs: Jobs) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.removeById(jobs)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun resetError() {
        _dataState.value = FeedModelState()
    }
}