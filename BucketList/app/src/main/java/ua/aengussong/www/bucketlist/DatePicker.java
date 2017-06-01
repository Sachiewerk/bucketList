package ua.aengussong.www.bucketlist;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by coolsmileman on 01.06.2017.
 */

public class DatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // define current date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // create DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                year, month, day);
        //set current date min date that user can choose
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        Dialog picker = datePickerDialog;
        picker.setTitle(getResources().getString(R.string.choose_target_date));

        return picker;
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year,
                          int month, int day) {

        TextView addTargetDate = (TextView) getActivity().findViewById(R.id.add_target_date_edit);
        addTargetDate.setText(day + "-" + month + "-" + year);
    }
}
