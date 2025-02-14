package ru.shelq.nework.util

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ru.shelq.nework.R
import ru.shelq.nework.dto.Attachment

class MediaLifecycleObserver : LifecycleEventObserver {

    var mediaPlayer: MediaPlayer? = MediaPlayer()
    var runnable: Runnable? = null
    private var handler = Handler(Looper.getMainLooper())
    private var playButton: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var pause: Boolean = false
    private lateinit var seekBarListener: SeekBar.OnSeekBarChangeListener

    private fun pause() {
        mediaPlayer?.pause()
        playButton?.setBackgroundResource(R.drawable.ic_play_48dp)
    }

    fun stop() {
        mediaPlayer?.stop()
        playButton?.setBackgroundResource(R.drawable.ic_play_48dp)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_STOP -> {
                mediaPlayer?.release()
                mediaPlayer = null
                runnable?.takeIf { // Берём, если

                    handler.hasCallbacks(it) // если есть каллбеки

                }

                    ?.let { // если не null и есть каллбеки

                        handler.removeCallbacks(it)

                    }

            }

            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> Unit
        }
    }

    fun playAudio(attachment: Attachment?, seekBar: SeekBar?, playButton: ImageButton?) {
        if (attachment == null) {
            return
        }
        if (seekBar == null) {
            return
        }
        if (playButton == null) {
            return
        }
        this.playButton = playButton
        this.seekBar = seekBar
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }
        if (!mediaPlayer!!.isPlaying) {
            if (pause) {
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition)
                mediaPlayer!!.start()
                playButton.setBackgroundResource(R.drawable.ic_pause_48dp)
                pause = false
                attachment.isPlaying = true
            } else {

                mediaPlayer!!.reset()
                try {
                    mediaPlayer!!.setDataSource(attachment.url)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mediaPlayer!!.prepareAsync()
                mediaPlayer!!.setOnPreparedListener {
                    it.setVolume(15F, 15F)
                    it.start()
                    playButton.setBackgroundResource(R.drawable.ic_pause_48dp)
                    seekBar.progress = 0
                    seekBar.max = it.duration
                    attachment.isPlaying = true
                }
            }
        } else {
            pause()
            pause = true
            attachment.isPlaying = false
        }
        seekbar(attachment)


    }

    fun playAudioFromDescriptor(
        fileDescriptor: AssetFileDescriptor,
        seekBar: SeekBar,
        playButton: ImageButton
    ) {
        this.playButton = playButton
        this.seekBar = seekBar
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        if (!mediaPlayer!!.isPlaying) {
            if (pause) {
                mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition)
                mediaPlayer!!.start()
                playButton.setBackgroundResource(R.drawable.ic_pause_48dp)
                pause = false
            } else {
                mediaPlayer!!.reset()
                //загрузка трека
                try {
                    mediaPlayer!!.setDataSource(fileDescriptor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mediaPlayer!!.prepareAsync()
                mediaPlayer!!.setOnPreparedListener {
                    it.setVolume(15F, 15F)
                    it.start()
                    playButton.setBackgroundResource(R.drawable.ic_pause_48dp)
                    seekBar.progress = 0
                    seekBar.max = it.duration
                }
            }
        } else {
            pause()
            pause = true
        }
        seekbar(null)

    }

    private fun seekbar(attachment: Attachment?) {
        if (mediaPlayer != null) {
            seekBarListener = SeekBarListener(mediaPlayer as MediaPlayer)
            seekBar?.setOnSeekBarChangeListener(seekBarListener)
            runnable = Runnable {
                seekBar?.progress = mediaPlayer?.currentPosition ?: 0
                attachment?.progress = seekBar?.progress ?: 0
                handler.let { seekBar?.postDelayed(runnable, 1000) }
            }
            seekBar?.postDelayed(runnable!!, 1000)
            mediaPlayer?.setOnCompletionListener {

                mediaPlayer?.pause()
                playButton?.setBackgroundResource(R.drawable.ic_play_48dp)
            }
        }
    }

    fun seekSet(seekBar: SeekBar) {
        this.seekBar = seekBar
    }
}

class SeekBarListener(private val mediaPlayer: MediaPlayer) : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p2) {
            mediaPlayer.seekTo(p1)
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

}