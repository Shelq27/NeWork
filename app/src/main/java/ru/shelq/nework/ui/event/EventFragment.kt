package ru.shelq.nework.ui.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.util.idArg

class EventFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg

    }
}