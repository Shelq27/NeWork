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
import ru.shelq.nework.dto.Event
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.enumer.EventType
import ru.shelq.nework.error.AppError
import ru.shelq.nework.model.AttachmentModel
import ru.shelq.nework.model.FeedModelState
import ru.shelq.nework.repository.EventRepository
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.SingleLiveEvent
import java.io.File
import java.util.Calendar
import javax.inject.Inject

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
private val noAttachment: AttachmentModel? = null

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Event>> = appAuth
        .authState
        .flatMapLatest { auth ->
            repository.data
                .map { events ->
                    events.map { it.copy(ownedByMe = auth.id == it.authorId) }

                }
        }
        .catch { it.printStackTrace() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val newerEventCount: Flow<Int> = data.flatMapLatest {
        repository.getNewerEvent(repository.latestReadEventId())
            .catch { e -> throw AppError.from(e) }
            .flowOn(Dispatchers.Default)
    }



    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val edited = MutableLiveData(empty)
    val selectedEvent = MutableLiveData<Event?>()
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated
    private val _attachment = MutableLiveData(noAttachment)
    val attachment: LiveData<AttachmentModel?>
        get() = _attachment
    private val _changed = MutableLiveData<Boolean>()

    init {
        loadEvent()
    }

    private fun loadEvent() = viewModelScope.launch {
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
    fun edit(event: Event) {
        edited.value = event
    }

    fun likeByEvent(event: Event) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(error = false)
            repository.likeById(event)
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
    fun resetError(){
        _dataState.value = FeedModelState()
    }
}