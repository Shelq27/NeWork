package ru.shelq.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.activity.auth.SignUpViewModel
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.SignUpFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: SignUpViewModel by activityViewModels()
    private lateinit var binding: SignUpFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        
        return binding.root
    }
}