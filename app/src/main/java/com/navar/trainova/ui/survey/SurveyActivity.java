package com.navar.trainova.ui.survey;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.navar.trainova.R;
import com.navar.trainova.ui.home.HomeActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Actividad para recopilar datos iniciales del usuario a través de una encuesta.
 * Permite al usuario ingresar su nombre, ubicación, nivel físico y seleccionar
 * sus deportes o actividades preferidas (con un límite de selección y opción de "mostrar más").
 * Los datos recopilados se guardan en Firebase Firestore.
 * Una vez completada, navega a la pantalla principal de la aplicación.
 */
public class SurveyActivity extends AppCompatActivity {
    /** Campo para el nombre completo del usuario. */
    private TextInputEditText editTextNombreCompleto;
    /** Campo para la ubicación del usuario. */
    private TextInputEditText editTextUbicacion;
    /** Spinner para seleccionar el nivel físico del usuario. */
    private Spinner spinnerNivelFisico;
    /** Grupo de Chips para seleccionar los deportes preferidos. */
    private ChipGroup chipGroupDeportes;
    /** Botón para guardar los datos de la encuesta. */
    private Button buttonGuardarEncuesta;
    /** TextView para mostrar/ocultar más deportes. */
    private TextView textViewToggleDeportes;

    /** Instancia de FirebaseAuth para la autenticación. */
    private FirebaseAuth mAuth;
    /** Instancia de FirebaseFirestore para la interacción con la base de datos. */
    private FirebaseFirestore db;

    /** Array de strings para las opciones de niveles físicos. */
    private String[] nivelesFisicos;
    /** Array de strings con la lista completa de deportes disponibles. */
    private String[] listaDeportesCompleta;

    /** Número inicial de deportes a mostrar antes de pulsar "Mostrar más". */
    private static final int INITIAL_SPORTS_TO_SHOW = 20;
    /** Límite máximo de deportes que el usuario puede seleccionar. */
    private static final int MAX_SELECTED_SPORTS = 4;
    /** Indicador de si se están mostrando todos los deportes o la lista inicial. */
    private boolean allSportsShown = false;

    /** Conjunto para almacenar los nombres de los deportes seleccionados de forma persistente durante la interacción del usuario en esta actividad. */
    private Set<String> nombresDeportesPersistentementeSeleccionados = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        nivelesFisicos = getResources().getStringArray(R.array.niveles_fisicos_array);
        listaDeportesCompleta = getResources().getStringArray(R.array.lista_deportes_array);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextNombreCompleto = findViewById(R.id.editTextNombreCompleto);
        editTextUbicacion = findViewById(R.id.editTextUbicacion);
        spinnerNivelFisico = findViewById(R.id.spinnerNivelFisico);
        chipGroupDeportes = findViewById(R.id.chipGroupDeportes);
        buttonGuardarEncuesta = findViewById(R.id.buttonGuardarEncuesta);
        textViewToggleDeportes = findViewById(R.id.textViewToggleDeportes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            editTextNombreCompleto.setText(currentUser.getDisplayName());
        }

        setupNivelFisicoSpinner();
        updateDeportesChipsUI();

        if (listaDeportesCompleta.length > INITIAL_SPORTS_TO_SHOW) {
            textViewToggleDeportes.setVisibility(View.VISIBLE);
            textViewToggleDeportes.setOnClickListener(v -> {
                actualizarSeleccionPersistenteDesdeChipGroup();
                allSportsShown = !allSportsShown;
                updateDeportesChipsUI();
            });
        } else {
            textViewToggleDeportes.setVisibility(View.GONE);
        }

        buttonGuardarEncuesta.setOnClickListener(v -> guardarDatosEncuesta());
    }

    /**
     * Configura el Spinner para la selección del nivel físico.
     * Carga las opciones desde el array 'nivelesFisicos' y establece el adaptador.
     */
    private void setupNivelFisicoSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, nivelesFisicos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNivelFisico.setAdapter(adapter);
    }

    /**
     * Actualiza el conjunto global de deportes seleccionados ('nombresDeportesPersistentementeSeleccionados')
     * basándose en el estado actual de los Chips visibles en 'chipGroupDeportes'.
     * Este método se asegura de que las selecciones realizadas en los Chips visibles se reflejen
     * en el conjunto de datos persistente de la actividad.
     */
    private void actualizarSeleccionPersistenteDesdeChipGroup() {
        for (int i = 0; i < chipGroupDeportes.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDeportes.getChildAt(i);
            String deporteNombre = chip.getText().toString();
            // Solo actualiza si el chip está marcado y no se excede el límite,
            // o si se está desmarcando. La lógica principal del límite está en el listener del chip.
            if (chip.isChecked()) {
                if (nombresDeportesPersistentementeSeleccionados.size() < MAX_SELECTED_SPORTS || nombresDeportesPersistentementeSeleccionados.contains(deporteNombre)) {
                    nombresDeportesPersistentementeSeleccionados.add(deporteNombre);
                }
            } else {
                nombresDeportesPersistentementeSeleccionados.remove(deporteNombre);
            }
        }
    }

    /**
     * Actualiza la interfaz de usuario del ChipGroup de deportes.
     * Muestra una lista inicial limitada de deportes o la lista completa,
     * según el estado de 'allSportsShown'. Mantiene el estado de selección
     * de los deportes al cambiar entre vistas y aplica el límite de selección.
     */
    private void updateDeportesChipsUI() {
        actualizarSeleccionPersistenteDesdeChipGroup();
        chipGroupDeportes.removeAllViews();

        String[] deportesAMostrar;
        if (allSportsShown || listaDeportesCompleta.length <= INITIAL_SPORTS_TO_SHOW) {
            deportesAMostrar = listaDeportesCompleta;
            if (listaDeportesCompleta.length > INITIAL_SPORTS_TO_SHOW) {
                textViewToggleDeportes.setText(getString(R.string.mostrar_menos));
            }
        } else {
            deportesAMostrar = Arrays.copyOfRange(listaDeportesCompleta, 0, INITIAL_SPORTS_TO_SHOW);
            textViewToggleDeportes.setText(getString(R.string.mostrar_mas));
        }

        for (String deporte : deportesAMostrar) {
            Chip chip = new Chip(this);
            chip.setText(deporte);
            chip.setCheckable(true);

            if (nombresDeportesPersistentementeSeleccionados.contains(deporte)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String deporteNombre = buttonView.getText().toString();
                if (isChecked) {
                    // Si se intenta seleccionar uno nuevo y ya se alcanzó el límite
                    if (nombresDeportesPersistentementeSeleccionados.size() >= MAX_SELECTED_SPORTS &&
                        !nombresDeportesPersistentementeSeleccionados.contains(deporteNombre)) {
                        buttonView.setChecked(false); // Revertir la selección
                        Toast.makeText(SurveyActivity.this, "Puedes seleccionar un máximo de " + MAX_SELECTED_SPORTS + " actividades.", Toast.LENGTH_SHORT).show();
                    } else {
                        nombresDeportesPersistentementeSeleccionados.add(deporteNombre);
                    }
                } else {
                    nombresDeportesPersistentementeSeleccionados.remove(deporteNombre);
                }
            });
            chipGroupDeportes.addView(chip);
        }
    }

    /**
     * Recopila todos los datos introducidos por el usuario en la encuesta,
     * realiza validaciones y los guarda en Firebase Firestore.
     * Si los datos se guardan correctamente, navega a la HomeActivity.
     */
    private void guardarDatosEncuesta() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        String nombre = editTextNombreCompleto.getText().toString().trim();
        String ubicacion = editTextUbicacion.getText().toString().trim();

        if (TextUtils.isEmpty(ubicacion)) {
            editTextUbicacion.setError("La ubicación es requerida");
            editTextUbicacion.requestFocus();
            return;
        }

        String nivelFisicoSeleccionado = "";
        if (spinnerNivelFisico.getSelectedItemPosition() > 0) {
            nivelFisicoSeleccionado = spinnerNivelFisico.getSelectedItem().toString();
        } else {
            Toast.makeText(this, "Por favor, selecciona tu nivel físico.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombresDeportesPersistentementeSeleccionados.size() > MAX_SELECTED_SPORTS) {
            Toast.makeText(this, "Error: Demasiadas actividades seleccionadas.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nombresDeportesPersistentementeSeleccionados.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona al menos un deporte o actividad.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Boolean> deportesSeleccionadosMap = new HashMap<>();
        for(String deporteSeleccionado : nombresDeportesPersistentementeSeleccionados) {
            deportesSeleccionadosMap.put(deporteSeleccionado, true);
        }


        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);

        if (!TextUtils.isEmpty(nombre)) {
            userData.put("nombre", nombre);
        } else if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()){
            userData.put("nombre", currentUser.getDisplayName());
        } else {
            userData.put("nombre", "Usuario Trainova");
        }

        userData.put("fechaRegistro", FieldValue.serverTimestamp());
        userData.put("nivelFisico", nivelFisicoSeleccionado);

        Map<String, Object> preferencias = new HashMap<>();
        preferencias.put("ubicacion", ubicacion);
        preferencias.put("deportes", deportesSeleccionadosMap);

        userData.put("preferencias", preferencias);

        db.collection("Usuario").document(uid)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(SurveyActivity.this, "Datos guardados correctamente.", Toast.LENGTH_SHORT).show();
                goToHomeActivity();
            })
            .addOnFailureListener(e -> Toast.makeText(SurveyActivity.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Navega a la HomeActivity y finaliza la actividad actual.
     * Se utiliza después de guardar exitosamente los datos de la encuesta.
     */
    private void goToHomeActivity() {
        Intent intent = new Intent(SurveyActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(SurveyActivity.this, com.navar.trainova.ui.auth.AuthActivity.class);
            startActivity(intent);
            finish();
        }
    }
}