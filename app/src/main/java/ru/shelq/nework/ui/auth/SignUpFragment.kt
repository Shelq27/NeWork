package ru.shelq.nework.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.SignUpFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: SignUpViewModel by activityViewModels()
    private lateinit var binding: SignUpFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignUpFragmentBinding.inflate(inflater, container, false)
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
                    viewModel.changePhoto(uri, uri?.toFile())
                }
            }
        }
        binding.LoadAvatarIB.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(2048)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg"
                    )
                )
                .start(20)
        }


        viewModel.photo.observe(viewLifecycleOwner) {

            binding.LoadAvatarIB.setImageURI(it.uri)
        }
        enableLogin()
        binding.Name.doOnTextChanged { text, start, before, count ->
            enableLogin()
        }
        binding.Login.doOnTextChanged { text, start, before, count ->
            enableLogin()
        }

        binding.Pass.doOnTextChanged { text, start, before, count ->
            enableLogin()
        }
        binding.PassRepeat.doOnTextChanged { text, start, before, count ->
            enableLogin()
        }
        binding.SignUpB.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.Pass.text.toString() != binding.PassRepeat.text.toString()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.pass_don_t_match), Snackbar.LENGTH_LONG
                ).show()
            } else {
                viewModel.name.value = binding.Name.text.toString()
                viewModel.login.value = binding.Login.text.toString()
                viewModel.pass.value = binding.Pass.text.toString()
                viewModel.signUp()
            }

        }
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            appAuth.setAuth(state.id, state.token!!)
            findNavController().navigateUp()
        }

        viewModel.userAuthResult.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        return binding.root
    }

    private fun enableLogin() {
        binding.SignUpB.isEnabled = binding.Name.text.toString().isNotEmpty() &&
                binding.Login.text.toString().isNotEmpty() &&
                binding.Pass.text.toString().isNotEmpty() &&
                binding.PassRepeat.text.toString().isNotEmpty()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                viewModel.changePhoto(uri, uri.toFile())
                binding.LoadAvatarIB.setImageURI(uri)
            }

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {
                Toast.makeText(requireContext(), getString(R.string.cancelled), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}