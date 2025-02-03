package ru.shelq.nework.ui.post

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.databinding.PostNewFragmentBinding
import ru.shelq.nework.dto.Attachment
import ru.shelq.nework.dto.Coordinates
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.ui.post.ChooseMentionedFragment.Companion.longArrayArg
import ru.shelq.nework.ui.post.PostMapFragment.Companion.lat
import ru.shelq.nework.ui.post.PostMapFragment.Companion.long
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.addMarkerOnMap
import ru.shelq.nework.util.AndroidUtils.getFile
import ru.shelq.nework.util.AndroidUtils.moveCamera
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.viewmodel.PostViewModel
import java.io.FileNotFoundException
import java.io.IOException

@AndroidEntryPoint
class PostNewFragment : Fragment() {
    companion object {
        const val MAX_SIZE = 15728640
        var Bundle.id by IdArg
    }


    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireActivity)
    private val mediaObserver = MediaLifecycleObserver()
    private lateinit var checkedUsers: LongArray
    private lateinit var binding: PostNewFragmentBinding


    private fun removeAttachment() {
        viewModel.changeAttachment(null, null, null, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostNewFragmentBinding.inflate(layoutInflater)
        lifecycle.addObserver(mediaObserver)


        binding.NewPostTTB.setNavigationOnClickListener {
            findNavController().navigateUp()
        }




        val postId = arguments?.id ?: -1L
        if (postId != -1L) {
            viewModel.getPostById(postId)
        }
        viewModel.selectedPost.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.edit(it)
            }
        }

        viewModel.edited.observe(viewLifecycleOwner) {
            if (viewModel.edited.value?.id != 0L && viewModel.changed.value != true) {
                val edited = viewModel.edited.value
                binding.ContentPostET.setText(edited?.content)
                binding.Link.setText(edited?.link)

                edited?.attachment?.let {
                    viewModel.changeAttachment(it.url, null, null, it.type)
                }
                edited?.mentionIds?.let {
                    viewModel.changeMentionedNewPost(it)
                }

                edited?.coords?.let {
                    viewModel.changeCoords(it)
                }
            }
        }

        val longCoord = arguments?.long ?: -0.0
        val latCoord = arguments?.lat ?: -0.0

        if (longCoord != -0.0 && latCoord != -0.0) {
            val point = Point(latCoord, longCoord)
            setMarker(point)
            moveToMarker(point)
            viewModel.changeCoords(Coordinates(latCoord, longCoord))
        }

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                try {
                    if (uri != null) {
                        if (mediaObserver.mediaPlayer?.isPlaying == true) {
                            mediaObserver.stop()
                        }
                        binding.audioContainer.audioPlay.audioSB.progress = 0
                        val fileDescriptor =
                            requireContext().contentResolver.openAssetFileDescriptor(uri, "r")
                        val fileSize = fileDescriptor?.length ?: 0
                        if (fileSize > MAX_SIZE) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.attachment_no_more_15mb),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@registerForActivityResult
                        }
                        fileDescriptor?.close()
                        val file = uri.getFile(requireContext())
                        if (requireContext().contentResolver.getType(uri)
                                ?.startsWith("audio/") == true
                        ) {
                            viewModel.changeAttachment(null, uri, file, AttachmentType.AUDIO)
                        } else {
                            if (requireContext().contentResolver.getType(uri)
                                    ?.startsWith("video/") == true
                            ) {
                                viewModel.changeAttachment(null, uri, file, AttachmentType.VIDEO)
                            }
                        }
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        viewModel.attachment.observe(viewLifecycleOwner) {
            if (it == null) {
                if (mediaObserver.mediaPlayer?.isPlaying == true) {
                    mediaObserver.stop()
                }
                binding.audioContainer.audioPlay.audioSB.progress = 0
                binding.imageContainer.photoContainer.visibility = View.GONE
                binding.audioContainer.audioLoad.visibility = View.GONE
                binding.videoContainer.videoLoad.visibility = View.GONE
            } else {
                when (it.attachmentType) {

                    AttachmentType.IMAGE -> {
                        binding.audioContainer.audioLoad.visibility = View.GONE
                        binding.videoContainer.videoLoad.visibility = View.GONE
                        binding.imageContainer.photoContainer.visibility = View.VISIBLE
                        if (it.url != null) {
                            Glide.with(binding.imageContainer.photo)
                                .load("${it.url}")
                                .placeholder(R.drawable.ic_downloading_100dp)
                                .error(R.drawable.ic_error_outline_100dp)
                                .timeout(10_000)
                                .centerCrop()
                                .into(binding.imageContainer.photo)
                        } else {
                            binding.imageContainer.photo.setImageURI(it.uri)
                        }
                    }

                    AttachmentType.AUDIO -> {
                        binding.audioContainer.audioLoad.visibility = View.VISIBLE
                        binding.imageContainer.photoContainer.visibility = View.GONE
                        binding.videoContainer.videoLoad.visibility = View.GONE

                    }

                    AttachmentType.VIDEO -> {
                        if (mediaObserver.mediaPlayer?.isPlaying == true) {
                            mediaObserver.stop()
                        }
                        binding.audioContainer.audioLoad.visibility = View.GONE
                        binding.imageContainer.photoContainer.visibility = View.GONE
                        binding.videoContainer.videoLoad.visibility = View.VISIBLE
                        Glide.with(binding.videoContainer.videoPlay.videoThumb)
                            .load(it.url ?: it.uri)
                            .into(binding.videoContainer.videoPlay.videoThumb)

                    }

                    else -> Unit
                }
            }

        }

        viewModel.mentionedNewPost.observe(viewLifecycleOwner) {
            checkedUsers = it.toLongArray()
        }

        viewModel.coords.observe(viewLifecycleOwner) {
            binding.GeoPostMW.map.mapObjects.clear()
            if (it == null) {
                binding.CoordsContainerCL.visibility = View.GONE
            } else {
                binding.CoordsContainerCL.visibility = View.VISIBLE
                moveCamera(binding.GeoPostMW, Point(it.lat, it.long))
                addMarkerOnMap(
                    requireContext(),
                    binding.GeoPostMW,
                    Point(it.lat, it.long)
                )
            }
        }
        binding.RemoveCoords.setOnClickListener {
            viewModel.changeCoords(null)
        }
        binding.ContentPostET.requestFocus()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changeAttachment(null, uri, uri?.toFile(), AttachmentType.IMAGE)
                    }
                }
            }






        binding.imageContainer.removePhoto.setOnClickListener {
            removeAttachment()
        }
        binding.audioContainer.removeAudio.setOnClickListener {
            removeAttachment()
        }
        binding.videoContainer.removeVideo.setOnClickListener {
            removeAttachment()
        }
        binding.audioContainer.audioPlay.playAudioIB.setOnClickListener {

            if (viewModel.attachment.value?.url != null) {
                mediaObserver.playAudio(
                    Attachment(
                        url = viewModel.attachment.value!!.url.toString(),
                        type = AttachmentType.AUDIO
                    ),
                    binding.audioContainer.audioPlay.audioSB,
                    binding.audioContainer.audioPlay.playAudioIB
                )
            } else {
                requireContext().contentResolver.openAssetFileDescriptor(
                    viewModel.attachment.value!!.uri!!,
                    "r"
                )
                    ?.let {
                        mediaObserver.playAudioFromDescriptor(
                            it,
                            binding.audioContainer.audioPlay.audioSB,
                            binding.audioContainer.audioPlay.playAudioIB
                        )
                    }
            }
        }
        binding.videoContainer.videoPlay.playVideoIB.setOnClickListener {
            viewModel.attachment.value?.let { attachment ->
                binding.apply {
                    videoContainer.videoPlay.videoView.visibility = View.VISIBLE
                    videoContainer.videoPlay.videoView.apply {
                        setMediaController(MediaController(context))

                        if (attachment.url != null) {
                            val uri = Uri.parse(attachment.url)
                            setVideoURI(uri)
                        } else {
                            setVideoURI(attachment.uri)
                        }

                        setOnPreparedListener {
                            videoContainer.videoPlay.videoThumb.visibility = View.GONE
                            videoContainer.videoPlay.playVideoIB.visibility = View.GONE
                            start()
                        }
                        setOnCompletionListener {
                            stopPlayback()
                            videoContainer.videoPlay.videoView.visibility = View.GONE
                            videoContainer.videoPlay.playVideoIB.visibility = View.VISIBLE
                            videoContainer.videoPlay.videoThumb.visibility = View.VISIBLE
                        }
                    }
                }
            }

        }

        binding.NewPostBB.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {

                R.id.take_photo -> {
                    ImagePicker.with(this)
                        .crop()
                        .compress(MAX_SIZE)
                        .galleryMimeTypes(
                            arrayOf(
                                "image/png",
                                "image/jpeg",
                            )
                        )
                        .createIntent(pickPhotoLauncher::launch)
                    true
                }

                R.id.load_attachment -> {
                    val choose = arrayOf("audio/*", "video/*")
                    resultLauncher.launch(choose)
                    true
                }

                R.id.users -> {
                    findNavController().navigate(R.id.action_postNewFragment_to_chooseUsersFragment,
                        args = Bundle().apply {
                            longArrayArg = checkedUsers
                        })
                    true
                }

                R.id.geolocation -> {
                    findNavController().navigate(
                        R.id.action_postNewFragment_to_postMapFragment
                    )
                    true
                }

                else -> false
            }
        }

        binding.NewPostTTB.setOnMenuItemClickListener {
            val content = binding.ContentPostET.text.toString()
            val link = binding.Link.text.toString()
            viewModel.changeContent(content)
            viewModel.changeLink(link)
            viewModel.save()
            AndroidUtils.hideKeyboard(requireView())
            true
        }
        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.postFragment)
        }

        return binding.root
    }

    // Отображаем карты перед тем моментом, когда активити с картой станет видимой пользователю:
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.GeoPostMW.onStart()
    }

    // Останавливаем обработку карты, когда активити с картой становится невидимым для пользователя:
    override fun onStop() {
        binding.GeoPostMW.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun setMarker(point: Point) {
        addMarkerOnMap(requireContext(), binding.GeoPostMW, point)
    }

    private fun moveToMarker(point: Point) {
        moveCamera(binding.GeoPostMW, point)
    }
}






