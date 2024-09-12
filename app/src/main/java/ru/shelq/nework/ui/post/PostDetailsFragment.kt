package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.databinding.PostDetailsFragmentBinding
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.PostViewModel

class PostDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by idArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = PostDetailsFragmentBinding.inflate(inflater, container, false)
        val viewModelPost: PostViewModel by activityViewModels()

        val postId = arguments?.id ?: -1
        viewModelPost.getPostById(postId)
        viewModelPost.selectedPost.observe(viewLifecycleOwner) { post ->

            if (post != null)
                binding.apply {
                    PostDetailsTBL.setOnClickListener {
                        findNavController().navigateUp()
                    }
                    AuthorTV.text = post.content
                    DatePublicationPostTV.text = post.published
                    TextPostTV.text = post.content
                    LinkPostTV.text = post.link
                    LikeB.text = post.likeOwnerIds.toString()

                }
        }

        return binding.root
    }
}