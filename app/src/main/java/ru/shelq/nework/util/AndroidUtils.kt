package ru.shelq.nework.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.shelq.nework.BuildConfig
import java.util.concurrent.TimeUnit

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}