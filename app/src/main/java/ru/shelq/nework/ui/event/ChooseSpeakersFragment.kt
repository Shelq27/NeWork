package ru.shelq.nework.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.adapter.CheckOnInteractionListener
import ru.shelq.nework.adapter.ChooseUserAdapter
import ru.shelq.nework.databinding.ChooseUsersFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.util.LongArrayArg
import ru.shelq.nework.viewmodel.EventViewModel
import ru.shelq.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class ChooseSpeakersFragment : Fragment() {

    companion object {
        var Bundle.longArrayArg: LongArray? by LongArrayArg
    }

    private val userViewModel: UserViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ChooseUsersFragmentBinding.inflate(inflater,
            container,
            false
        )
        userViewModel.loadUsers()
        val checkedUsers = arguments?.longArrayArg ?: arrayOf<Long>().toLongArray()

        val adapter = ChooseUserAdapter(
            checkedUsers,
            object : CheckOnInteractionListener {
                override fun onCheck(user: User, checked: Boolean) {
                    if(checked){
                        eventViewModel.chooseUser(user)
                    } else
                        eventViewModel.removeUser(user)
                }
            })
        binding.ListUserChoose.adapter = adapter


        userViewModel.data.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        binding.ChooseUserTTB.setOnMenuItemClickListener {
            findNavController().navigateUp()
        }
        binding.ChooseUserTTB.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

}