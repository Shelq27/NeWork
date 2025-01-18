package ru.shelq.nework.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.databinding.PostDetailsFragmentBinding
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.AndroidUtils.share
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.PostViewModel

@Suppress("DEPRECATION")
@AndroidEntryPoint
class PostDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by idArg
    }

    private val mediaObserver = MediaLifecycleObserver()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = PostDetailsFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)
        val viewModelPost: PostViewModel by activityViewModels()
        val postId = arguments?.id ?: -1
        viewModelPost.getPostById(postId)
        viewModelPost.selectedPost.observe(viewLifecycleOwner) { post ->

            if (post != null)
                binding.apply {

                    PostDetailsTBL.setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }
                    PostDetailsTBL.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.share -> {
                                share(requireContext(), binding.TextPostTV.text.toString())
                                true
                            }
                            else -> super.onOptionsItemSelected(menuItem)
                        }

                    }
                    AvatarIV.loadImgCircle(post.authorAvatar)
                    AuthorTV.text = post.author
                    NameJobTV.text = post.authorJob
                    DatePublicationPostTV.text =
                        AndroidUtils.dateFormatToText(post.published, root.context)
                    TextPostTV.text = post.content
                    LinkPostTV.text = post.link
                    LikeIB.text = post.likeOwnerIds.size.toString()


                    if (post.attachment?.url != null) {
                        imageAttachment.visibility = View.GONE
                        audioAttachment.audioPlay.visibility = View.GONE
                        videoAttachment.videoPlay.visibility = View.GONE
                        when (post.attachment.type) {

                            //изображение
                            AttachmentType.IMAGE -> {
                                imageAttachment.visibility = View.VISIBLE
                                audioAttachment.audioPlay.visibility = View.GONE
                                videoAttachment.videoPlay.visibility = View.GONE
                                Glide.with(imageAttachment)
                                    .load(post.attachment.url)
                                    .placeholder(R.drawable.ic_downloading_100dp)
                                    .error(R.drawable.ic_error_outline_100dp)
                                    .timeout(10_000)
                                    .centerCrop()
                                    .into(binding.imageAttachment)
                            }
                            //видео
                            AttachmentType.VIDEO -> {
                                imageAttachment.visibility = View.GONE
                                audioAttachment.audioPlay.visibility = View.GONE
                                videoAttachment.videoPlay.visibility = View.VISIBLE
                                Glide.with(videoAttachment.videoThumb)
                                    .load(post.attachment.url)
                                    .into(binding.videoAttachment.videoThumb)
                            }
                            //аудио
                            AttachmentType.AUDIO -> {
                                imageAttachment.visibility = View.GONE
                                audioAttachment.audioPlay.visibility = View.VISIBLE
                                videoAttachment.videoPlay.visibility = View.GONE
                            }
                        }
                    } else {
                        AttachmentGroup.visibility = View.GONE
                        Glide.with(imageAttachment).clear(binding.imageAttachment)
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
                        mediaObserver.playAudio(
                            post.attachment!!,
                            binding.audioAttachment.audioSB,
                            binding.audioAttachment.playAudioIB
                        )
                    }
                }
        }

        return binding.root
    }
}