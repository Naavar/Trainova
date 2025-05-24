package com.navar.trainova.ui.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.data.model.ColorOption;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.ui.adapters.ColorSpinnerAdapter;
import com.navar.trainova.ui.home.HomeViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventoCreateEditDialogFragment extends DialogFragment {

    public static final String TAG_CREATE = "EventoCreateDialog";
    public static final String TAG_EDIT = "EventoEditDialog";

    private static final String ARG_CALENDAR_DAY = "calendar_day";
    private static final String ARG_EVENTO_TO_EDIT = "evento_to_edit";
    private static final String ARG_PLANTILLA = "plantilla_para_crear";

    private HomeViewModel homeViewModel;

    private EditText inputNombre;
    private Spinner spinnerTipoActividad;
    private Spinner spinnerColor;
    private Spinner spinnerEstado;
    private Button btnHoraInicio;
    private Button btnHoraFin;
    private EditText inputDescripcion;
    private Button btnGuardar;
    private Button btnCancelar;

    private CalendarDay selectedCalendarDay;
    private Evento eventoToEdit;
    private CatalogoEvento plantillaBase;

    private int[] horaInicioSeleccionada = {0, 0};
    private int[] horaFinSeleccionada = {0, 0};

    public static EventoCreateEditDialogFragment newInstanceForCreate(CalendarDay day) {
        EventoCreateEditDialogFragment fragment = new EventoCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CALENDAR_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventoCreateEditDialogFragment newInstanceForEdit(Evento evento) {
        EventoCreateEditDialogFragment fragment = new EventoCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENTO_TO_EDIT, evento);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventoCreateEditDialogFragment newInstanceForCreateFromTemplate(CalendarDay day, CatalogoEvento plantilla) {
        EventoCreateEditDialogFragment fragment = new EventoCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CALENDAR_DAY, day);
        args.putParcelable(ARG_PLANTILLA, plantilla);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        if (getArguments() != null) {
            selectedCalendarDay = getArguments().getParcelable(ARG_CALENDAR_DAY);
            eventoToEdit = getArguments().getParcelable(ARG_EVENTO_TO_EDIT);
            plantillaBase = getArguments().getParcelable(ARG_PLANTILLA);

            if (eventoToEdit != null) {
                selectedCalendarDay = eventoToEdit.getCalendarDay();
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_crear_actividad, null);

        inputNombre = view.findViewById(R.id.inputNombreActividadDialog);
        spinnerTipoActividad = view.findViewById(R.id.spinnerTipoActividadDialog);
        spinnerColor = view.findViewById(R.id.spinnerColorDialog);
        spinnerEstado = view.findViewById(R.id.spinnerEstadoDialog);
        btnHoraInicio = view.findViewById(R.id.btnHoraInicioDialog);
        btnHoraFin = view.findViewById(R.id.btnHoraFinDialog);
        inputDescripcion = view.findViewById(R.id.inputDescripcionDialog);
        btnGuardar = view.findViewById(R.id.btnGuardarDialog);
        btnCancelar = view.findViewById(R.id.btnCancelarDialog);

        setupSpinnersWithoutObservers();
        setupTimePickers();
        setupColorObserver();

        if (eventoToEdit != null) {
            builder.setTitle("Editar Actividad");
            populateFieldsForEdit();
        } else if (plantillaBase != null) {
            builder.setTitle("Crear desde Plantilla");
            populateFieldsFromTemplate();
        } else {
            builder.setTitle("Crear Nueva Actividad");
            btnHoraInicio.setText("HH:MM");
            btnHoraFin.setText("HH:MM");
        }

        btnGuardar.setOnClickListener(v -> saveEvent());
        btnCancelar.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void setupSpinnersWithoutObservers() {
        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.tipos_actividad, android.R.layout.simple_spinner_item);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoActividad.setAdapter(tipoAdapter);

        String[] estados = {"Pendiente", "En progreso", "Completado", "Cancelado"};
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, estados);
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);
    }

    private void setupColorObserver() {
        homeViewModel.colorOptions.observe(this, colorOptions -> {
            if (colorOptions != null && !colorOptions.isEmpty()) {
                ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(requireContext(), colorOptions);
                spinnerColor.setAdapter(colorAdapter);

                if (eventoToEdit != null) {
                    selectColorInSpinner(colorOptions, eventoToEdit.getColor());
                } else if (plantillaBase != null) {
                    selectColorInSpinner(colorOptions, plantillaBase.getColorEvento());
                }
            }
        });
    }

    private void setupTimePickers() {
        btnHoraInicio.setOnClickListener(v -> showTimePicker(true));
        btnHoraFin.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(final boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int hourToSet = isStartTime ? horaInicioSeleccionada[0] : horaFinSeleccionada[0];
        int minuteToSet = isStartTime ? horaInicioSeleccionada[1] : horaFinSeleccionada[1];

        if ((hourToSet == 0 && minuteToSet == 0)) {
            hourToSet = currentHour;
            minuteToSet = currentMinute;
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
            (view, hourOfDay, min) -> {
                if (isStartTime) {
                    horaInicioSeleccionada[0] = hourOfDay;
                    horaInicioSeleccionada[1] = min;
                    btnHoraInicio.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, min));
                } else {
                    horaFinSeleccionada[0] = hourOfDay;
                    horaFinSeleccionada[1] = min;
                    btnHoraFin.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, min));
                }
            }, hourToSet, minuteToSet, true);
        timePickerDialog.show();
    }

    private void populateFieldsForEdit() {
        if (eventoToEdit == null) return;
        inputNombre.setText(eventoToEdit.getNombre());
        setSpinnerSelection(spinnerTipoActividad, eventoToEdit.getTipoActividad());
        setSpinnerSelection(spinnerEstado, eventoToEdit.getEstado());
        inputDescripcion.setText(eventoToEdit.getDescripcion());

        if (eventoToEdit.getHoraInicio() != null && !eventoToEdit.getHoraInicio().isEmpty() && !eventoToEdit.getHoraInicio().equals("HH:MM")) {
            try {
                String[] partsInicio = eventoToEdit.getHoraInicio().split(":");
                horaInicioSeleccionada[0] = Integer.parseInt(partsInicio[0]);
                horaInicioSeleccionada[1] = Integer.parseInt(partsInicio[1]);
                btnHoraInicio.setText(eventoToEdit.getHoraInicio());
            } catch (Exception e) {
                btnHoraInicio.setText("HH:MM");
            }
        } else {
            btnHoraInicio.setText("HH:MM");
        }

        if (eventoToEdit.getHoraFin() != null && !eventoToEdit.getHoraFin().isEmpty() && !eventoToEdit.getHoraFin().equals("HH:MM")) {
            try {
                String[] partsFin = eventoToEdit.getHoraFin().split(":");
                horaFinSeleccionada[0] = Integer.parseInt(partsFin[0]);
                horaFinSeleccionada[1] = Integer.parseInt(partsFin[1]);
                btnHoraFin.setText(eventoToEdit.getHoraFin());
            } catch (Exception e) {
                btnHoraFin.setText("HH:MM");
            }
        } else {
            btnHoraFin.setText("HH:MM");
        }
    }

    private void populateFieldsFromTemplate() {
        if (plantillaBase == null) return;
        inputNombre.setText(plantillaBase.getNombreEvento());
        inputDescripcion.setText(plantillaBase.getDescripcion());
        setSpinnerSelection(spinnerTipoActividad, plantillaBase.getTipoEvento());
        btnHoraInicio.setText("HH:MM");
        btnHoraFin.setText("HH:MM");
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (value.equals(adapter.getItem(position).toString())) {
                spinner.setSelection(position);
                return;
            }
        }
    }

    private void selectColorInSpinner(List<ColorOption> colorOptions, int colorValue) {
        for (int i = 0; i < colorOptions.size(); i++) {
            if (colorOptions.get(i).getColorValue() == colorValue) {
                spinnerColor.setSelection(i);
                break;
            }
        }
    }

    private void saveEvent() {
        String nombre = inputNombre.getText().toString().trim();
        String tipoActividad = spinnerTipoActividad.getSelectedItem().toString();
        ColorOption selectedColorOption = (spinnerColor.getSelectedItem() instanceof ColorOption) ? (ColorOption) spinnerColor.getSelectedItem() : null;
        int color = (selectedColorOption != null) ? selectedColorOption.getColorValue() : android.graphics.Color.parseColor("#FFA000");
        String estado = spinnerEstado.getSelectedItem().toString();
        String horaInicioStr = btnHoraInicio.getText().toString();
        String horaFinStr = btnHoraFin.getText().toString();
        String descripcion = inputDescripcion.getText().toString().trim();

        if ("HH:MM".equals(horaInicioStr)) horaInicioStr = "";
        if ("HH:MM".equals(horaFinStr)) horaFinStr = "";

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventoToEdit != null) {
            homeViewModel.updateEvento(eventoToEdit.getIdEvento(), selectedCalendarDay, nombre, tipoActividad, color, estado, horaInicioStr, horaFinStr, descripcion);
        } else {
            if (selectedCalendarDay == null) {
                Toast.makeText(getContext(), "Error: Fecha no seleccionada.", Toast.LENGTH_SHORT).show();
                return;
            }
            homeViewModel.saveEvento(selectedCalendarDay, nombre, tipoActividad, color, estado, horaInicioStr, horaFinStr, descripcion);
        }
        dismiss();
    }
}