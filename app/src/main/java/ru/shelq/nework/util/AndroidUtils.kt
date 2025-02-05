@file:Suppress("DEPRECATION")

package ru.shelq.nework.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import ru.shelq.nework.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
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

    fun dateRangeToText(dateStartStr: String, dateEndStr: String?): String {
        val dateStartRes = dateUTCToLongDateText(dateStartStr)
        val dateEndRes = if (dateEndStr == null) "НВ" else dateUTCToLongDateText(dateEndStr)
        return ("$dateStartRes - $dateEndRes")
    }

    private fun dateUTCToLongDateText(dateStr: String): String {
        val date = ZonedDateTime.parse(dateStr).withZoneSameInstant(ZoneId.systemDefault())
        return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    }


    fun share(context: Context, content: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }

        val shareIntent =
            Intent.createChooser(intent, context.getString(R.string.share))
        startActivity(context, shareIntent, null)
    }

    fun showSignInDialog(fragment: Fragment) {
        val dialog = SignInDialogFragment()
        dialog.show(fragment.parentFragmentManager, fragment.getString(R.string.authentication))
    }

    fun dateUTCToCalendar(dateStr: String): Calendar {
        val zonedDateTime = ZonedDateTime.parse(dateStr)
        val date = Date.from(zonedDateTime.toInstant())
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    @SuppressLint("SimpleDateFormat")
    fun calendarToUTCDate(calendar: Calendar): String {
        val date = calendar.time
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }


    fun Uri.getFile(context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(this)
        val tempFile = File.createTempFile("temp", ".jpg")

        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun createBitmapFromVector(context: Context, art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, art) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun moveCamera(mapView: MapView, point: Point, zoom: Float = 16.5f) {
        mapView.map.move(
            CameraPosition(point, zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )
    }

    fun addMarkerOnMap(context: Context, mapView: MapView, point: Point): MapObject {
        val markerPin = createBitmapFromVector(context, R.drawable.ic_location_point_24dp)
        val marker = mapView.map.mapObjects.addPlacemark(point, ImageProvider.fromBitmap(markerPin))
        marker.opacity = 0.5f //
        return marker
    }

}

