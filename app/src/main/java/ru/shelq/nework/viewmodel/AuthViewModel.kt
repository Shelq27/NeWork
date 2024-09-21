package ru.shelq.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.auth.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(auth: AppAuth) : ViewModel() {
    val data: LiveData<AuthState> = auth
        .authState
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = AppAuth.getInstance().authState.value.id != 0L
}