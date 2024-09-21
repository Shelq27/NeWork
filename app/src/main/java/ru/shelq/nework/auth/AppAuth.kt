package ru.shelq.nework.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(@ApplicationContext private val  context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow(
        AuthState(
            prefs.getLong(KEY_ID, 0L),
            prefs.getString(KEY_TOKEN, null)
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }


    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"

        @Volatile
        private var instance: AppAuth? = null

        fun getInstance() = synchronized(this) {
            instance
                ?: throw IllegalStateException("getInstance should be called only after initApp")
        }

        fun initApp(context: Context) = instance ?: synchronized(this) {
            instance ?: AppAuth(context).also { instance = it }
        }
    }
}

data class AuthState(val id: Long = 0L, val token: String? = null)