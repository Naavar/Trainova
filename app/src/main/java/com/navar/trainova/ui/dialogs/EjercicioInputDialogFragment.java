package com.navar.trainova.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.navar.trainova.R;
import com.navar.trainova.data.model.EjercicioPlantilla;

/**
 * DialogFragment para crear o editar un EjercicioPlantilla individual.
 */
public class EjercicioInputDialogFragment extends DialogFragment {

    /**
     * Interfaz para comunicar el ejercicio guardado (nuevo o editado)
     * de vuelta al DialogFragment que lo llamó.
     */
    public interface OnEjercicioSaveListener {
        void onEjercicioSave(EjercicioPlantilla ejercicio, int position);
    }

    private static final String ARG_EJERCICIO_TO_EDIT = "ejercicio_to_edit";
    private static final String ARG_POSITION = "position";

    private EditText etNombre, etSeries, etRepeticiones, etDescanso, etNotas;
    private Button btnGuardar, btnCancelar;

    private EjercicioPlantilla ejercicioToEdit;
    private int M_position = -1; // -1 indica nuevo ejercicio
    private OnEjercicioSaveListener saveListener;

    /**
     * Crea una nueva instancia para añadir un nuevo ejercicio.
     * @param listener El listener para el evento de guardado.
     * @return Una nueva instancia de EjercicioInputDialogFragment.
     */
    public static EjercicioInputDialogFragment newInstance(OnEjercicioSaveListener listener) {
        EjercicioInputDialogFragment fragment = new EjercicioInputDialogFragment();
        fragment.saveListener = listener;
        return fragment;
    }

    /**
     * Crea una nueva instancia para editar un ejercicio existente.
     * @param ejercicio El EjercicioPlantilla a editar.
     * @param position La posición del ejercicio en la lista (para devolverla al listener).
     * @param listener El listener para el evento de guardado.
     * @return Una nueva instancia de EjercicioInputDialogFragment.
     */
    public static EjercicioInputDialogFragment newInstance(EjercicioPlantilla ejercicio, int position, OnEjercicioSaveListener listener) {
        EjercicioInputDialogFragment fragment = new EjercicioInputDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EJERCICIO_TO_EDIT, ejercicio);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        fragment.saveListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ejercicioToEdit = getArguments().getParcelable(ARG_EJERCICIO_TO_EDIT);
            M_position = getArguments().getInt(ARG_POSITION, -1);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_input_ejercicio, null);

        etNombre = view.findViewById(R.id.etNombreEjercicioInput);
        etSeries = view.findViewById(R.id.etSeriesEjercicioInput);
        etRepeticiones = view.findViewById(R.id.etRepeticionesEjercicioInput);
        etDescanso = view.findViewById(R.id.etDescansoEjercicioInput);
        etNotas = view.findViewById(R.id.etNotasEjercicioInput);
        btnGuardar = view.findViewById(R.id.btnGuardarEjercicioInput);
        btnCancelar = view.findViewById(R.id.btnCancelarEjercicioInput);

        if (ejercicioToEdit != null) {
            builder.setTitle("Editar Ejercicio");
            etNombre.setText(ejercicioToEdit.getNombreEjercicio());
            etSeries.setText(String.valueOf(ejercicioToEdit.getSeries()));
            etRepeticiones.setText(ejercicioToEdit.getRepeticiones());
            etDescanso.setText(ejercicioToEdit.getDescanso());
            etNotas.setText(ejercicioToEdit.getNotas());
        } else {
            builder.setTitle("Añadir Nuevo Ejercicio");
        }

        btnGuardar.setOnClickListener(v -> guardarEjercicio());
        btnCancelar.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void guardarEjercicio() {
        String nombre = etNombre.getText().toString().trim();
        String seriesStr = etSeries.getText().toString().trim();
        String repeticiones = etRepeticiones.getText().toString().trim();
        String descanso = etDescanso.getText().toString().trim();
        String notas = etNotas.getText().toString().trim();

        if (nombre.isEmpty() || seriesStr.isEmpty() || repeticiones.isEmpty() || descanso.isEmpty()) {
            Toast.makeText(getContext(), "Nombre, series, repeticiones y descanso son obligatorios.",
                Toast.LENGTH_SHORT).show();
            return;
        }

        int series;
        try {
            series = Integer.parseInt(seriesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Las series deben ser un número.", Toast.LENGTH_SHORT).show();
            return;
        }

        EjercicioPlantilla ejercicioResultado = (ejercicioToEdit != null) ? ejercicioToEdit :
            new EjercicioPlantilla();
        ejercicioResultado.setNombreEjercicio(nombre);
        ejercicioResultado.setSeries(series);
        ejercicioResultado.setRepeticiones(repeticiones);
        ejercicioResultado.setDescanso(descanso);
        ejercicioResultado.setNotas(notas);

        if (saveListener != null) {
            saveListener.onEjercicioSave(ejercicioResultado, M_position);
        }
        dismiss();
    }
}