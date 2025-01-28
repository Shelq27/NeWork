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
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.PostDetailsFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.AndroidUtils.share
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.PostViewModel
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class PostDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by idArg
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

    private lateinit var binding: PostDetailsFragmentBinding
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
                        binding.listAvatarsLikers.ShowMoreB.isVisible =
                            post.likeOwnerIds.size > 5
                    }
                }
                binding.listAvatarsLikers.ShowMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_postDetailsFragment_to_postLikersFragment)
                }

                if (post.mentionIds.isNotEmpty()) {
                    if (needLoadMentionedAvatars) {
                        viewModel.getMentioned(post)
                        fillMentioned()
                        needLoadMentionedAvatars = false
                        binding.listAvatarsMentioned.ShowMoreB.isVisible = post.mentionIds.size > 5
                    }
                }
                binding.listAvatarsMentioned.ShowMoreB.setOnClickListener {
                    findNavController().navigate(R.id.action_postDetailsFragment_to_postMentionedFragment)
                }


                binding.apply {

                    if (post.authorJob != null) {
                        NameJobTV.text = post.authorJob
                    } else {
                        NameJobTV.text = getText(R.string.looking_for_a_job)
                    }
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
                    DatePublicationPostTV.text =
                        AndroidUtils.dateFormatToText(post.published, root.context)
                    TextPostTV.text = post.content
                    if (post.link != null) {
                        LinkPostTV.visibility = View.VISIBLE
                        LinkPostTV.text = post.link
                    } else {
                        LinkPostTV.visibility = View.GONE
                    }
                    MentionedB.isChecked = post.mentionedMe
                    MentionedB.text = post.mentionIds.size.toString()
                    LikeIB.text = post.likeOwnerIds.size.toString()
                    LikeIB.isChecked = post.likedByMe
                    LikeIB.setOnClickListener {
                        if (auth.authenticated()) {
                            viewModel.likeByPost(post)
                        } else {
                            LikeIB.isChecked = post.likedByMe
                            AndroidUtils.showSignInDialog(this@PostDetailsFragment)
                        }
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

    private fun clearMentionAvatars() {
        mentionedNumber = -1
        needLoadMentionedAvatars = true
        mapMentioned.clear()
        binding.listAvatarsMentioned.avatar1.isVisible = false
        binding.listAvatarsMentioned.avatar2.isVisible = false
        binding.listAvatarsMentioned.avatar3.isVisible = false
        binding.listAvatarsMentioned.avatar4.isVisible = false
        binding.listAvatarsMentioned.avatar5.isVisible = false
    }

    private fun fillLikers() {
        mapLikers[0] = binding.listAvatarsLikers.avatar1
        mapLikers[1] = binding.listAvatarsLikers.avatar2
        mapLikers[2] = binding.listAvatarsLikers.avatar3
        mapLikers[3] = binding.listAvatarsLikers.avatar4
        mapLikers[4] = binding.listAvatarsLikers.avatar5


    }

    private fun fillMentioned() {
        mapMentioned[0] = binding.listAvatarsMentioned.avatar1
        mapMentioned[1] = binding.listAvatarsMentioned.avatar2
        mapMentioned[2] = binding.listAvatarsMentioned.avatar3
        mapMentioned[3] = binding.listAvatarsMentioned.avatar4
        mapMentioned[4] = binding.listAvatarsMentioned.avatar5
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
