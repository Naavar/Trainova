package com.navar.trainova;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private MaterialCalendarView calendarView;
    private final Map<CalendarDay, Evento> eventos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Inicialización Auth y UI ---
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            irALogin();
            return;
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Button cerrarSesionButton = findViewById(R.id.cerrarSesionButton);
        cerrarSesionButton.setOnClickListener(v -> cerrarSesion());
        calendarView = findViewById(R.id.calendarView);
        // --- Configuración Calendario ---
        configurarCalendario();
        cargarEventosSimulados();
        agregarDecoradores(); // Aplicar decoradores iniciales

        // --- Listeners Calendario ---
        calendarView.setOnDateChangedListener((@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) -> {
            Evento eventoDelDia = eventos.get(date);
            String message = (eventoDelDia != null) ? "Evento: " + eventoDelDia.getNombre() : "No hay evento registrado";
            if (date.getMonth() == widget.getCurrentDate().getMonth()) { // Mostrar solo si es del mes actual
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        calendarView.setOnMonthChangedListener((widget, date) -> {
            // Reaplicar decoradores al cambiar de mes para actualizar los días de "otro mes"
            agregarDecoradores();
        });
    }

    private void configurarCalendario() {
        calendarView.setTitleFormatter((day) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(day.getYear(), day.getMonth() - 1, day.getDay());
            // Locale para español
            Locale localeSpanish = new Locale("es", "ES");
            java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("MMMM 'de' yyyy", localeSpanish);
            // Poner en mayúsculas si lo deseas
            return dateFormat.format(cal.getTime()).toUpperCase();
        });
    }

    private void cargarEventosSimulados() {
        eventos.clear();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int colorNaranja = Color.parseColor("#FFA000");
        int colorRojo = Color.parseColor("#F44336");
        int colorVerde = Color.parseColor("#4CAF50");
        int colorDescanso = Color.parseColor("#80CBC4");
        int colorPrimario = ContextCompat.getColor(this, R.color.colorPrimario);

        // ---- Eventos ----
        eventos.put(CalendarDay.from(year, 4, 12), new Evento("Convocatoria", colorNaranja));
        eventos.put(CalendarDay.from(year, 4, 14), new Evento("Entrega Interfaz", colorRojo));
        eventos.put(CalendarDay.from(year, 4, 20), new Evento("Curso Leng. Marcas", colorVerde));
        eventos.put(CalendarDay.from(year, 4, 15), new Evento("Reunión Proyecto", colorPrimario));
        eventos.put(CalendarDay.from(year, 4, 22), new Evento("Demo Cliente", colorVerde));

        // ---- Descansos ----
        int[] sabadosFeb = {1, 8, 15, 22};
        int[] domingosFeb = {2, 9, 16, 23};
        for (int dia : sabadosFeb)
            eventos.put(CalendarDay.from(year, 2, dia), new Evento("Descanso", colorDescanso));
        for (int dia : domingosFeb)
            eventos.put(CalendarDay.from(year, 2, dia), new Evento("Descanso", colorDescanso));
        int[] sabadosAbr = {5, 12, 19, 26};
        int[] domingosAbr = {6, 13, 20, 27};
        for (int dia : sabadosAbr)
            eventos.put(CalendarDay.from(year, 4, dia), new Evento("Descanso", colorDescanso));
        for (int dia : domingosAbr)
            eventos.put(CalendarDay.from(year, 4, dia), new Evento("Descanso", colorDescanso));
    }

    private void agregarDecoradores() {
        calendarView.removeDecorators(); // Limpiar siempre primero

        // --- Decoradores en Orden de Aplicación ---

        // 1. Decorador para días de OTRO mes (Texto gris claro)
        OtherMonthDayDecorator otherMonthDecorator = new OtherMonthDayDecorator(this, calendarView);
        calendarView.addDecorator(otherMonthDecorator);

        // 2. Decoradores de Eventos (Puntos de color debajo del número)
        //    Agrupamos por día para poder añadir múltiples puntos si hay varios eventos el mismo día
        //    (Aunque nuestra clase Evento actual solo permite 1 por día, esto es más escalable)
        Map<CalendarDay, Set<Integer>> coloresPorDia = new HashMap<>();
        if (eventos != null) {
            for (Map.Entry<CalendarDay, Evento> entry : eventos.entrySet()) {
                CalendarDay dia = entry.getKey();
                Evento evento = entry.getValue();
                if (evento != null) {
                    coloresPorDia.computeIfAbsent(dia, k -> new HashSet<>()).add(evento.getColor());
                }
            }
        }

        // Añadir un SingleEventDecorator (que ahora usa DotSpan) por cada día que tenga eventos
        for (Map.Entry<CalendarDay, Set<Integer>> entry : coloresPorDia.entrySet()) {
            CalendarDay diaConEvento = entry.getKey();
            Set<Integer> coloresDelDia = entry.getValue(); // Set de colores para ese día
            // Pasamos el Set de colores al decorador
            SingleEventDecorator eventDecorator = new SingleEventDecorator(coloresDelDia, diaConEvento);
            calendarView.addDecorator(eventDecorator);
        }
    }


    // --- Métodos de Sesión ---
    private void cerrarSesion() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(HomeActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irALogin();
        });
    }

    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    // --- CLASES INTERNAS / DECORADORES ---

    // Clase Evento (sin cambios)
    public static class Evento {
        private final String nombre;
        private final int color;

        public Evento(String nombre, int color) {
            this.nombre = nombre;
            this.color = color;
        }

        public String getNombre() {
            return nombre;
        }

        public int getColor() {
            return color;
        }
    }

    // --- DECORADOR PARA DÍAS DE OTRO MES (sin cambios) ---
    public static class OtherMonthDayDecorator implements DayViewDecorator {
        private final int otherMonthColor;
        private final int currentMonth;

        public OtherMonthDayDecorator(@NonNull Context context, @NonNull MaterialCalendarView calendarView) {
            this.otherMonthColor = ContextCompat.getColor(context, R.color.colorOtherMonthDayText);
            this.currentMonth = calendarView.getCurrentDate().getMonth();
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            return day.getMonth() != currentMonth;
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(otherMonthColor));
            view.setDaysDisabled(true);
        }
    }

    // --- DECORADOR DE EVENTOS ---
    // Ahora decora un solo día pero puede aplicar múltiples puntos si es necesario
    public static class SingleEventDecorator implements DayViewDecorator {
        private final Set<Integer> colors; // Puede tener uno o más colores para el día
        private final CalendarDay day;

        // Constructor modificado: recibe los colores y el día específico
        public SingleEventDecorator(Set<Integer> eventColors, CalendarDay day) {
            this.colors = eventColors;
            this.day = day;
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            // Solo decora el día específico para el que fue creado
            return day.equals(this.day);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            // Añade un DotSpan por cada color asociado a este día
            // Puedes ajustar el radio (primer parámetro de DotSpan) si quieres puntos más grandes/pequeños
            // DotSpan.DEFAULT_RADIUS es una opción
            float dotRadius = 4; // Radio pequeño para los puntos
            for (int color : colors) {
                view.addSpan(new DotSpan(dotRadius, color));
            }
            // Ya no cambiamos el fondo: view.setBackgroundDrawable(...) ELIMINADO
        }
    }
}