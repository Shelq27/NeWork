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
import kotlinx.coroutines.Job
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
import ru.shelq.nework.dto.User
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
private var getEventJob: Job? = null
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
    val edited = MutableLiveData(empty)

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
    val changed: LiveData<Boolean>
        get() = _changed
    private val _datetime = MutableLiveData(emptyDateTime)
    val datetime: LiveData<String>
        get() = _datetime

    private val _likers = MutableLiveData<List<User>>(emptyList())
    val likers: LiveData<List<User>>
        get() = _likers

    private val _likersLoaded = SingleLiveEvent<Unit>()
    val likersLoaded: LiveData<Unit>
        get() = _likersLoaded

    private val _speakers = MutableLiveData<List<User>>(emptyList())
    val speakers: LiveData<List<User>>
        get() = _speakers

    private val _speakersLoaded = SingleLiveEvent<Unit>()
    val speakersLoaded: LiveData<Unit>
        get() = _speakersLoaded

    private val _participants = MutableLiveData<List<User>>(emptyList())
    val participants: LiveData<List<User>>
        get() = _participants

    private val _participantsLoaded = SingleLiveEvent<Unit>()
    val participantsLoaded: LiveData<Unit>
        get() = _participantsLoaded


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
        getEventJob?.cancel()
        getEventJob = viewModelScope.launch {
            try {
                repository.getEventById(postId).collect {
                    selectedEvent.value = it
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
        _speakersNewEvent.value = emptyList()
        _datetime.value = emptyDateTime
        _eventType.value = defaultType
    }

    fun reset() {
        _changed.value = false
        selectedEvent.value = null
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
            if (url != null) {
                _attachment.value = AttachmentModel(url, null, null, attachmentType)
            } else {
                _attachment.value = null
            }
        } else {
            _attachment.value = AttachmentModel(null, uri, file, attachmentType)
        }
        _changed.value = true
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
    fun participateByEvent(event: Event) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.participateById(event)
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

    fun changeSpeakersNewEvent(list: List<Long>) {

        _speakersNewEvent.value = list

        _changed.value = true
    }

    fun chooseUser(user: User) {
        _speakersNewEvent.value = speakersNewEvent.value?.plus(user.id)
        _changed.value = true
    }

    fun removeUser(user: User) {
        _speakersNewEvent.value = speakersNewEvent.value?.filter { it != user.id }
        _changed.value = true
    }

    fun getLikers(event: Event) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            _likers.value = repository.getLikers(event)
            _likersLoaded.value = Unit
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            println(e.stackTrace)
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun getSpeakers(event: Event) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            _speakers.value = repository.getSpeakers(event)
            _speakersLoaded.value = Unit
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }



    fun getParticipants(event: Event) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            _participants.value = repository.getParticipants(event)
            _participantsLoaded.value = Unit
            _dataState.value = FeedModelState()


        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)

        }
    }
    fun changeCoords(coords: Coordinates?) {
        _coords.value = coords
        _changed.value = true
    }
}