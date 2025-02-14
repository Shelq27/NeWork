package ru.shelq.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.SignInFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.viewmodel.SignInViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SingInFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: SignInViewModel by viewModels()
    private var binding: SignInFragmentBinding? = null
    private fun requireBinding() = requireNotNull(binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignInFragmentBinding.inflate(layoutInflater)

        requireBinding().loginTI.doOnTextChanged { _, _, _, _ ->
            enableLogin()
        }

        passError()
        requireBinding().passTI.doOnTextChanged { text, _, _, _ ->
            if (text == null) {
                passError()
            } else {
                requireBinding().passwordETL.error = null
            }
            enableLogin()
        }
        requireBinding().signIn.isEnabled =
            requireBinding().loginTI.text.toString()
                .isNotEmpty() && requireBinding().passTI.text.toString()
                .isNotEmpty()

        requireBinding().signIn.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            viewModel.login.value = requireBinding().loginTI.text.toString()
            viewModel.pass.value = requireBinding().passTI.text.toString()
            viewModel.signIn()
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

        requireBinding().signUpB.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        requireBinding().loginMTB.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        return requireBinding().root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun passError() {
        requireBinding().passwordETL.error = getString(R.string.password_empty)
    }

    private fun enableLogin() {
        requireBinding().signIn.isEnabled =
            requireBinding().loginTI.text.toString().isNotEmpty() && requireBinding().passTI.text.toString()
                .isNotEmpty()
    }
}
