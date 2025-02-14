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
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.EventDetailsFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.enumer.EventType
import ru.shelq.nework.ui.post.PostDetailsFragment.Companion.saveLat
import ru.shelq.nework.ui.post.PostDetailsFragment.Companion.saveLong
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.addMarkerOnMap
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.AndroidUtils.moveCamera
import ru.shelq.nework.util.AndroidUtils.share
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by IdArg
    }

    @Inject
    lateinit var auth: AppAuth
    private val mediaObserver = MediaLifecycleObserver()
    private val viewModel: EventViewModel by activityViewModels()
    private var binding: EventDetailsFragmentBinding? = null
    private fun requireBinding() = requireNotNull(binding)
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
        viewModel.reset()
        viewModel.edit(null)
        val eventId = arguments?.id ?: -1
        println(eventId)
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
                        requireBinding().listAvatarsLikers.showMoreB.isVisible =
                            event.likeOwnerIds.size > 5
                    }
                }

                requireBinding().listAvatarsLikers.showMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_eventDetailsFragment_to_eventLikersFragment)
                }

                if (event.participantsIds.isNotEmpty()) {
                    if (needLoadParticipantsAvatars) {
                        viewModel.getParticipants(event)
                        fillParticipants()
                        needLoadParticipantsAvatars = false
                        requireBinding().listAvatarsParticipant.showMoreB.isVisible =
                            event.participantsIds.size > 5
                    }
                }

                requireBinding().listAvatarsParticipant.showMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_eventDetailsFragment_to_eventParticipantsFragment)
                }

                if (event.speakerIds.isNotEmpty()) {
                    requireBinding().titleSpeakersTV.visibility = View.VISIBLE
                    requireBinding().groupSpeakersLL.visibility = View.VISIBLE
                    if (needLoadSpeakersAvatars) {
                        viewModel.getSpeakers(event)
                        fillSpeakers()
                        needLoadSpeakersAvatars = false
                        requireBinding().listAvatarsSpeakers.showMoreB.isVisible =
                            event.speakerIds.size > 5
                    }
                }

                requireBinding().listAvatarsSpeakers.showMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_eventDetailsFragment_to_eventSpeakersFragment)
                }




                requireBinding().apply {

                    eventDetailsTBL.run {
                        setNavigationOnClickListener {
                            viewModel.reset()
                            viewModel.edit(null)
                            findNavController().navigate(R.id.action_eventDetailsFragment_to_eventFragment)

                        }
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.share -> {
                                    share(
                                        requireContext(),
                                        requireBinding().textEventTV.text.toString()
                                    )
                                    true
                                }

                                else -> false
                            }
                        }

                    }
                    avatarIV.loadImgCircle(event.authorAvatar)
                    authorTV.text = event.author

                    if (event.authorJob != null) {
                        nameJobTV.text = event.authorJob
                    } else {
                        nameJobTV.text = getText(R.string.looking_for_a_job)
                    }
                    textEventTV.text = event.content
                    if (event.link != null) {
                        titleLinkTV.visibility = View.VISIBLE
                        linkPostTV.visibility = View.VISIBLE
                        linkPostTV.text = event.link
                    } else {
                        linkPostTV.visibility = View.GONE
                        titleLinkTV.visibility = View.GONE
                    }

                    typeEventTV.text =
                        when (event.type) {
                            EventType.OFFLINE -> getString(R.string.offline)
                            EventType.ONLINE -> getString(R.string.online)
                        }

                    dateEventTV.text = AndroidUtils.dateFormatToText(event.datetime, root.context)


                    participantB.run {
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

                    likeIB.run {
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


                    if (event.coords != null) {
                        val point = Point(event.coords.lat, event.coords.long)
                        geoEventMW.visibility = View.VISIBLE
                        containerMap.visibility = View.VISIBLE
                        moveToMarker(point)
                        setMarker(point)
                        geoEventMW.setNoninteractive(true)
                        geoEventMW.setOnClickListener {
                            geoEventMW.onStop()
                            findNavController().navigate(
                                R.id.action_eventDetailsFragment_to_eventMapFragment,
                                args = Bundle().apply {
                                    id = event.id
                                    saveLat = event.coords.lat
                                    saveLong = event.coords.long
                                })
                        }

                    } else {
                        geoEventMW.visibility = View.GONE
                        containerMap.visibility = View.GONE
                    }

                    if (event.attachment?.url != null) {
                        attachmentGroup.isVisible = true
                        imageAttachment.isVisible = false
                        audioAttachment.audioPlay.isVisible = false
                        videoAttachment.videoPlay.isVisible = false
                        when (event.attachment.type) {

                            AttachmentType.IMAGE -> {
                                imageAttachment.isVisible = true
                                Glide.with(imageAttachment)
                                    .load(event.attachment.url)
                                    .placeholder(R.drawable.ic_downloading_100dp)
                                    .error(R.drawable.ic_error_outline_100dp)
                                    .timeout(10_000)
                                    .centerCrop()
                                    .into(requireBinding().imageAttachment)
                            }

                            AttachmentType.VIDEO -> {
                                videoAttachment.videoPlay.isVisible = true
                                Glide.with(videoAttachment.videoThumb)
                                    .load(event.attachment.url)
                                    .into(requireBinding().videoAttachment.videoThumb)
                            }

                            AttachmentType.AUDIO -> {
                                audioAttachment.audioPlay.isVisible = true
                            }
                        }
                    } else {
                        attachmentGroup.isVisible = false
                        Glide.with(imageAttachment).clear(requireBinding().imageAttachment)
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
                            requireBinding().audioAttachment.audioSB,
                            requireBinding().audioAttachment.playAudioIB
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
                Snackbar.make(requireBinding().root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
                viewModel.resetError()
            }
        }
        return requireBinding().root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        requireBinding().geoEventMW.onStart()
    }

    override fun onStop() {
        requireBinding().geoEventMW.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun setMarker(point: Point) {
        addMarkerOnMap(requireContext(), requireBinding().geoEventMW, point)
    }

    private fun moveToMarker(point: Point) {
        moveCamera(requireBinding().geoEventMW, point)
    }


    private fun clearLikersAvatars() {
        likerNumber = -1
        needLoadLikersAvatars = true
        mapLikers.clear()
        requireBinding().listAvatarsLikers.avatar1.isVisible = false
        requireBinding().listAvatarsLikers.avatar2.isVisible = false
        requireBinding().listAvatarsLikers.avatar3.isVisible = false
        requireBinding().listAvatarsLikers.avatar4.isVisible = false
        requireBinding().listAvatarsLikers.avatar5.isVisible = false


    }

    private fun clearParticipantsAvatars() {
        participantNumber = -1
        needLoadParticipantsAvatars = true
        mapParticipants.clear()
        requireBinding().listAvatarsParticipant.avatar1.isVisible = false
        requireBinding().listAvatarsParticipant.avatar2.isVisible = false
        requireBinding().listAvatarsParticipant.avatar3.isVisible = false
        requireBinding().listAvatarsParticipant.avatar4.isVisible = false
        requireBinding().listAvatarsParticipant.avatar5.isVisible = false
    }

    private fun clearSpeakersAvatars() {
        speakerNumber = -1
        needLoadSpeakersAvatars = true
        mapSpeakers.clear()
        requireBinding().groupSpeakersLL.visibility = View.GONE
        requireBinding().titleSpeakersTV.visibility = View.GONE
        requireBinding().listAvatarsSpeakers.avatar1.isVisible = false
        requireBinding().listAvatarsSpeakers.avatar2.isVisible = false
        requireBinding().listAvatarsSpeakers.avatar3.isVisible = false
        requireBinding().listAvatarsSpeakers.avatar4.isVisible = false
        requireBinding().listAvatarsSpeakers.avatar5.isVisible = false
    }

    private fun fillLikers() {
        mapLikers[0] = requireBinding().listAvatarsLikers.avatar1
        mapLikers[1] = requireBinding().listAvatarsLikers.avatar2
        mapLikers[2] = requireBinding().listAvatarsLikers.avatar3
        mapLikers[3] = requireBinding().listAvatarsLikers.avatar4
        mapLikers[4] = requireBinding().listAvatarsLikers.avatar5

    }

    private fun fillParticipants() {
        mapParticipants[0] = requireBinding().listAvatarsParticipant.avatar1
        mapParticipants[1] = requireBinding().listAvatarsParticipant.avatar2
        mapParticipants[2] = requireBinding().listAvatarsParticipant.avatar3
        mapParticipants[3] = requireBinding().listAvatarsParticipant.avatar4
        mapParticipants[4] = requireBinding().listAvatarsParticipant.avatar5
    }

    private fun fillSpeakers() {
        mapSpeakers[0] = requireBinding().listAvatarsSpeakers.avatar1
        mapSpeakers[1] = requireBinding().listAvatarsSpeakers.avatar2
        mapSpeakers[2] = requireBinding().listAvatarsSpeakers.avatar3
        mapSpeakers[3] = requireBinding().listAvatarsSpeakers.avatar4
        mapSpeakers[4] = requireBinding().listAvatarsSpeakers.avatar5
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


