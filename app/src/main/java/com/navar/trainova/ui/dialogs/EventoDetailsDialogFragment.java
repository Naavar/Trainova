package com.navar.trainova.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.navar.trainova.R;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.ui.home.HomeViewModel;

import java.util.Locale;

/**
 * Fragmento de diálogo que muestra los detalles de un objeto Evento.
 * Permite al usuario ver la información completa de un evento seleccionado,
 * así como opciones para editarlo o eliminarlo.
 * Interactúa con un HomeViewModel para realizar operaciones de datos.
 */
public class EventoDetailsDialogFragment extends DialogFragment {

    /** Etiqueta para identificar este diálogo */
    public static final String TAG = "EventoDetailsDialog";
    /** Clave para pasar el objeto Evento como argumento */
    private static final String ARG_EVENTO = "evento_details_arg";
    /** ViewModel para interactuar con la lógica de negocio */
    private HomeViewModel homeViewModel;
    /** El evento cuyos detalles se están mostrando */
    private Evento currentEvento;

    /** Vistas del layout del diálogo */
    private TextView tvNombreMostrado;
    private TextView tvFecha;
    private TextView tvHora;
    private TextView tvEstado;
    private View viewColorIndicator;
    private TextView tvDescripcion;
    private Button btnEditar;
    private Button btnEliminar;
    private Button btnCerrar;

    /**
     * Crea una nueva instancia de EventoDetailsDialogFragment.
     * Este método es la forma recomendada de instanciar el diálogo y pasarle el Evento.
     *
     * @param evento El objeto Evento cuyos detalles se van a mostrar.
     * @return Una nueva instancia del fragmento de diálogo.
     */
    public static EventoDetailsDialogFragment newInstance(Evento evento) {
        EventoDetailsDialogFragment fragment = new EventoDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENTO, evento);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Se llama cuando el fragmento es creado.
     * Aquí se inicializa el HomeViewModel y se recupera el objeto Evento
     * pasado como argumento.
     *
     * @param savedInstanceState Si el fragmento se está recreando a partir de un
     *                           estado guardado previamente, este es el estado.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        if (getArguments() != null) {
            currentEvento = getArguments().getParcelable(ARG_EVENTO);
        }
    }

    /**
     * Se llama para crear y devolver un diálogo para el fragmento.
     * Infla el layout del diálogo, enlaza las vistas y configura los listeners
     * para los botones de editar, eliminar y cerrar.
     *
     * @param savedInstanceState El Bundle que puede contener el estado guardado del diálogo.
     * @return Una nueva instancia de Dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Infla el layout personalizado para el diálogo de detalles del evento.
        View view = inflater.inflate(R.layout.dialog_event_details, null);

        // Inicializa las vistas del layout, asegurándose de que los IDs coincidan con el XML.
        tvNombreMostrado = view.findViewById(R.id.tvDetallesNombreMostrado);
        tvFecha = view.findViewById(R.id.tvDetallesFecha);
        tvHora = view.findViewById(R.id.tvDetallesHora);
        tvEstado = view.findViewById(R.id.tvDetallesEstado);
        viewColorIndicator = view.findViewById(R.id.viewDetallesColorIndicator);
        tvDescripcion = view.findViewById(R.id.tvDetallesDescripcion);
        btnEditar = view.findViewById(R.id.btnDetallesEditar);
        btnEliminar = view.findViewById(R.id.btnDetallesEliminar);
        btnCerrar = view.findViewById(R.id.btnDetallesCerrar);

        // Si se cargó un evento, llena los campos con sus detalles.
        if (currentEvento != null) {
            builder.setTitle("Detalles de la Actividad");
            populateDetails(); // Método para rellenar los TextViews con los datos del evento
        } else {
            // Si no se pudo cargar el evento, muestra un mensaje de error y deshabilita los botones.
            builder.setTitle("Error");
            tvNombreMostrado.setText("No se pudo cargar el evento.");
            btnEditar.setEnabled(false);
            btnEliminar.setEnabled(false);
        }

        // Configura el listener para el botón Editar.
        btnEditar.setOnClickListener(v -> {
            if (currentEvento != null) {
                // Solicita al ViewModel que abra el diálogo de edición para este evento.
                homeViewModel.requestEditEventDialog(currentEvento.getIdEvento());
            }
            dismiss(); // Cierra el diálogo de detalles después de solicitar la edición.
        });

        // Configura el listener para el botón Eliminar.
        btnEliminar.setOnClickListener(v -> {
            if (currentEvento != null) {
                showDeleteConfirmationDialog();
            }
        });

        // Configura el listener para el botón Cerrar.
        btnCerrar.setOnClickListener(v -> dismiss()); // Cierra el diálogo.

        builder.setView(view);
        return builder.create();
    }

    /**
     * Rellena los TextViews del diálogo con los detalles del evento actual.
     */
    private void populateDetails() {
        if (currentEvento == null) return;

        tvNombreMostrado.setText(currentEvento.getNombreMostrado());

        // Formatea y muestra la fecha del evento.
        String fechaFormateada = String.format(Locale.getDefault(), "%02d/%02d/%d",
            currentEvento.getFecha().getDay(),
            currentEvento.getFecha().getMonth(),
            currentEvento.getFecha().getYear());
        tvFecha.setText("Fecha: " + fechaFormateada);

        // Formatea y muestra la hora de inicio y fin del evento.
        String horaStr = "No especificada";
        if (currentEvento.getHoraInicio() != null && !currentEvento.getHoraInicio().isEmpty() && !currentEvento.getHoraInicio().equals("HH:MM")) {
            horaStr = currentEvento.getHoraInicio();
            if (currentEvento.getHoraFin() != null && !currentEvento.getHoraFin().isEmpty() && !currentEvento.getHoraFin().equals("HH:MM")) {
                horaStr += " - " + currentEvento.getHoraFin();
            }
        }
        tvHora.setText("Hora: " + horaStr);

        tvEstado.setText("Estado: " + currentEvento.getEstado());
        viewColorIndicator.setBackgroundColor(currentEvento.getColor());

        // Muestra la descripción si está disponible, de lo contrario indica que no hay.
        if (currentEvento.getDescripcion() != null && !currentEvento.getDescripcion().isEmpty()) {
            tvDescripcion.setText("Descripción: " + currentEvento.getDescripcion());
            tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            tvDescripcion.setText("Descripción: (ninguna)");
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar el evento.
     * Si el usuario confirma, el evento es eliminado a través del ViewModel.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar la actividad \"" + currentEvento.getNombreMostrado() + "\"?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                // Llama al ViewModel para eliminar el evento.
                homeViewModel.deleteEvento(currentEvento.getIdEvento());
                Toast.makeText(getContext(), "Actividad eliminada", Toast.LENGTH_SHORT).show();
                dismiss();
            })
            .setNegativeButton("Cancelar", null) // Nada si se cancela
            .setIcon(android.R.drawable.ic_dialog_alert) // Añade un icono de alerta
            .show();
    }
}