package com.navar.trainova.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.navar.trainova.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import java.util.List;

public class MultiDatePickerDialogFragment extends DialogFragment {

    /** Interfaz para devolver la lista de días seleccionados */
    public interface OnMultiDateSetListener {
        void onDatesSelected(List<CalendarDay> dates);
    }

    private OnMultiDateSetListener listener;

    public static MultiDatePickerDialogFragment newInstance() {
        return new MultiDatePickerDialogFragment();
    }

    public void setOnMultiDateSetListener(OnMultiDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /** Inflamos nuestro layout personalizado */
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_multi_date_picker, null);
        MaterialCalendarView calendarView = view.findViewById(R.id.dialogCalendarView);

        /** Activamos el modo de selección múltiple */
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);

        /** Construimos el diálogo usando AlertDialog para tener botones estándar */
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
            .setTitle("Selecciona uno o más días")
            .setPositiveButton("Aceptar", (dialog, which) -> {
                if (listener != null) {
                    // Obtenemos la lista de días seleccionados y la pasamos a la actividad
                    List<CalendarDay> selectedDates = calendarView.getSelectedDates();
                    if (!selectedDates.isEmpty()) {
                        listener.onDatesSelected(selectedDates);
                    }
                }
            })
            .setNegativeButton("Cancelar", (dialog, which) -> dismiss());

        return builder.create();
    }
}