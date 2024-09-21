package ru.shelq.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.R
import ru.shelq.nework.databinding.SignInFragmentBinding

class SingInFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = SignInFragmentBinding.inflate(layoutInflater)

        binding.RegB.setOnClickListener{
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.LoginMTB.setNavigationOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }

}
