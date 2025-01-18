package ru.shelq.nework.ui.event

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.adapter.EventAdapter
import ru.shelq.nework.adapter.EventOnInteractionListener
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.EventDetailsFragmentBinding
import ru.shelq.nework.dto.Event
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.AndroidUtils.share
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.EventViewModel
import ru.shelq.nework.viewmodel.PostViewModel
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class EventDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by idArg
    }

    @Inject
    lateinit var auth: AppAuth
    private val mediaObserver = MediaLifecycleObserver()
    private val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EventDetailsFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)
        val eventId = arguments?.id ?: -1
        viewModel.getEventById(eventId)
        viewModel.selectedEvent.observe(viewLifecycleOwner) { event ->
            if (event != null)
                binding.apply {
                    EventDetailsTBL.setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }
                    EventDetailsTBL.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.share -> {
                                share(requireContext(), binding.TextEventTV.text.toString())
                                true
                            }

                            else -> super.onOptionsItemSelected(menuItem)
                        }

                    }

                    AvatarIV.loadImgCircle(event.authorAvatar)
                    AuthorTV.text = event.author
                    NameJobTV.text = event.authorJob
                    LinkPostTV.text = event.link
                    TextEventTV.text = event.content
                    DateEventTV.text = AndroidUtils.dateFormatToText(event.datetime, root.context)
                    LikeIB.text = event.likeOwnerIds.size.toString()
                    LikeIB.isChecked = event.likedByMe
                    LikeIB.setOnClickListener {
                        if (auth.authenticated()) {
                            viewModel.likeByEvent(event)
                        } else {
                            LikeIB.isChecked = event.likedByMe
                            AndroidUtils.showSignInDialog(this@EventDetailsFragment)
                        }
                    }



                    if (event.attachment?.url != null) {
                        AttachmentGroup.isVisible = true
                        imageAttachment.isVisible = false
                        audioAttachment.audioPlay.isVisible = false
                        videoAttachment.videoPlay.isVisible = false
                        when (event.attachment.type) {

                            //изображение
                            AttachmentType.IMAGE -> {
                                imageAttachment.isVisible = true
                                Glide.with(imageAttachment)
                                    .load(event.attachment.url)
                                    .placeholder(R.drawable.ic_downloading_100dp)
                                    .error(R.drawable.ic_error_outline_100dp)
                                    .timeout(10_000)
                                    .centerCrop()
                                    .into(binding.imageAttachment)
                            }
                            //видео
                            AttachmentType.VIDEO -> {
                                videoAttachment.videoPlay.isVisible = true
                                Glide.with(videoAttachment.videoThumb)
                                    .load(event.attachment.url)
                                    .into(binding.videoAttachment.videoThumb)
                            }
                            //аудио
                            AttachmentType.AUDIO -> {
                                audioAttachment.audioPlay.isVisible = true
                            }
                        }
                    } else {
                        AttachmentGroup.isVisible = false
                        Glide.with(imageAttachment).clear(binding.imageAttachment)
                    }

                    videoAttachment.playVideoIB.setOnClickListener {
                        videoAttachment.videoView.isVisible = true
                        videoAttachment.videoView.apply {
                            setMediaController(MediaController(context))
                            setVideoURI(Uri.parse(event.attachment?.url))
                            setOnPreparedListener {
                                videoAttachment.videoThumb.isVisible = false
                                videoAttachment.playVideoIB.isVisible = false
                                start()
                            }
                            setOnCompletionListener {
                                stopPlayback()
                                videoAttachment.videoView.isVisible = false
                                videoAttachment.playVideoIB.isVisible = true
                                videoAttachment.videoThumb.isVisible = true
                            }
                        }
                    }
                    audioAttachment.playAudioIB.setOnClickListener {
                        mediaObserver.playAudio(
                            event.attachment!!,
                            binding.audioAttachment.audioSB,
                            binding.audioAttachment.playAudioIB
                        )
                    }

                }
        }
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
                viewModel.resetError()
            }
        }

        return binding.root
    }
}