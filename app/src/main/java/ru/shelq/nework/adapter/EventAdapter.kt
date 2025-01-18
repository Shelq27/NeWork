package ru.shelq.nework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.shelq.nework.R
import ru.shelq.nework.databinding.EventCardBinding
import ru.shelq.nework.dto.Event
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.enumer.EventType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.MediaLifecycleObserver

interface EventOnInteractionListener {
    fun onLike(event: Event) {}
    fun onRemove(event: Event) {}
    fun onEdit(event: Event) {}
    fun onOpen(event: Event) {}
    fun onShare(event: Event) {}

}

class EventAdapter(
    private val onInteractionListener: EventOnInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.bind(event, position)
        }
    }

}

class EventViewHolder(
    private val binding: EventCardBinding,
    private val onInteractionListener: EventOnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    private var previousPosition = -1
    private val mediaLifecycleObserver = MediaLifecycleObserver()
    fun bind(event: Event, position: Int) {
        binding.apply {
            EventCard.setOnClickListener {
                onInteractionListener.onOpen(event)
            }
            AuthorTV.text = event.author
            AvatarIV.loadImgCircle(event.authorAvatar)
            PublishedEventTV.text = AndroidUtils.dateFormatToText(event.published, root.context)
            TextEventTV.text = event.content
            if (event.link != null) {
                LinkEventTV.visibility = View.VISIBLE
                LinkEventTV.text = event.link
            } else {
                LinkEventTV.visibility = View.GONE
            }
            TypeEventTV.text = when (event.type) {
                EventType.ONLINE -> root.context.getString(R.string.online)
                EventType.OFFLINE -> root.context.getString(R.string.offline)
            }
            DateEventTV.text = AndroidUtils.dateFormatToText(event.datetime, root.context)
            LikeIB.text = event.likeOwnerIds.size.toString()
            LikeIB.isChecked = event.likedByMe
            LikeIB.setOnClickListener {
                onInteractionListener.onLike(event)
            }
            imageAttachment.visibility = View.GONE
            audioAttachment.audioPlay.visibility = View.GONE
            videoAttachment.videoPlay.visibility = View.GONE
            Glide.with(imageAttachment).clear(binding.imageAttachment)
            Glide.with(videoAttachment.videoThumb).clear(binding.videoAttachment.videoThumb)
            audioAttachment.playAudioIB.setBackgroundResource(R.drawable.ic_play_80dp)
            videoAttachment.videoView.setVideoURI(null)
            videoAttachment.videoView.stopPlayback()
            audioAttachment.audioSB.progress = 0
            if (event.attachment != null) {
                when (event.attachment.type) {
                    AttachmentType.IMAGE -> {
                        imageAttachment.visibility = View.VISIBLE
                        audioAttachment.audioPlay.visibility = View.GONE
                        videoAttachment.videoPlay.visibility = View.GONE
                        Glide.with(imageAttachment).load(event.attachment.url)
                            .placeholder(R.drawable.ic_downloading_100dp)
                            .error(R.drawable.ic_error_outline_100dp).timeout(10_000).centerCrop()
                            .into(binding.imageAttachment)
                    }

                    AttachmentType.VIDEO -> {
                        videoAttachment.videoPlay.visibility = View.VISIBLE
                        audioAttachment.audioPlay.visibility = View.GONE
                        imageAttachment.visibility = View.GONE
                        Glide.with(videoAttachment.videoThumb).load(event.attachment.url)
                            .centerCrop().into(binding.videoAttachment.videoThumb)
                    }

                    AttachmentType.AUDIO -> {
                        audioAttachment.audioPlay.visibility = View.VISIBLE
                        imageAttachment.visibility = View.GONE
                        videoAttachment.videoPlay.visibility = View.GONE
                        if (position != previousPosition) {
                            audioAttachment.playAudioIB.setBackgroundResource(R.drawable.ic_play_48dp)
                            audioAttachment.audioSB.progress = 0
                            audioAttachment.audioSB.removeCallbacks(mediaLifecycleObserver.runnable)
                            audioAttachment.playAudioIB.setBackgroundResource(R.drawable.ic_play_48dp)
                        } else {
                            audioAttachment.audioSB.max =
                                mediaLifecycleObserver.mediaPlayer!!.duration
                            audioAttachment.audioSB.progress =
                                mediaLifecycleObserver.mediaPlayer!!.currentPosition
                            audioAttachment.audioSB.postDelayed(
                                mediaLifecycleObserver.runnable, 1000
                            )
                            audioAttachment.playAudioIB.setBackgroundResource(R.drawable.ic_pause_48dp)
                            mediaLifecycleObserver.seekSet(audioAttachment.audioSB)
                        }
                    }
                }
            }
            videoAttachment.playVideoIB.setOnClickListener {
                videoAttachment.videoView.visibility = View.VISIBLE
                videoAttachment.videoView.apply {
                    setMediaController(MediaController(context))
                    setVideoURI(Uri.parse(event.attachment?.url))
                    setOnPreparedListener {
                        videoAttachment.videoThumb.visibility = View.GONE
                        videoAttachment.playVideoIB.visibility = View.GONE
                        start()
                    }
                    setOnCompletionListener {
                        stopPlayback()
                        videoAttachment.videoView.visibility = View.GONE
                        videoAttachment.playVideoIB.visibility = View.VISIBLE
                        videoAttachment.videoThumb.visibility = View.VISIBLE
                    }
                }
            }
            audioAttachment.playAudioIB.setOnClickListener {
                if (previousPosition == -1) {
                    mediaLifecycleObserver.playAudio(
                        event.attachment!!, audioAttachment.audioSB, audioAttachment.playAudioIB
                    )
                } else {
                    if (position == previousPosition) {
                        mediaLifecycleObserver.playAudio(
                            event.attachment!!, audioAttachment.audioSB, audioAttachment.playAudioIB
                        )
                    } else {
                        if (mediaLifecycleObserver.mediaPlayer?.isPlaying == true) {
                            mediaLifecycleObserver.stop()
                            audioAttachment.audioSB.progress = previousPosition
                            mediaLifecycleObserver.playAudio(
                                event.attachment!!,
                                audioAttachment.audioSB,
                                audioAttachment.playAudioIB
                            )
                        } else {
                            mediaLifecycleObserver.playAudio(
                                event.attachment!!,
                                audioAttachment.audioSB,
                                audioAttachment.playAudioIB
                            )
                        }
                    }
                }
                previousPosition = position
            }
            ShareIB.setOnClickListener { onInteractionListener.onShare(event) }
            MenuIB.isVisible = event.ownedByMe
            MenuIB.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_card)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}