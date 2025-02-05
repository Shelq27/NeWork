package ru.shelq.nework.ui.users

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.JobNewFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class JobNewFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth
    private lateinit var binding: JobNewFragmentBinding

    @Inject
    lateinit var factory: JobViewModel.Factory

    private val jobViewModel: JobViewModel by viewModels {
        JobViewModel.provideJobViewModelFactory(
            factory,
            auth.authState.value.id
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = JobNewFragmentBinding.inflate(layoutInflater, container, false)
        jobViewModel.newJob.observe(viewLifecycleOwner) {
            binding.EnterDatesB.text = formatDate(it.start) + " - " + formatDate(it.finish)
        }

        jobViewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.EnterDatesB.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.job_dates_picker)
            dialog.window?.decorView?.setBackgroundResource(R.drawable.dialog_background)
            val datePicker = dialog.findViewById<ImageButton>(R.id.DatePickerIB)
            //начальная дата
            val startDate = dialog.findViewById<TextInputEditText>(R.id.DateStartTI)
            val calendarStart = AndroidUtils.dateUTCToCalendar(jobViewModel.newJob.value!!.start)
            startDate.setText(
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
                    calendarStart.time
                )
            )
            //конечная дата
            val endDate = dialog.findViewById<TextInputEditText>(R.id.date_end_input)
            val calendarEnd = Calendar.getInstance()
            jobViewModel.newJob.value!!.finish?.let {
                calendarEnd.time = Date.from(ZonedDateTime.parse(it).toInstant())
            }
            endDate.setText(
                SimpleDateFormat(
                    "MM/dd/yyyy",
                    Locale.getDefault()
                ).format(calendarEnd.time)
            )

            datePicker.setOnClickListener {
                val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setSelection(Pair(calendarStart.timeInMillis, calendarEnd.timeInMillis))
                    .build()
                materialDatePicker.addOnPositiveButtonClickListener { pair ->
                    calendarStart.timeInMillis = pair.first
                    startDate.setText(
                        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
                            pair.first
                        )
                    )
                    pair.second?.let {
                        calendarEnd.timeInMillis = it
                    }

                    endDate.setText(
                        SimpleDateFormat(
                            "MM/dd/yyyy",
                            Locale.getDefault()
                        ).format(pair.second)
                    )
                }
                materialDatePicker.show(childFragmentManager, "tag")
            }
            val clearFinish = dialog.findViewById(R.id.ClearDateIB) as ImageButton
            clearFinish.setOnClickListener {
                endDate.setText("")
            }
            val yesBtn = dialog.findViewById(R.id.OkIB) as Button
            yesBtn.setOnClickListener {
                jobViewModel.setStart(AndroidUtils.calendarToUTCDate(calendarStart))
                if (endDate.text.toString() == "") {
                    jobViewModel.setFinish(null)
                } else calendarEnd.let { jobViewModel.setFinish(AndroidUtils.calendarToUTCDate(it)) }

                dialog.dismiss()
            }
            val noBtn = dialog.findViewById(R.id.CancelIB) as Button
            noBtn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

        }

        binding.CreateJobB.setOnClickListener {
            jobViewModel.setName(binding.NameJobET.text.toString())
            jobViewModel.setPosition(binding.PositionET.text.toString())
            jobViewModel.setLink(binding.LinkET.text.toString())
            jobViewModel.save()
        }

        jobViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
                jobViewModel.resetError()
            }
        }
        return binding.root
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return "НВ"
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
            AndroidUtils.dateUTCToCalendar(dateString).time
        )
    }

}