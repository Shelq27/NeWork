package ru.shelq.nework.viewmodel
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.shelq.nework.R
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.auth.AuthState
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.NetworkError
import ru.shelq.nework.error.UnknownError
import ru.shelq.nework.model.PhotoModel
import ru.shelq.nework.model.UserAuthResult
import ru.shelq.nework.util.SingleLiveEvent
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel
@Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val name: MutableLiveData<String> = MutableLiveData<String>()
    val login: MutableLiveData<String> = MutableLiveData<String>()
    val pass: MutableLiveData<String> = MutableLiveData<String>()


    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState>
        get() = _authState

    private val _userAuthResult = SingleLiveEvent<UserAuthResult>()
    val userAuthResult: LiveData<UserAuthResult>
        get() = _userAuthResult


    private val noPhoto = PhotoModel()
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun signUp() = viewModelScope.launch {
        try {
            var authResult = AuthState()
            when (_photo.value) {
                noPhoto -> authResult = registerUser(
                    name.value!!.toString(),
                    login.value!!.toString().trim(),
                    pass.value!!.toString().trim(),
                    null
                )

                else -> _photo.value?.file?.let { file ->
                    authResult = registerUser(
                        name.value!!.toString(),
                        login.value!!.toString().trim(),
                        pass.value!!.toString().trim(),
                        MediaUpload(file)
                    )
                }
            }
            if (authResult != null) {
                _authState.value = authResult
                _userAuthResult.value = UserAuthResult()
            }
        } catch (apiException: ApiError) {
            _userAuthResult.value =
                UserAuthResult(error = true, "${apiException.status}:${apiException.code}")
        } catch (e: Exception) {
            println(e.printStackTrace())
            _userAuthResult.value = UserAuthResult(error = true)
        }
    }

    private suspend fun registerUser(
        name: String,
        login: String,
        pass: String,
        upload: MediaUpload?
    ): AuthState {
        try {
            val response: retrofit2.Response<AuthState> = if (upload != null) {
                val media = MultipartBody.Part.createFormData(
                    "file", upload.file.name, upload.file.asRequestBody()
                )
                apiService.registerUserAvatar(
                    login.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    pass.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    name.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                    media
                )
            } else {
                apiService.registerUser(login, pass, name)
            }

            if (!response.isSuccessful) {
                var message = ""
                message = when (response.code()) {
                    403 -> context.getString(R.string.error_user_exist)
                    415 -> context.getString(R.string.error_format_image)
                    else -> response.message()
                }
                throw ApiError(response.code(), message)
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}