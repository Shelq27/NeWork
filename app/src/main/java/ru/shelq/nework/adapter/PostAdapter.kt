package ru.shelq.nework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.shelq.nework.R
import ru.shelq.nework.databinding.PostCardBinding
import ru.shelq.nework.dto.Post
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.MediaLifecycleObserver


interface PostOnInteractionListener {
    fun onLike(post: Post) {}
    fun onRemove(post: Post) {}
    fun onEdit(post: Post) {}
    fun onOpen(post: Post) {}
    fun onShare(post: Post) {}
    fun onPlayAudio(post: Post, seekBar: SeekBar, playAudio: ImageButton) {}

}

class PostAdapter(
    private val onInteractionListener: PostOnInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        if (post != null) {
            holder.bing(post, position)
        }

    }
}

class PostViewHolder(
    private val binding: PostCardBinding,
    private val onInteractionListener: PostOnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {


    private var previousPosition = -1
    private val mediaLifecycleObserver = MediaLifecycleObserver()
    fun bing(post: Post, position: Int) {

        binding.apply {

            CardPost.setOnClickListener {
                onInteractionListener.onOpen(post)
            }
            AuthorTV.text = post.author
            AvatarIV.loadImgCircle(post.authorAvatar)

            if (post.link.equals("")) {
                LinkPostTV.visibility = View.GONE
            } else {
                LinkPostTV.visibility = View.VISIBLE
                LinkPostTV.text = post.link
            }
            if (post.content == "") {
                TextPostTV.visibility = View.GONE
            } else {
                TextPostTV.visibility = View.VISIBLE
                TextPostTV.text = post.content
            }
            PublishedPostTV.text = AndroidUtils.dateFormatToText(post.published, root.context)
            LikeIB.text = post.likeOwnerIds.size.toString()
            LikeIB.isChecked = post.likedByMe

            LikeIB.setOnClickListener {
                onInteractionListener.onLike(post)
                LikeIB.isChecked = post.likedByMe
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
            if (post.attachment != null) {
                AttachmentGroup.visibility = View.VISIBLE
                when (post.attachment.type) {
                    AttachmentType.IMAGE -> {
                        imageAttachment.visibility = View.VISIBLE
                        audioAttachment.audioPlay.visibility = View.GONE
                        videoAttachment.videoPlay.visibility = View.GONE
                        Glide.with(imageAttachment).load(post.attachment.url)
                            .placeholder(R.drawable.ic_downloading_100dp)
                            .error(R.drawable.ic_error_outline_100dp).timeout(10_000).centerCrop()
                            .into(binding.imageAttachment)
                    }

                    AttachmentType.VIDEO -> {
                        videoAttachment.videoPlay.visibility = View.VISIBLE
                        audioAttachment.audioPlay.visibility = View.GONE
                        imageAttachment.visibility = View.GONE
                        Glide.with(videoAttachment.videoThumb).load(post.attachment.url)
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
                    setVideoURI(Uri.parse(post.attachment?.url))
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
                        post.attachment!!, audioAttachment.audioSB, audioAttachment.playAudioIB
                    )
                } else {
                    if (position == previousPosition) {
                        mediaLifecycleObserver.playAudio(
                            post.attachment!!, audioAttachment.audioSB, audioAttachment.playAudioIB
                        )
                    } else {
                        if (mediaLifecycleObserver.mediaPlayer?.isPlaying == true) {
                            mediaLifecycleObserver.stop()
                            audioAttachment.audioSB.progress = previousPosition
                            mediaLifecycleObserver.playAudio(
                                post.attachment!!,
                                audioAttachment.audioSB,
                                audioAttachment.playAudioIB
                            )
                        } else {
                            mediaLifecycleObserver.playAudio(
                                post.attachment!!,
                                audioAttachment.audioSB,
                                audioAttachment.playAudioIB
                            )
                        }
                    }
                }
                previousPosition = position
            }

            ShareIB.setOnClickListener { onInteractionListener.onShare(post) }
            MenuIB.isVisible = post.ownedByMe
            MenuIB.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_card)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            itemView.setOnClickListener {
                onInteractionListener.onOpen(post)
            }
        }
    }

}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}