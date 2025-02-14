package ru.shelq.nework.ui.auth

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private val viewModel: SignUpViewModel by viewModels()
    private var binding: SignUpFragmentBinding? = null
    private fun requireBinding() = requireNotNull(binding)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignUpFragmentBinding.inflate(inflater, container, false)
        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            requireBinding().root,
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


        requireBinding().loadAvatarIB.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(2048)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg"
                    )
                )
                .createIntent(photoLauncher::launch)
        }


        viewModel.photo.observe(viewLifecycleOwner) {

            requireBinding().loadAvatarIB.setImageURI(it.uri)
        }
        enableLogin()
        requireBinding().nameTI.doOnTextChanged { _, _, _, _ ->
            enableLogin()
        }
        requireBinding().loginTI.doOnTextChanged { _, _, _, _ ->
            enableLogin()
        }

        requireBinding().passTI.doOnTextChanged { _, _, _, _ ->
            enableLogin()
        }
        requireBinding().passRepeat.doOnTextChanged { _, _, _, _ ->
            enableLogin()
        }
        requireBinding().registrationMTB.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        requireBinding().signUpB.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (requireBinding().passTI.text.toString() != requireBinding().passTI.text.toString()) {
                Snackbar.make(
                    requireBinding().root,
                    getString(R.string.pass_don_t_match), Snackbar.LENGTH_LONG
                ).show()
            } else {
                viewModel.name.value = requireBinding().nameTI.text.toString()
                viewModel.login.value = requireBinding().loginTI.text.toString()
                viewModel.pass.value = requireBinding().passTI.text.toString()
                viewModel.signUp()
            }

        }
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            appAuth.setAuth(state.id, state.token!!)
            findNavController().navigateUp()
        }

        viewModel.userAuthResult.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(requireBinding().root, state.message, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        return requireBinding().root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun enableLogin() {
        requireBinding().signUpB.isEnabled =
            requireBinding().nameTI.text.toString().isNotEmpty() &&
                    requireBinding().loginTI.text.toString().isNotEmpty() &&
                    requireBinding().passTI.text.toString().isNotEmpty() &&
                    requireBinding().passRepeat.text.toString().isNotEmpty()
    }

}
