package ru.shelq.nework.error

import android.database.SQLException
import java.io.IOException

sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code)
data object NetworkError : AppError("error_network") {
    private fun readResolve(): Any = NetworkError
}

data object DbError : AppError("error_db") {
    private fun readResolve(): Any = DbError
}

data object UnknownError : AppError("error_unknown") {
    private fun readResolve(): Any = UnknownError
}