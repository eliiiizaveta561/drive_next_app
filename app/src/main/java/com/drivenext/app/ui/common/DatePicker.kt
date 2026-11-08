package com.drivenext.app.ui.common

import android.widget.EditText
import androidx.fragment.app.Fragment
import com.drivenext.app.R
import com.drivenext.app.domain.validation.Validator
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.time.ZoneOffset

fun Fragment.attachDatePicker(layout: TextInputLayout, field: EditText) {
    val openPicker = { showDatePicker { date -> field.setText(date) } }
    layout.setStartIconOnClickListener { openPicker() }
    field.setOnClickListener { openPicker() }
}

private fun Fragment.showDatePicker(onDatePicked: (String) -> Unit) {
    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.select_date)
        .build()

    datePicker.addOnPositiveButtonClickListener { selectedDateUtcMillis ->
        val date = Instant.ofEpochMilli(selectedDateUtcMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        onDatePicked(Validator.dateFormatter.format(date))
    }

    datePicker.show(parentFragmentManager, "date_picker")
}
