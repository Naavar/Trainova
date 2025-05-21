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
import com.navar.trainova.data.model.ColorOption;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.ui.adapters.ColorSpinnerAdapter;
import com.navar.trainova.ui.home.HomeViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Locale;

/**
 * Fragmento de diálogo para crear o editar un objeto Evento.
 * Este diálogo presenta un formulario al usuario para introducir detalles de un evento
 * (ej. nombre, tipo de actividad, etc.).
 */
public class EventoCreateEditDialogFragment extends DialogFragment {

    /** Etiquetas para identificar el tipo de diálogo */
    public static final String TAG_CREATE = "EventoCreateDialog";
    public static final String TAG_EDIT = "EventoEditDialog";

    /** Claves para los argumentos del Bundle */
    private static final String ARG_CALENDAR_DAY = "calendar_day";
    private static final String ARG_EVENTO_TO_EDIT = "evento_to_edit";

    private HomeViewModel homeViewModel; // ViewModel para manejar la lógica de datos

    /** Vistas del formulario en el diálogo */
    private EditText inputNombre;
    private Spinner spinnerTipoActividad;
    private Spinner spinnerColor;
    private Spinner spinnerEstado;
    private Button btnHoraInicio;
    private Button btnHoraFin;
    private EditText inputDescripcion;
    private Button btnGuardar;
    private Button btnCancelar;

    /** El día del calendario seleccionado para el evento */
    private CalendarDay selectedCalendarDay;
    /** El evento si estamos en modo edición */
    private Evento eventoToEdit;

    /** Arrays para almacenar la hora y el minuto seleccionados para inicio y fin */
    private int[] horaInicioSeleccionada = {0, 0}; // hora, minuto
    private int[] horaFinSeleccionada = {0, 0};   // hora, minuto

    /**
     * Crea una nueva instancia de EventoCreateEditDialogFragment para crear un nuevo evento.
     * @param day El CalendarDay en el que se creará el evento.
     * @return Una nueva instancia del fragmento de diálogo.
     */
    public static EventoCreateEditDialogFragment newInstanceForCreate(CalendarDay day) {
        EventoCreateEditDialogFragment fragment = new EventoCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CALENDAR_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Crea una nueva instancia de EventoCreateEditDialogFragment para editarun evento existente.
     * @param evento El objeto Evento a editar.
     * @return Una nueva instancia del fragmento de diálogo.
     */
    public static EventoCreateEditDialogFragment newInstanceForEdit(Evento evento) {
        EventoCreateEditDialogFragment fragment = new EventoCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENTO_TO_EDIT, evento);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Se llama cuando el fragmento es creado.
     * Aquí se inicializa el HomeViewModel y se recuperan los argumentos
     * pasados al diálogo (el día seleccionado o el evento a editar).
     * @param savedInstanceState Si el fragmento se está recreando a partir de un
     * estado guardado previamente, este es el estado.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        if (getArguments() != null) {
            selectedCalendarDay = getArguments().getParcelable(ARG_CALENDAR_DAY);
            eventoToEdit = getArguments().getParcelable(ARG_EVENTO_TO_EDIT);
            // Si estamos editando un evento, el día seleccionado es el día del evento.
            if (eventoToEdit != null) {
                selectedCalendarDay = eventoToEdit.getCalendarDay();
            }
        }
    }

    /**
     * Se llama para crear y devolver un diálogo para el fragmento.
     * Aquí se infla el layout del diálogo, se enlazan las vistas y se configuran
     * los adaptadores de spinners, los selectores de tiempo y los listeners.
     * @param savedInstanceState El Bundle que puede contener el estado guardado del diálogo.
     * @return Una nueva instancia de Dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Infla el layout personalizado para el diálogo de crear/editar actividad.
        View view = inflater.inflate(R.layout.dialog_crear_actividad, null);

        // Enlaza las vistas del layout con las variables del fragmento.
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

        // Establece el título del diálogo y pre-popula los campos si estamos editando.
        if (eventoToEdit != null) {
            builder.setTitle("Editar Actividad");
            populateFieldsForEdit();
        } else {
            builder.setTitle("Crear Nueva Actividad");
            // Establece el texto por defecto para las horas si es un evento nuevo.
            btnHoraInicio.setText("HH:MM");
            btnHoraFin.setText("HH:MM");
        }

        btnGuardar.setOnClickListener(v -> saveEvent());
        btnCancelar.setOnClickListener(v -> dismiss()); // Cierra el diálogo al cancelar.

        builder.setView(view); // Establece la vista personalizada en el diálogo.
        return builder.create();
    }

    /**
     * Configura los adaptadores para el spinner de tipo de actividad y el spinner de estado.
     * Estos se configuran directamente desde recursos estáticos.
     */
    private void setupSpinnersWithoutObservers() {
        // Adaptador para el spinner de Tipo de Actividad
        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.tipos_actividad, android.R.layout.simple_spinner_item);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoActividad.setAdapter(tipoAdapter);

        // Adaptador para el spinner de Estado
        String[] estados = {"Pendiente", "En progreso", "Completado", "Cancelado"};
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, estados);
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);
    }

    /**
     * Configura un observador para las opciones de color del ViewModel.
     * Cuando las opciones de color están disponibles, actualiza el adaptador del spinner de color
     * y selecciona el color correcto si estamos editando un evento.
     */
    private void setupColorObserver() {
        // Observa los cambios en la lista de opciones de color del ViewModel.
        homeViewModel.colorOptions.observe(this, colorOptions -> {
            if (colorOptions != null && !colorOptions.isEmpty()) {
                // Crea un adaptador personalizado para el spinner de color.
                ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(requireContext(), colorOptions);
                spinnerColor.setAdapter(colorAdapter);

                // Si estamos editando un evento, selecciona el color correspondiente en el spinner.
                if (eventoToEdit != null) {
                    for (int i = 0; i < colorOptions.size(); i++) {
                        if (colorOptions.get(i).getColorValue() == eventoToEdit.getColor()) {
                            spinnerColor.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Configura los listeners para los botones de selección de hora de inicio y fin,
     * abriendo un TimePickerDialog al hacer clic.
     */
    private void setupTimePickers() {
        btnHoraInicio.setOnClickListener(v -> showTimePicker(true));
        btnHoraFin.setOnClickListener(v -> showTimePicker(false));
    }

    /**
     * Muestra un TimePickerDialog para que el usuario seleccione una hora.
     * @param isStartTime true si se está seleccionando la hora de inicio, false para la hora de fin.
     */
    private void showTimePicker(final boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        int hourToSet = isStartTime ? horaInicioSeleccionada[0] : horaFinSeleccionada[0];
        int minuteToSet = isStartTime ? horaInicioSeleccionada[1] : horaFinSeleccionada[1];

        // Si es un evento nuevo y las horas no se han establecido, o si son 00:00,
        // se usa la hora actual como valor inicial para el TimePickerDialog.
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
            }, hourToSet, minuteToSet, true); // true para formato de 24 horas
        timePickerDialog.show();
    }

    /**
     * Rellena los campos del formulario con los datos del evento si estamos en modo edición.
     */
    private void populateFieldsForEdit() {
        if (eventoToEdit == null) return;
        inputNombre.setText(eventoToEdit.getNombre());
        setSpinnerSelection(spinnerTipoActividad, eventoToEdit.getTipoActividad());
        setSpinnerSelection(spinnerEstado, eventoToEdit.getEstado());
        // La selección del color se gestiona en el `setupColorObserver`

        inputDescripcion.setText(eventoToEdit.getDescripcion());

        // Manejo de la hora de inicio
        if (eventoToEdit.getHoraInicio() != null && !eventoToEdit.getHoraInicio().isEmpty() && !eventoToEdit.getHoraInicio().equals("HH:MM")) {
            try {
                String[] partsInicio = eventoToEdit.getHoraInicio().split(":");
                horaInicioSeleccionada[0] = Integer.parseInt(partsInicio[0]);
                horaInicioSeleccionada[1] = Integer.parseInt(partsInicio[1]);
                btnHoraInicio.setText(eventoToEdit.getHoraInicio());
            } catch (Exception e) {
                // En caso de error de formato, resetea la hora
                btnHoraInicio.setText("HH:MM");
                horaInicioSeleccionada[0] = 0; horaInicioSeleccionada[1] = 0;
            }
        } else {
            btnHoraInicio.setText("HH:MM");
            horaInicioSeleccionada[0] = 0; horaInicioSeleccionada[1] = 0;
        }

        // Manejo de la hora de fin
        if (eventoToEdit.getHoraFin() != null && !eventoToEdit.getHoraFin().isEmpty() && !eventoToEdit.getHoraFin().equals("HH:MM")) {
            try {
                String[] partsFin = eventoToEdit.getHoraFin().split(":");
                horaFinSeleccionada[0] = Integer.parseInt(partsFin[0]);
                horaFinSeleccionada[1] = Integer.parseInt(partsFin[1]);
                btnHoraFin.setText(eventoToEdit.getHoraFin());
            } catch (Exception e) {
                // En caso de error de formato, resetea la hora
                btnHoraFin.setText("HH:MM");
                horaFinSeleccionada[0] = 0; horaFinSeleccionada[1] = 0;
            }
        } else {
            btnHoraFin.setText("HH:MM");
            horaFinSeleccionada[0] = 0; horaFinSeleccionada[1] = 0;
        }
    }

    /**
     * Ayuda a seleccionar un valor en un spinner dado el valor como String.
     * @param spinner El Spinner en el que se desea establecer la selección.
     * @param value El valor (como String) a seleccionar.
     */
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

    /**
     * Recopila los datos del formulario, realiza validaciones básicas y luego
     * llama al ViewModel para guardar o actualizar el evento. Finalmente, cierra el diálogo.
     */
    private void saveEvent() {
        String nombre = inputNombre.getText().toString().trim();
        String tipoActividad = spinnerTipoActividad.getSelectedItem().toString();

        ColorOption selectedColorOption = null;
        if (spinnerColor.getSelectedItem() instanceof ColorOption) {
            selectedColorOption = (ColorOption) spinnerColor.getSelectedItem();
        }
        // Si no hay una opción de color seleccionada, usa un naranja por defecto.
        int color = (selectedColorOption != null) ? selectedColorOption.getColorValue() : android.graphics.Color.parseColor("#FFA000");

        String estado = spinnerEstado.getSelectedItem().toString();
        String horaInicioStr = btnHoraInicio.getText().toString();
        String horaFinStr = btnHoraFin.getText().toString();
        String descripcion = inputDescripcion.getText().toString().trim();

        // Si las horas muestran el texto por defecto "HH:MM", se guardan como cadenas vacías.
        if ("HH:MM".equals(horaInicioStr)) horaInicioStr = "";
        if ("HH:MM".equals(horaFinStr)) horaFinStr = "";

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventoToEdit != null) {
            homeViewModel.updateEvento(eventoToEdit.getId(), selectedCalendarDay, nombre, tipoActividad, color, estado, horaInicioStr, horaFinStr, descripcion);
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