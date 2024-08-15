package ru.shelq.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Event
import ru.shelq.nework.repository.EventRepository
import ru.shelq.nework.repository.EventRepositoryImpl

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "EventAutor",
    authorJob = "",
    authorAvatar = "",
    content = "",
    datetime = "event datetime",
    published = "event date pub",
    coords = "",
    type = "offline",
    likeOwnerIds = 0,
    likedByMe = false,
    participantsIds = 0,
    participatedByMe = false,
    attachment = "",
    link = "",
    users = "",
)

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository = EventRepositoryImpl(
        AppDb.getInstance(application).eventDao
    )
    val data = repository.getAll()
    private val edited = MutableLiveData(empty)
    fun likeById(id: Long) = repository.likeById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun changeContentAndSave(content: String) {
        val text = content.trim()
        edited.value?.let { event ->
            if (event.content != text) {
                edited.value = event.copy(content = text)
                repository.save(edited.value!!)
            }
        }
        edited.value = empty
    }

    fun edit(event: Event) {
        edited.value = event
    }


}