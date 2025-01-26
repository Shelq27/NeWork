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
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.MediaUpload
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
private const val emptyDateTime = ""
private val defaultType = EventType.ONLINE


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
    private val _coords = MutableLiveData<Coordinates?>()
    val coords: LiveData<Coordinates?>
        get() = _coords
    private val _speakersNewEvent = MutableLiveData<List<Long>>(emptyList())
    val speakersNewEvent: LiveData<List<Long>>
        get() = _speakersNewEvent
    private val _changed = MutableLiveData<Boolean>()
    private val _datetime = MutableLiveData(emptyDateTime)
    val datetime: LiveData<String>
        get() = _datetime

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
            val newEvent = it.copy(
                datetime = _datetime.value!!,
                published = AndroidUtils.calendarToUTCDate(Calendar.getInstance()),
                coords = _coords.value,
                speakerIds = _speakersNewEvent.value!!,
                type = _eventType.value!!
            )
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_attachment.value) {
                        null -> {
                            repository.save(newEvent.copy(attachment = null))
                        }

                        else -> {

                            if (_attachment.value?.url != null) {
                                repository.save(newEvent)
                            } else {
                                _attachment.value!!.file?.let {
                                    repository.saveWithAttachment(
                                        newEvent,
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

    fun edit(event: Event?) {
        if (event != null) {
            edited.value = event
        } else {
            clearEdit()
        }
    }

    private fun clearEdit() {
        edited.value = empty
        _attachment.value = null
        _coords.value = null
        _changed.value = false
        _datetime.value = emptyDateTime
        _eventType.value = defaultType
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

    fun resetError() {
        _dataState.value = FeedModelState()
    }

    fun changeDateTime(dateTime: String) {
        _datetime.value = dateTime
        _changed.value = true
    }

    private val _eventType = MutableLiveData(defaultType)
    val eventType: LiveData<EventType>
        get() = _eventType

    fun changeType(eventType: EventType) {
        _eventType.value = eventType
        _changed.value = true
    }
}