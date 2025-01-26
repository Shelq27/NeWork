package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.databinding.PostEditFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.viewmodel.PostViewModel

class PostEditFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostEditFragmentBinding.inflate(inflater, container, false)
        val bindingEdit = binding.PostEditFragmnet
        val viewModel: PostViewModel by activityViewModels()

        val text = arguments?.text
        if (text != null) {
            bindingEdit.ContentPostET.setText(text)

        }
        bindingEdit.NewPostTTB.setOnClickListener {
            findNavController().navigateUp()
        }
        bindingEdit.NewPostTTB.setOnMenuItemClickListener {
            val content = bindingEdit.ContentPostET.text.toString()

            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        bindingEdit.NewPostTTB.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        return binding.root
    }
}