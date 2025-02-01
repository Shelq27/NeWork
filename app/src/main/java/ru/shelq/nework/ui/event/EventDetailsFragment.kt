package ru.shelq.nework.ui.event

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.EventDetailsFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.AndroidUtils.share
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.viewmodel.EventViewModel
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class EventDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by IdArg
    }

    @Inject
    lateinit var auth: AppAuth
    private val mediaObserver = MediaLifecycleObserver()
    private val viewModel: EventViewModel by activityViewModels()
    private lateinit var binding: EventDetailsFragmentBinding

    private var mapLikers = HashMap<Int, ImageView>()
    private var mapParticipants = HashMap<Int, ImageView>()
    private var mapSpeakers = HashMap<Int, ImageView>()
    private var likerNumber: Int = -1
    private var speakerNumber: Int = -1
    private var participantNumber: Int = -1
    private var needLoadLikersAvatars = false
    private var needLoadSpeakersAvatars = false
    private var needLoadParticipantsAvatars = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EventDetailsFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)

        val eventId = arguments?.id ?: -1
        viewModel.getEventById(eventId)

        viewModel.selectedEvent.observe(viewLifecycleOwner) { event ->
            clearParticipantsAvatars()
            clearSpeakersAvatars()
            clearLikersAvatars()

            if (event != null) {
                if (event.likeOwnerIds.isNotEmpty()) {
                    if (needLoadLikersAvatars) {
                        viewModel.getLikers(event)
                        fillLikers()
                        needLoadLikersAvatars = false
                        binding.listAvatarsLikers.ShowMoreB.isVisible =
                            event.likeOwnerIds.size > 5
                    }
                }

                binding.listAvatarsLikers.ShowMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_eventDetailsFragment_to_eventLikersFragment)
                }

                if (event.participantsIds.isNotEmpty()) {
                    if (needLoadParticipantsAvatars) {
                        viewModel.getParticipants(event)
                        fillParticipants()
                        needLoadParticipantsAvatars = false
                        binding.listAvatarsParticipant.ShowMoreB.isVisible =
                            event.participantsIds.size > 5
                    }
                }

                binding.listAvatarsParticipant.ShowMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_eventDetailsFragment_to_eventParticipantsFragment)
                }

                if (event.speakerIds.isNotEmpty()) {
                    if (needLoadSpeakersAvatars) {
                        viewModel.getSpeakers(event)
                        fillSpeakers()
                        needLoadSpeakersAvatars = false
                        binding.listAvatarsSpeakers.ShowMoreB.isVisible =
                            event.speakerIds.size > 5
                    }
                }

                binding.listAvatarsSpeakers.ShowMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_eventDetailsFragment_to_eventSpeakersFragment)
                }


                binding.apply {

                    EventDetailsTBL.run {
                        setNavigationOnClickListener {
                            viewModel.reset()
                            findNavController().navigateUp()

                        }
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.share -> {
                                    share(requireContext(), binding.TextEventTV.text.toString())
                                    true
                                }

                                else -> super.onOptionsItemSelected(menuItem)
                            }
                        }

                    }
                    AvatarIV.loadImgCircle(event.authorAvatar)
                    AuthorTV.text = event.author
                    if (event.authorJob != null) {
                        NameJobTV.text = event.authorJob
                    } else {
                        NameJobTV.text = getText(R.string.looking_for_a_job)
                    }
                    TextEventTV.text = event.content
                    if (event.link != null) {
                        LinkPostTV.visibility = View.VISIBLE
                        LinkPostTV.text = event.link
                    } else {
                        LinkPostTV.visibility = View.GONE
                    }
                    DateEventTV.text = AndroidUtils.dateFormatToText(event.datetime, root.context)


                    ParticipantB.run {
                        text = event.participantsIds.size.toString()
                        isChecked = event.participatedByMe
                        setOnClickListener {
                            if (auth.authenticated()) {
                                viewModel.participateByEvent(event)
                            } else {
                                isChecked = event.participatedByMe
                                AndroidUtils.showSignInDialog(this@EventDetailsFragment)
                            }
                        }
                    }

                    LikeIB.run {
                        text = event.likeOwnerIds.size.toString()
                        isChecked = event.likedByMe

                        setOnClickListener {
                            if (auth.authenticated()) {
                                viewModel.likeByEvent(event)
                            } else {
                                isChecked = event.likedByMe
                                AndroidUtils.showSignInDialog(this@EventDetailsFragment)
                            }
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
        }
        viewModel.likersLoaded.observe(viewLifecycleOwner) {
            viewModel.likers.value?.forEach { user ->
                likerNumber++
                mapLikers[likerNumber]?.let { imageView ->
                    loadAvatar(imageView, user)
                    imageView.isVisible = true
                }
            }
        }

        viewModel.speakersLoaded.observe(viewLifecycleOwner) {
            viewModel.speakers.value?.forEach { user ->
                speakerNumber++
                mapSpeakers[speakerNumber]?.let { imageView ->
                    loadAvatar(imageView, user)
                    imageView.isVisible = true
                }
            }
        }
        viewModel.participantsLoaded.observe(viewLifecycleOwner) {
            viewModel.participants.value?.forEach { user ->
                participantNumber++
                mapParticipants[participantNumber]?.let { imageView ->
                    loadAvatar(imageView, user)
                    imageView.isVisible = true
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

    private fun clearLikersAvatars() {
        likerNumber = -1
        needLoadLikersAvatars = true
        mapLikers.clear()
        binding.listAvatarsLikers.avatar1.isVisible = false
        binding.listAvatarsLikers.avatar2.isVisible = false
        binding.listAvatarsLikers.avatar3.isVisible = false
        binding.listAvatarsLikers.avatar4.isVisible = false
        binding.listAvatarsLikers.avatar5.isVisible = false


    }

    private fun clearParticipantsAvatars() {
        participantNumber = -1
        needLoadParticipantsAvatars = true
        mapParticipants.clear()
        binding.listAvatarsParticipant.avatar1.isVisible = false
        binding.listAvatarsParticipant.avatar2.isVisible = false
        binding.listAvatarsParticipant.avatar3.isVisible = false
        binding.listAvatarsParticipant.avatar4.isVisible = false
        binding.listAvatarsParticipant.avatar5.isVisible = false
    }

    private fun clearSpeakersAvatars() {
        speakerNumber = -1
        needLoadSpeakersAvatars = true
        mapSpeakers.clear()
        binding.listAvatarsSpeakers.avatar1.isVisible = false
        binding.listAvatarsSpeakers.avatar2.isVisible = false
        binding.listAvatarsSpeakers.avatar3.isVisible = false
        binding.listAvatarsSpeakers.avatar4.isVisible = false
        binding.listAvatarsSpeakers.avatar5.isVisible = false
    }

    private fun fillLikers() {
        mapLikers[0] = binding.listAvatarsLikers.avatar1
        mapLikers[1] = binding.listAvatarsLikers.avatar2
        mapLikers[2] = binding.listAvatarsLikers.avatar3
        mapLikers[3] = binding.listAvatarsLikers.avatar4
        mapLikers[4] = binding.listAvatarsLikers.avatar5

    }

    private fun fillParticipants() {
        mapParticipants[0] = binding.listAvatarsParticipant.avatar1
        mapParticipants[1] = binding.listAvatarsParticipant.avatar2
        mapParticipants[2] = binding.listAvatarsParticipant.avatar3
        mapParticipants[3] = binding.listAvatarsParticipant.avatar4
        mapParticipants[4] = binding.listAvatarsParticipant.avatar5
    }

    private fun fillSpeakers() {
        mapSpeakers[0] = binding.listAvatarsSpeakers.avatar1
        mapSpeakers[1] = binding.listAvatarsSpeakers.avatar2
        mapSpeakers[2] = binding.listAvatarsSpeakers.avatar3
        mapSpeakers[3] = binding.listAvatarsSpeakers.avatar4
        mapSpeakers[4] = binding.listAvatarsSpeakers.avatar5
    }

    private fun loadAvatar(imageView: ImageView, user: User) {
        Glide.with(imageView)
            .load(user.avatar)
            .placeholder(R.drawable.ic_downloading_100dp)
            .error(R.drawable.ic_error_outline_100dp)
            .timeout(10_000)
            .circleCrop()
            .into(imageView)

    }
}


