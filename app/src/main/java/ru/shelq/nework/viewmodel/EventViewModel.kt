package ru.shelq.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Post
import ru.shelq.nework.enumer.EventType
import ru.shelq.nework.model.FeedModel
import ru.shelq.nework.model.FeedModelState
import ru.shelq.nework.repository.EventRepository
import ru.shelq.nework.repository.EventRepositoryImpl
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.SingleLiveEvent
import java.util.Calendar

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = "",
    authorAvatar = "",
    datetime = AndroidUtils.calendarFormatDate(Calendar.getInstance()),
    published = "",
    content = "",
    likeOwnerIds = emptyList(),
    likedByMe = false,
    participantsIds = emptyList(),
    participatedByMe = false,
    speakerIds = emptyList(),
    type = EventType.ONLINE,
    users = emptyMap()
)

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository =
        EventRepositoryImpl(AppDb.getInstance(context = application).eventDao)
    val data: LiveData<FeedModel<Event>> = repository.data
        .map(::FeedModel)
        .asLiveData(Dispatchers.Default)

    val newerEventCount = data.switchMap {
        repository.getNewerEvent(it.data.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default)
    }
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    val edited = MutableLiveData(empty)
    val selectedEvent = MutableLiveData<Event?>()
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated


    init {
        loadEvent()
    }

    fun loadEvent() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getEventById(postId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            selectedEvent.value = repository.getEventById(postId)
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshEvents() = viewModelScope.launch {
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
                _eventCreated.value = Unit
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
        edited.value = empty
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun likeByEvent(event: Event) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(error = false)
            repository.likeByEvent(event)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun readNewEvents() = viewModelScope.launch {
        repository.readNewEvents()
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