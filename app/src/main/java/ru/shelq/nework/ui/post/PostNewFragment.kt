package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.R
import ru.shelq.nework.databinding.PostNewFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.viewmodel.PostViewModel

class PostNewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModelPost by activityViewModels<PostViewModel>()
        val binding = PostNewFragmentBinding.inflate(layoutInflater)
        binding.ContentPostET.requestFocus()

        binding.NewPostTTB.setOnMenuItemClickListener {
            val content = binding.ContentPostET.text.toString()
            if (content.isBlank()) {
                Toast.makeText(context, "Error : can't empty", Toast.LENGTH_LONG).show()
            }
            viewModelPost.changeContentAndSave(content)
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        binding.NewPostTTB.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}