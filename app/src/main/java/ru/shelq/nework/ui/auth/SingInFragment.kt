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
    private lateinit var binding: SignInFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignInFragmentBinding.inflate(layoutInflater)

        binding.Login.doOnTextChanged{text, start, before, count ->
            enableLogin()
        }

        passError()
        binding.Pass.doOnTextChanged{text, start, before, count ->
            if(text == null){
                passError()
            }
            else{
                binding.PasswordETL.error = null
            }
            enableLogin()
        }
        binding.SignIn.isEnabled = binding.Login.text.toString().isNotEmpty() && binding.Pass.text.toString().isNotEmpty()

        binding.SignIn.setOnClickListener{
            AndroidUtils.hideKeyboard(requireView())
            viewModel.login.value = binding.Login.text.toString()
            viewModel.pass.value = binding.Pass.text.toString()
            viewModel.signIn()
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

        binding.SignUpB.setOnClickListener{
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.LoginMTB.setNavigationOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }

    private fun passError(){
        binding.PasswordETL.error = getString(R.string.password_empty)
    }

    private fun enableLogin(){
        binding.SignIn.isEnabled = binding.Login.text.toString().isNotEmpty() && binding.Pass.text.toString().isNotEmpty()
    }
}
