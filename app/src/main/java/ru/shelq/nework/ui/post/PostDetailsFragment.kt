package ru.shelq.nework.ui.post

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
import ru.shelq.nework.databinding.PostDetailsFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.addMarkerOnMap
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.AndroidUtils.moveCamera
import ru.shelq.nework.util.AndroidUtils.share
import ru.shelq.nework.util.DoubleArg
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by IdArg
        var Bundle.saveLat: Double by DoubleArg
        var Bundle.saveLong: Double by DoubleArg
    }

    @Inject
    lateinit var auth: AppAuth
    private val mediaObserver = MediaLifecycleObserver()
    private var needLoadMentionedAvatars = false
    private var needLoadLikersAvatars = false
    private var mentionedNumber: Int = -1
    private var likerNumber: Int = -1
    private var mapMentioned = HashMap<Int, ImageView>()
    private var mapLikers = HashMap<Int, ImageView>()

    private var binding: PostDetailsFragmentBinding? = null
    private fun requireBinding() = requireNotNull(binding)
    private val viewModel: PostViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostDetailsFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)
        val postId = arguments?.id ?: -1
        viewModel.getPostById(postId)

        viewModel.selectedPost.observe(viewLifecycleOwner) { post ->
            clearLikersAvatars()
            clearMentionAvatars()


            if (post != null) {
                if (post.likeOwnerIds.isNotEmpty()) {
                    if (needLoadLikersAvatars) {
                        viewModel.getLikers(post)
                        fillLikers()
                        needLoadLikersAvatars = false
                        requireBinding().listAvatarsLikers.showMoreB.isVisible =
                            post.likeOwnerIds.size > 5
                    }
                }
                requireBinding().listAvatarsLikers.showMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_postDetailsFragment_to_postLikersFragment)
                }

                if (post.mentionIds.isNotEmpty()) {
                    if (needLoadMentionedAvatars) {
                        viewModel.getMentioned(post)
                        fillMentioned()
                        needLoadMentionedAvatars = false
                        requireBinding().listAvatarsMentioned.showMoreB.isVisible =
                            post.mentionIds.size > 5
                    }
                }
                requireBinding().listAvatarsMentioned.showMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_postDetailsFragment_to_postMentionedFragment)
                }






                requireBinding().apply {

                    if (post.authorJob != null) {
                        nameJobTV.text = post.authorJob
                    } else {
                        nameJobTV.text = getText(R.string.looking_for_a_job)
                    }
                    postDetailsTBL.setNavigationOnClickListener {
                        findNavController().navigate(R.id.action_postDetailsFragment_to_postFragment)
                    }

                    postDetailsTBL.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.share -> {
                                share(requireContext(), textPostTV.text.toString())
                                true
                            }

                            else -> false
                        }

                    }
                    avatarIV.loadImgCircle(post.authorAvatar)
                    authorTV.text = post.author
                    datePublicationPostTV.text =
                        AndroidUtils.dateFormatToText(post.published, root.context)
                    textPostTV.text = post.content

                    if (post.link != null) {
                        linkPostTV.visibility = View.VISIBLE
                        titleLinkTV.visibility = View.VISIBLE
                        linkPostTV.text = post.link
                    } else {
                        linkPostTV.visibility = View.GONE
                        titleLinkTV.visibility = View.GONE
                    }

                    mentionedB.run {
                        text = post.mentionIds.size.toString()
                        isChecked = post.mentionedMe
                    }


                    likeIB.run {
                        text = post.likeOwnerIds.size.toString()
                        isChecked = post.likedByMe
                        setOnClickListener {
                            if (auth.authenticated()) {
                                viewModel.likeByPost(post)
                            } else {
                                isChecked = post.likedByMe
                                AndroidUtils.showSignInDialog(this@PostDetailsFragment)
                            }
                        }
                    }

                    if (post.coords != null) {
                        val point = Point(post.coords.lat, post.coords.long)
                        containerMap.visibility = View.VISIBLE
                        geoPostMW.visibility = View.VISIBLE
                        moveToMarker(point)// Перемещаем камеру в определенную область на карте
                        setMarker(point)// Устанавливаем маркер на карте
                        geoPostMW.setNoninteractive(true)
                        geoPostMW.setOnClickListener {
                            geoPostMW.onStop()
                            findNavController().navigate(
                                R.id.action_postDetailsFragment_to_postMapFragment,
                                args = Bundle().apply {
                                    id = post.id
                                    saveLat = post.coords.lat
                                    saveLong = post.coords.long
                                })
                        }

                    } else {
                        geoPostMW.visibility = View.GONE
                        containerMap.visibility = View.GONE
                    }

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
                                    .into(imageAttachment)
                            }
                            //видео
                            AttachmentType.VIDEO -> {
                                imageAttachment.visibility = View.GONE
                                audioAttachment.audioPlay.visibility = View.GONE
                                videoAttachment.videoPlay.visibility = View.VISIBLE
                                Glide.with(videoAttachment.videoThumb)
                                    .load(post.attachment.url)
                                    .into(videoAttachment.videoThumb)
                            }
                            //аудио
                            AttachmentType.AUDIO -> {
                                imageAttachment.visibility = View.GONE
                                audioAttachment.audioPlay.visibility = View.VISIBLE
                                videoAttachment.videoPlay.visibility = View.GONE
                            }
                        }
                    } else {
                        attachmentGroup.visibility = View.GONE
                        Glide.with(imageAttachment).clear(imageAttachment)
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
                            audioAttachment.audioSB,
                            audioAttachment.playAudioIB
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

        viewModel.mentionedLoaded.observe(viewLifecycleOwner) {
            viewModel.mentioned.value?.forEach { user ->
                mentionedNumber++
                mapMentioned[mentionedNumber]?.let { imageView ->
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

    // Отображаем карты перед тем моментом, когда активити с картой станет видимой пользователю:
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        requireBinding().geoPostMW.onStart()
    }

    // Останавливаем обработку карты, когда активити с картой становится невидимым для пользователя:
    override fun onStop() {
        requireBinding().geoPostMW.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun setMarker(point: Point) {
        addMarkerOnMap(requireContext(), requireBinding().geoPostMW, point)
    }

    private fun moveToMarker(point: Point) {
        moveCamera(requireBinding().geoPostMW, point)
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

    private fun clearMentionAvatars() {
        mentionedNumber = -1
        needLoadMentionedAvatars = true
        mapMentioned.clear()
        requireBinding().listAvatarsMentioned.avatar1.isVisible = false
        requireBinding().listAvatarsMentioned.avatar2.isVisible = false
        requireBinding().listAvatarsMentioned.avatar3.isVisible = false
        requireBinding().listAvatarsMentioned.avatar4.isVisible = false
        requireBinding().listAvatarsMentioned.avatar5.isVisible = false
    }

    private fun fillLikers() {
        mapLikers[0] = requireBinding().listAvatarsLikers.avatar1
        mapLikers[1] = requireBinding().listAvatarsLikers.avatar2
        mapLikers[2] = requireBinding().listAvatarsLikers.avatar3
        mapLikers[3] = requireBinding().listAvatarsLikers.avatar4
        mapLikers[4] = requireBinding().listAvatarsLikers.avatar5


    }

    private fun fillMentioned() {
        mapMentioned[0] = requireBinding().listAvatarsMentioned.avatar1
        mapMentioned[1] = requireBinding().listAvatarsMentioned.avatar2
        mapMentioned[2] = requireBinding().listAvatarsMentioned.avatar3
        mapMentioned[3] = requireBinding().listAvatarsMentioned.avatar4
        mapMentioned[4] = requireBinding().listAvatarsMentioned.avatar5
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
