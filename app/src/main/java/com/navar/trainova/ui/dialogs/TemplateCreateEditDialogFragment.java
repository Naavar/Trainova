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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.data.model.ColorOption;
import com.navar.trainova.data.model.EjercicioPlantilla;
import com.navar.trainova.ui.adapters.ColorSpinnerAdapter;
import com.navar.trainova.ui.adapters.EjerciciosPlantillaAdapter;
import com.navar.trainova.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DialogFragment para crear o editar una plantilla de evento (CatalogoEvento).
 * Permite al usuario definir los detalles básicos de la plantilla y gestionar
 * una lista de ejercicios asociados si se trata de una rutina, utilizando
 * un diálogo secundario (EjercicioInputDialogFragment) para la entrada de datos de cada ejercicio.
 */
public class TemplateCreateEditDialogFragment extends DialogFragment implements
    EjercicioInputDialogFragment.OnEjercicioSaveListener {

    /**
     * Interfaz para comunicar el guardado de la plantilla a la actividad o fragmento anfitrión.
     */
    public interface OnTemplateSaveListener {
        void onTemplateSave(CatalogoEvento plantilla);
    }

    private OnTemplateSaveListener saveListener;

    private static final String ARG_PLANTILLA_TO_EDIT = "plantilla_to_edit";

    private EditText etNombre, etDescripcion, etDuracion;
    private Spinner spinnerTipoActividad, spinnerColor;
    private Button btnGuardar, btnCancelar;

    private RecyclerView recyclerViewEjercicios;
    private Button btnAnadirEjercicio;
    private EjerciciosPlantillaAdapter ejerciciosAdapter;
    private List<EjercicioPlantilla> listaDeEjerciciosActual;

    private CatalogoEvento plantillaToEdit;
    private HomeViewModel homeViewModel;

    /**
     * Crea una nueva instancia del diálogo para crear una nueva plantilla.
     * @return Una nueva instancia de TemplateCreateEditDialogFragment.
     */
    public static TemplateCreateEditDialogFragment newInstance() {
        return new TemplateCreateEditDialogFragment();
    }

    /**
     * Crea una nueva instancia del diálogo precargada con datos de una plantilla existente para editarla.
     * @param plantilla El CatalogoEvento a editar.
     * @return Una nueva instancia de TemplateCreateEditDialogFragment.
     */
    public static TemplateCreateEditDialogFragment newInstance(CatalogoEvento plantilla) {
        TemplateCreateEditDialogFragment fragment = new TemplateCreateEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLANTILLA_TO_EDIT, plantilla);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Establece el listener que será notificado cuando la plantilla se guarde.
     * @param listener El listener para el evento de guardado.
     */
    public void setOnTemplateSaveListener(OnTemplateSaveListener listener) {
        this.saveListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        listaDeEjerciciosActual = new ArrayList<>();
        if (getArguments() != null) {
            plantillaToEdit = getArguments().getParcelable(ARG_PLANTILLA_TO_EDIT);
            if (plantillaToEdit != null && plantillaToEdit.getEjercicios() != null) {
                listaDeEjerciciosActual.addAll(plantillaToEdit.getEjercicios());
            }
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

        recyclerViewEjercicios = view.findViewById(R.id.recyclerViewEjerciciosPlantilla);
        btnAnadirEjercicio = view.findViewById(R.id.btnAnadirEjercicio);

        setupSpinners(view);
        setupEjerciciosRecyclerView(view);

        if (plantillaToEdit != null) {
            builder.setTitle(getString(R.string.editar_plantilla_title));
            populateFieldsForEdit();
        } else {
            builder.setTitle(getString(R.string.crear_plantilla_title));
        }

        btnGuardar.setOnClickListener(v -> saveTemplate());
        btnCancelar.setOnClickListener(v -> dismiss());

        btnAnadirEjercicio.setOnClickListener(v -> {
            EjercicioInputDialogFragment ejercicioDialog = EjercicioInputDialogFragment.newInstance(this);
            ejercicioDialog.show(getChildFragmentManager(), "EjercicioInputDialog_New");
        });

        builder.setView(view);
        return builder.create();
    }

    private void setupSpinners(View dialogView) {
        spinnerTipoActividad = dialogView.findViewById(R.id.spinnerTemplateTipo);
        spinnerColor = dialogView.findViewById(R.id.spinnerTemplateColor);

        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.tipos_actividad, android.R.layout.simple_spinner_item);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoActividad.setAdapter(tipoAdapter);

        homeViewModel.colorOptions.observe(this, colorOptions -> {
            if (colorOptions != null && !colorOptions.isEmpty() && getContext() != null) {
                ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(requireContext(), colorOptions);
                spinnerColor.setAdapter(colorAdapter);
                if (plantillaToEdit != null) {
                    selectColorInSpinner(colorOptions, plantillaToEdit.getColorEvento());
                }
            }
        });
    }

    private void setupEjerciciosRecyclerView(View dialogView) {
        recyclerViewEjercicios = dialogView.findViewById(R.id.recyclerViewEjerciciosPlantilla);
        ejerciciosAdapter = new EjerciciosPlantillaAdapter(listaDeEjerciciosActual,
            new EjerciciosPlantillaAdapter.OnEjercicioActionListener() {
            @Override
            public void onEditEjercicio(EjercicioPlantilla ejercicio, int position) {
                EjercicioInputDialogFragment ejercicioDialog = EjercicioInputDialogFragment.newInstance(
                    ejercicio,
                    position,
                    TemplateCreateEditDialogFragment.this
                );
                ejercicioDialog.show(getChildFragmentManager(), "EjercicioInputDialog_Edit");
            }

            @Override
            public void onDeleteEjercicio(int position) {
                if (position >= 0 && position < listaDeEjerciciosActual.size()) {
                    listaDeEjerciciosActual.remove(position);
                    ejerciciosAdapter.notifyItemRemoved(position);
                    ejerciciosAdapter.notifyItemRangeChanged(position, listaDeEjerciciosActual.size());
                    Toast.makeText(getContext(), getString(R.string.ejercicio_borrado_toast),
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerViewEjercicios.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEjercicios.setAdapter(ejerciciosAdapter);
    }

    private void populateFieldsForEdit() {
        if (plantillaToEdit == null) return;
        etNombre.setText(plantillaToEdit.getNombreEvento());
        etDescripcion.setText(plantillaToEdit.getDescripcion());
        etDuracion.setText(plantillaToEdit.getDuracion());
        setSpinnerSelection(spinnerTipoActividad, plantillaToEdit.getTipoEvento());
        if (ejerciciosAdapter != null) {
            ejerciciosAdapter.submitList(new ArrayList<>(listaDeEjerciciosActual));
        }
    }

    private void saveTemplate() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String duracion = etDuracion.getText().toString().trim();
        String tipo = spinnerTipoActividad.getSelectedItem().toString();

        int color = 0;
        if (spinnerColor.getSelectedItem() instanceof ColorOption) {
            color = ((ColorOption) spinnerColor.getSelectedItem()).getColorValue();
        } else if (plantillaToEdit != null) {
            color = plantillaToEdit.getColorEvento();
        }

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.error_nombre_plantilla_obligatorio),
                Toast.LENGTH_SHORT).show();
            return;
        }
        if (duracion.isEmpty()){
            Toast.makeText(getContext(), getString(R.string.error_duracion_obligatoria),
                Toast.LENGTH_SHORT).show();
            return;
        }

        CatalogoEvento plantillaResultante = (plantillaToEdit != null) ? plantillaToEdit : new CatalogoEvento();
        if (plantillaToEdit != null) {
            plantillaResultante.setId(plantillaToEdit.getId());
            plantillaResultante.setUidCreador(plantillaToEdit.getUidCreador());
        }

        plantillaResultante.setNombreEvento(nombre);
        plantillaResultante.setDescripcion(descripcion);
        plantillaResultante.setDuracion(duracion);
        plantillaResultante.setTipoEvento(tipo);
        plantillaResultante.setColorEvento(color);
        plantillaResultante.setEjercicios(new ArrayList<>(listaDeEjerciciosActual));

        if (saveListener != null) {
            saveListener.onTemplateSave(plantillaResultante);
        }
        dismiss();
    }

    /**
     * Método callback de EjercicioInputDialogFragment.OnEjercicioSaveListener.
     * Se llama cuando un ejercicio es guardado (ya sea nuevo o editado) desde el diálogo de ejercicios.
     * @param ejercicio El objeto EjercicioPlantilla que fue guardado.
     * @param position La posición del ejercicio en la lista. Es -1 si es un ejercicio nuevo.
     */
    @Override
    public void onEjercicioSave(EjercicioPlantilla ejercicio, int position) {
        if (position == -1) {
            listaDeEjerciciosActual.add(ejercicio);
            if (ejerciciosAdapter != null) {
                ejerciciosAdapter.notifyItemInserted(listaDeEjerciciosActual.size() - 1);
                if(recyclerViewEjercicios != null) recyclerViewEjercicios
                    .scrollToPosition(listaDeEjerciciosActual.size() - 1);
            }
        } else {
            if (position >= 0 && position < listaDeEjerciciosActual.size()) {
                listaDeEjerciciosActual.set(position, ejercicio);
                if (ejerciciosAdapter != null) ejerciciosAdapter.notifyItemChanged(position);
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position) != null && value.equals(adapter.getItem(position).toString())) {
                spinner.setSelection(position);
                return;
            }
        }
    }

    private void selectColorInSpinner(List<ColorOption> colorOptions, int colorValue) {
        if (colorOptions == null || spinnerColor.getAdapter() == null || colorOptions.isEmpty()) return;
        for (int i = 0; i < colorOptions.size(); i++) {
            if (colorOptions.get(i).getColorValue() == colorValue) {
                spinnerColor.setSelection(i);
                break;
            }
        }
    }
}