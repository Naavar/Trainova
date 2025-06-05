package com.navar.trainova.ui.settings;

import android.content.Intent; // Importación añadida
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView; // Importación añadida
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Importación añadida
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth; // Importación añadida
import com.navar.trainova.R;
import com.navar.trainova.ui.auth.AuthActivity; // Importación añadida

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchNotificaciones;
    private SwitchMaterial switchModoOscuro;
    private TextView tvCerrarSesion; // TextView para cerrar sesión
    private FirebaseAuth mAuth; // Instancia de FirebaseAuth

    public static final String KEY_PREF_NOTIFICACIONES = "pref_notificaciones_activadas";
    public static final String KEY_PREF_MODO_OSCURO = "pref_modo_oscuro_activado";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        tvCerrarSesion = findViewById(R.id.tvCerrarSesion); // Inicializar TextView

        mAuth = FirebaseAuth.getInstance(); // Inicializar FirebaseAuth

        cargarPreferenciasGuardadas();
        setupListeners();
    }

    private void cargarPreferenciasGuardadas() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean notificacionesActivadas = sharedPreferences.getBoolean(KEY_PREF_NOTIFICACIONES, false);
        switchNotificaciones.setChecked(notificacionesActivadas);
        boolean modoOscuroActivado = sharedPreferences.getBoolean(KEY_PREF_MODO_OSCURO, true);
        switchModoOscuro.setChecked(modoOscuroActivado);
    }

    private void setupListeners() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(KEY_PREF_NOTIFICACIONES, isChecked);
            editor.apply();
            String estado = isChecked ? "activadas" : "desactivadas";
            Toast.makeText(this, "Notificaciones " + estado, Toast.LENGTH_SHORT).show();
        });

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(KEY_PREF_MODO_OSCURO, isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Toast.makeText(this, "Modo oscuro: " + isChecked, Toast.LENGTH_SHORT).show(); // Comentado para evitar doble Toast si se recrea la actividad
        });

        tvCerrarSesion.setOnClickListener(v -> {
            mostrarDialogoConfirmacionCerrarSesion();
        });
    }

    private void mostrarDialogoConfirmacionCerrarSesion() {
        new AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                cerrarSesionUsuario();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void cerrarSesionUsuario() {
        mAuth.signOut();
        Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Cierra esta actividad y todas las actividades padre en la tarea actual
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }
}