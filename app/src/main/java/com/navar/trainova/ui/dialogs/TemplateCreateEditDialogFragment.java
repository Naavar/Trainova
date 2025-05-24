package com.navar.trainova.ui.dialogs;

import android.app.Dialog;
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
import com.navar.trainova.ui.adapters.ColorSpinnerAdapter;
import com.navar.trainova.ui.home.HomeViewModel;

import java.util.List;

public class TemplateCreateEditDialogFragment extends DialogFragment {

    public interface OnTemplateSaveListener {
        void onTemplateSave(CatalogoEvento plantilla);
    }

    private OnTemplateSaveListener saveListener;

    private static final String ARG_PLANTILLA_TO_EDIT = "plantilla_to_edit";

    private EditText etNombre, etDescripcion, etDuracion;
    private Spinner spinnerTipoActividad, spinnerColor;
    private Button btnGuardar, btnCancelar;

    private CatalogoEvento plantillaToEdit;
    private HomeViewModel homeViewModel;

    public static TemplateCreateEditDialogFragment newInstance() {
        return new TemplateCreateEditDialogFragment();
    }

    public static TemplateCreateEditDialogFragment newInstance(CatalogoEvento plantilla) {
        TemplateCreateEditDialogFragment fragment = new TemplateCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLANTILLA_TO_EDIT, plantilla);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnTemplateSaveListener(OnTemplateSaveListener listener) {
        this.saveListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        if (getArguments() != null) {
            plantillaToEdit = getArguments().getParcelable(ARG_PLANTILLA_TO_EDIT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_create_edit_template, null);

        etNombre = view.findViewById(R.id.etTemplateNombre);
        etDescripcion = view.findViewById(R.id.etTemplateDescripcion);
        etDuracion = view.findViewById(R.id.etTemplateDuracion);
        spinnerTipoActividad = view.findViewById(R.id.spinnerTemplateTipo);
        spinnerColor = view.findViewById(R.id.spinnerTemplateColor);
        btnGuardar = view.findViewById(R.id.btnTemplateGuardar);
        btnCancelar = view.findViewById(R.id.btnTemplateCancelar);

        setupSpinners();

        if (plantillaToEdit != null) {
            builder.setTitle("Editar Plantilla");
            populateFieldsForEdit();
        } else {
            builder.setTitle("Crear Nueva Plantilla");
        }

        btnGuardar.setOnClickListener(v -> saveTemplate());
        btnCancelar.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void setupSpinners() {
        // Adaptador para el spinner de Tipo de Actividad
        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.tipos_actividad, android.R.layout.simple_spinner_item);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoActividad.setAdapter(tipoAdapter);

        // Adaptador para el spinner de Color (observando desde el ViewModel)
        homeViewModel.colorOptions.observe(this, colorOptions -> {
            if (colorOptions != null && !colorOptions.isEmpty()) {
                ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(requireContext(), colorOptions);
                spinnerColor.setAdapter(colorAdapter);
                if (plantillaToEdit != null) {
                    selectColorInSpinner(colorOptions, plantillaToEdit.getColorEvento());
                }
            }
        });
    }

    private void populateFieldsForEdit() {
        if (plantillaToEdit == null) return;
        etNombre.setText(plantillaToEdit.getNombreEvento());
        etDescripcion.setText(plantillaToEdit.getDescripcion());
        etDuracion.setText(plantillaToEdit.getDuracion());
        setSpinnerSelection(spinnerTipoActividad, plantillaToEdit.getTipoEvento());
        // La selección de color se maneja en el observer de colorOptions
    }

    private void saveTemplate() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String duracion = etDuracion.getText().toString().trim();
        String tipo = spinnerTipoActividad.getSelectedItem().toString();

        int color = 0; // Color por defecto
        if (spinnerColor.getSelectedItem() instanceof ColorOption) {
            color = ((ColorOption) spinnerColor.getSelectedItem()).getColorValue();
        } else if (plantillaToEdit != null) {
            // Si no hay selección y estamos editando, mantener el color original
            color = plantillaToEdit.getColorEvento();
        }

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), "El nombre de la plantilla es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (duracion.isEmpty()){
            Toast.makeText(getContext(), "La duración es obligatoria", Toast.LENGTH_SHORT).show();
            return;
        }

        CatalogoEvento plantillaResultante;
        if (plantillaToEdit != null) {
            // Estamos editando, modificamos el objeto existente
            plantillaToEdit.setNombreEvento(nombre);
            plantillaToEdit.setDescripcion(descripcion);
            plantillaToEdit.setDuracion(duracion);
            plantillaToEdit.setTipoEvento(tipo);
            plantillaToEdit.setColorEvento(color);
            plantillaResultante = plantillaToEdit;
        } else {
            plantillaResultante = new CatalogoEvento(nombre, descripcion, duracion, tipo, color, null);
        }

        if (saveListener != null) {
            saveListener.onTemplateSave(plantillaResultante);
        }
        dismiss();
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
        if (colorOptions == null) return;
        for (int i = 0; i < colorOptions.size(); i++) {
            if (colorOptions.get(i).getColorValue() == colorValue) {
                spinnerColor.setSelection(i);
                break;
            }
        }
    }
}