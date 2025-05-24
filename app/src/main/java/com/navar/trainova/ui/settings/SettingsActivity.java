package com.navar.trainova.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.navar.trainova.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchNotificaciones;
    private SwitchMaterial switchModoOscuro;

    public static final String KEY_PREF_NOTIFICACIONES = "pref_notificaciones_activadas";
    public static final String KEY_PREF_MODO_OSCURO = "pref_modo_oscuro_activado";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);

        cargarPreferenciasGuardadas();
        setupListeners();
    }

    private void cargarPreferenciasGuardadas() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean notificacionesActivadas = sharedPreferences.getBoolean(KEY_PREF_NOTIFICACIONES, false);
        switchNotificaciones.setChecked(notificacionesActivadas);
        // Por defecto activado
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
            Toast.makeText(this, "Modo oscuro: " + isChecked, Toast.LENGTH_SHORT).show();

        });
    }
}