package ru.shelq.nework.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.shelq.nework.R
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun ImageView.loadImgCircle(url: String?) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_downloading_100dp)
            .error(R.drawable.ic_error_outline_100dp)
            .timeout(10_0000)
            .circleCrop()
            .into(this)
    }

    fun dateFormatToText(dateStr: String, context: Context): String {
        val date = ZonedDateTime.parse(dateStr).withZoneSameInstant(ZoneId.systemDefault())
        return date.format(DateTimeFormatter.ofPattern(context.getString(R.string.date_format)))
    }
    @SuppressLint("SimpleDateFormat")
    fun calendarFormatDate(calendar: Calendar): String{
        val date = calendar.time
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }


}

